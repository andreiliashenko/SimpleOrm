package com.anli.simpleorm.descriptors;

import java.util.HashMap;
import java.util.Map;

public class UnitDescriptorManager {

    protected final Map<Class, EntityDescriptor> descriptors;
    protected final Map<Class, Class> proxyClasses;

    public UnitDescriptorManager() {
        this.descriptors = new HashMap<>();
        this.proxyClasses = new HashMap<>();
    }

    public void addDescriptor(EntityDescriptor descriptor) {
        descriptors.put(descriptor.getEntityClass(), descriptor);
    }

    public EntityDescriptor getDescriptor(Class entityClass) {
        return descriptors.get(entityClass);
    }

    public void addProxy(Class basicClass, Class proxyClass) {
        proxyClasses.put(proxyClass, basicClass);
    }

    public Class getBasicEntityClass(Class proxyClass) {
        Class basicClass = proxyClasses.get(proxyClass);
        if (basicClass == null) {
            basicClass = proxyClass;
        }
        return basicClass;
    }
}
