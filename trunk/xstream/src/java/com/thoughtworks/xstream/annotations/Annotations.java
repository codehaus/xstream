package com.thoughtworks.xstream.annotations;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import com.thoughtworks.xstream.InitializationException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Contains utility methods that enable to configure an XStream instance
 * with class and field aliases, based on a class decorated
 * with annotations defined in this package.
 *
 * @author Emil Kirschner
 * @author Chung-Onn Cheong
 */
public class Annotations {
	/**
	 * Collection of visited types.
	 */
    private static final Set<Class<?>> visitedTypes = new HashSet<Class<?>>();

    /**
     * This class is not instantiable
     */
    private Annotations() {
    }

    /**
     * Configures aliases on the specified XStream object based on annotations that decorate the specified class.
     * It will recursively invoke itself for each field annotated with XStreamContainedType. If a field containing
     * such annotation is parameterized, a recursive call for each of its parameters type will be made.
     *
     * @param topLevelClasses the class for which the XStream object is configured.
     * This class is expected to be decorated with annotations defined in this package.
     * @param xstream the XStream object that will be configured
     */
    public static synchronized void configureAliases(XStream xstream, Class<?>... topLevelClasses) {
        visitedTypes.clear();
        for(Class<?> topLevelClass : topLevelClasses){
            configureClass(xstream, topLevelClass);
        }
    }

    private static synchronized void configureClass(XStream xstream, Class<?> configurableClass) {
        if (configurableClass == null
              || visitedTypes.contains(configurableClass)) {
            return;
        }

        if(Converter.class.isAssignableFrom(configurableClass)){
            Class<Converter> converterType = (Class<Converter>)configurableClass;
            registerConverter(xstream, converterType);
            return;
        }

        visitedTypes.add(configurableClass);

        //Do Class Level Converters
        AnnotatedElement element = configurableClass;
        if(configurableClass.isAnnotationPresent(XStreamConverters.class)){
            XStreamConverters convertersAnnotation = element.getAnnotation(XStreamConverters.class);
            for(XStreamConverter converterAnnotation : convertersAnnotation.value()){
                registerConverter(xstream, converterAnnotation.value());
            }
        }

        //Do Class Level - Converter
        if(configurableClass.isAnnotationPresent(XStreamConverter.class)){
            XStreamConverter converterAnnotation = element.getAnnotation(XStreamConverter.class);
            registerConverter(xstream, converterAnnotation.value());
        }

        //Do Class Level Alias
        if(configurableClass.isAnnotationPresent(XStreamAlias.class)){
            XStreamAlias aliasAnnotation = element.getAnnotation(XStreamAlias.class);
            if(aliasAnnotation.impl() != Void.class){
                //Alias for Interface/Class with an impl
                xstream.alias(aliasAnnotation.value(), configurableClass, aliasAnnotation.impl());
                if(configurableClass.isInterface()){
                    configureClass(xstream,aliasAnnotation.impl()); //alias Interface's impl
                    return;
                }
            }else{
                xstream.alias(aliasAnnotation.value(), configurableClass);
            }
        }

        //Do Class Level ImplicitCollection
        if(configurableClass.isAnnotationPresent(XStreamImplicitCollection.class)){
            XStreamImplicitCollection implicitColAnnotation = element.getAnnotation(XStreamImplicitCollection.class);
            String fieldName = implicitColAnnotation.value();
            String itemFieldName = implicitColAnnotation.item();
            Field field;
            try {
                field = configurableClass.getDeclaredField(fieldName);
                Class itemType = getFieldParameterizedType(field, xstream);
                if (itemType == null) {
                    xstream.addImplicitCollection(configurableClass, fieldName);
                } else {
                    if (itemFieldName.equals("")) {
                        xstream.addImplicitCollection(configurableClass, fieldName,
                                itemType);
                    } else {
                        xstream.addImplicitCollection(configurableClass, fieldName,
                                itemFieldName, itemType);
                    }
                }
            } catch (Exception e) {
                System.err.println("Fail to derive ImplicitCollection member type");
            }
        }

        //Do Member Level Field annotations
        Field[] fields = configurableClass.getDeclaredFields();
        for (Field field : fields) {
            if(field.isSynthetic()) continue;

            Class fieldType = field.getType();

            // recursive calls for fields
            if(field.isAnnotationPresent(XStreamContainedType.class)){
                configureClass(xstream, fieldType);
                configureParameterizedTypes(field, xstream);
            }

            //Alias the member's Type
            boolean shouldAlias = field.isAnnotationPresent(XStreamAlias.class);
            boolean isAttribute = field.isAnnotationPresent(XStreamAsAttribute.class);
			if(shouldAlias && !isAttribute){
                XStreamAlias fieldXStreamAliasAnnotation =  field.getAnnotation(XStreamAlias.class);
                xstream.aliasField(fieldXStreamAliasAnnotation.value(), configurableClass, field.getName());
                configureClass(xstream, field.getType());
            }
			if(isAttribute){
                xstream.useAttributeFor(configurableClass, field.getName());
                if(shouldAlias) {
                    XStreamAlias fieldXStreamAliasAnnotation =  field.getAnnotation(XStreamAlias.class);
                    xstream.aliasAttribute(configurableClass, field.getName(), fieldXStreamAliasAnnotation.value());
                }
                configureClass(xstream, field.getType());
            }
            // Do field level implicit collection
            if (field.isAnnotationPresent(XStreamImplicit.class)) {
                if (!Collection.class.isAssignableFrom(field.getType())) {
                    throw new InitializationException("@XStreamImplicit must be assigned to Collection types, but \""
                        + field.getDeclaringClass().getName() + ":" + field.getName() + " is of type " + field.getType().getName());
                }
                XStreamImplicit implicitAnnotation =  field.getAnnotation(XStreamImplicit.class);
                String fieldName = field.getName();
                String itemFieldName = implicitAnnotation.itemFieldName();
                Class itemType = getFieldParameterizedType(field, xstream);
                if (itemFieldName != null && !"".equals(itemFieldName)) {
                    xstream.addImplicitCollection(configurableClass, fieldName, itemFieldName, itemType);
                } else {
                    xstream.addImplicitCollection(configurableClass, fieldName, itemType);
                }
            }

           //Do field level OmitField
           if (field.isAnnotationPresent(XStreamOmitField.class)){
               String fieldName = field.getName();
               xstream.omitField(configurableClass, fieldName);
           }

        }

        //Do Member Classes Alias
        for(Class<?>memberClass : configurableClass.getDeclaredClasses()){
            configureClass(xstream, memberClass);
        }

        //Do Superclass and Superinterface Alias
        Class superClass = configurableClass.getSuperclass();
        if (superClass != null && !Object.class.equals(superClass))
            configureClass(xstream, superClass);
        Class[] interfaces = configurableClass.getInterfaces();
        for(Class intf : interfaces){
            configureClass(xstream, intf);
        }

    }


