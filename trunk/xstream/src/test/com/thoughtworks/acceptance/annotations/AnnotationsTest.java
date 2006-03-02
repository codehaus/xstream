package com.thoughtworks.acceptance.annotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.acceptance.AbstractAcceptanceTest;
import com.thoughtworks.xstream.annotations.Annotations;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamContainedType;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Simple tests for class annotations
 * 
 * @author Chung-Onn Cheong
 * @author Mauro Talevi
 */
public class AnnotationsTest extends AbstractAcceptanceTest {
    
	public void testAnnotations()  {
        Annotations.configureAliases(xstream, Person.class, AddressBookInfo.class);
        Map<String, Person> map = new HashMap<String, Person>();
        map.put("first person", new Person("john doe"));
        map.put("second person", new Person("jane doe"));
        String xml = 
                "<map>\n"+
                "  <entry>\n" +
                "    <string>second person</string>\n" +
                "    <person>jane doe</person>\n" +
                "  </entry>\n" +
                "  <entry>\n" +
                "    <string>first person</string>\n" +
                "    <person>john doe</person>\n" +
                "  </entry>\n" +
                "</map>";
        assertBothWays(map, xml);
    }
    
    @XStreamAlias("person")
    @XStreamConverter(PersonConverter.class)
    public static class Person {
        String name;
        AddressBookInfo addressBook;
        
        public Person(String name){
            this.name = name;
            addressBook = new AddressBook();
        }
        
        public boolean equals(Object obj) {
            if((obj == null) || !(obj instanceof Person)) return false;
            return addressBook.equals(((Person)obj).addressBook);
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("name:").append(name)
            .append("addresbook:").append(addressBook);
            return sb.toString();
        }
        
    }
    
    @XStreamAlias(value="addressbook-info", impl=AddressBook.class)
    public interface AddressBookInfo {
        public List<AddressInfo> getAddresses();
        public void setAddresses(List<AddressInfo> address);
    }

    @XStreamAlias("addressbookAlias")
    public static class AddressBook implements AddressBookInfo {
        
        @XStreamContainedType
        private List<AddressInfo> addresses;
        
        public AddressBook(){
            addresses = new ArrayList<AddressInfo>();
            addresses.add(new Address("Home Address", 111));
            addresses.add(new Address("Office Address", 222));
        }

        public List<AddressInfo> getAddresses() {
            return addresses;
        }

        public void setAddresses(List<AddressInfo> addresses) {
            this.addresses = addresses;
            
        }
   
        public boolean equals(Object obj) {
            if((obj == null) || !(obj instanceof AddressBookInfo)) return false;
          return addresses.containsAll(((AddressBookInfo)obj).getAddresses());
        }

    }
    
    @XStreamAlias(value="addressinfoAlias", impl=Address.class)
    public interface AddressInfo {
        public String addr();
        public int zipcode();
    }
    
    @XStreamAlias(value="addressAlias")
    public static class Address implements AddressInfo {

        private String addr;
        private int zipcode;

        public Address(String addr, int zipcode){
            this.addr = addr;
            this.zipcode = zipcode;
        }
        public String addr() {
            return addr;
        }

        public int zipcode() {
            return zipcode;
        }
        
    }
    
    public static class PersonConverter implements Converter{
        public PersonConverter() {}
        
        public String toString(Object obj) {
            return ((Person)obj).name;
        }
        
        public Object fromString(String str) {
            return new Person(str);
        }

        public boolean canConvert(Class type) {
            return type == Person.class;
        }

        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            writer.setValue(toString(source));
        }

        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            return fromString(reader.getValue());
        }
    }

}
