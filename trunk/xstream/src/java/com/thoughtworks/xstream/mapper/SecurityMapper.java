/*
 * Copyright (C) 2014 XStream Committers.
 * All rights reserved.
 *
 * Created on 08. January 2014 by Joerg Schaible
 */
package com.thoughtworks.xstream.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.security.AnyTypePermission;
import com.thoughtworks.xstream.security.ForbiddenClassException;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.TypePermission;


/**
 * A Mapper implementation injecting a security layer based on permission rules for any type required in the
 * unmarshalling process.
 * 
 * @author J&ouml;rg Schaible
 * @since upcoming
 */
public class SecurityMapper extends MapperWrapper {

    private final List<TypePermission> permissions;

    /**
     * Construct a SecurityMapper.
     * 
     * @param wrapped the mapper chain
     * @since upcoming
     */
    public SecurityMapper(final Mapper wrapped) {
        this(wrapped, (TypePermission[])null);
    }

    /**
     * Construct a SecurityMapper.
     * 
     * @param wrapped the mapper chain
     * @param permissions the predefined permissions
     * @since upcoming
     */
    public SecurityMapper(final Mapper wrapped, final TypePermission... permissions) {
        super(wrapped);
        this.permissions = permissions == null //
            ? new ArrayList<TypePermission>()
            : new ArrayList<TypePermission>(Arrays.asList(permissions));
    }

    /**
     * Add a new permission.
     * <p>
     * Permissions are evaluated in the added sequence. An instance of {@link NoTypePermission} or
     * {@link AnyTypePermission} will implicitly wipe any existing permission.
     * </p>
     * 
     * @param permission the permission to add.
     * @since upcoming
     */
    public void addPermission(final TypePermission permission) {
        if (permission.equals(NoTypePermission.NONE) || permission.equals(AnyTypePermission.ANY))
            permissions.clear();
        permissions.add(0, permission);
    }

    @Override
    public Class realClass(final String elementName) {
        final Class type = super.realClass(elementName);
        for (final TypePermission permission : permissions)
            if (permission.allows(type))
                return type;
        throw new ForbiddenClassException(type);
    }
}