    private static void registerConverter(XStream xstream, Class<? extends Converter> converterType) {
        Converter converter;
        if(visitedTypes.contains(converterType))
            return;
        visitedTypes.add(converterType);
        if (AbstractCollectionConverter.class.isAssignableFrom(converterType)) {
            try {
                Constructor<? extends Converter> converterConstructor = converterType.getConstructor(Mapper.class);
                converter = converterConstructor.newInstance(xstream.getMapper());
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

        } else {
            try {
                converter = converterType.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        xstream.registerConverter(converter);

    }

    /*
     * Return a concrete class
     */
    private static Class getFieldParameterizedType(Field field, XStream xstream){
        if(field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) field.getGenericType();

            Type typeArgument = pType.getActualTypeArguments()[0];
            Class type = null;
            if(typeArgument instanceof ParameterizedType) {
                type = (Class) ((ParameterizedType) typeArgument).getRawType();
            } else if (typeArgument instanceof Class) {
                type = (Class) typeArgument;
            }
            //Get the interface Impl
            if(type.isInterface()){
                AnnotatedElement element = type;
                XStreamAlias alias =  element.getAnnotation(XStreamAlias.class);
                configureClass(xstream, type);
                type = alias.impl();
                assert !type.isInterface()  : type;
            }
            return type;
        }
        assert false : "Field is raw type :" + field;
        return null;
    }

    /**
     * Invokes configureClass for each parameterized type declared within this field.
     */
    private static void configureParameterizedTypes(Field field, XStream xstream){
        if(field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) field.getGenericType();
            Queue<Type> queue = new LinkedList<Type>(Arrays.asList(pType.getActualTypeArguments()));
            while(!queue.isEmpty()) {
                Type parameter = queue.poll();
                if(parameter instanceof ParameterizedType) {
                    ParameterizedType parameterized = (ParameterizedType) parameter;
                    queue.addAll(Arrays.asList(parameterized.getActualTypeArguments()));
                } else if(parameter instanceof Class) {
                	Class type = (Class) parameter;
                	configureClass(xstream, type);
                }
            }
        }
    }
}
