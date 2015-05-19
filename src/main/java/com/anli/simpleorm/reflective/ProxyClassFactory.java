package com.anli.simpleorm.reflective;

import javassist.util.proxy.ProxyFactory;

public class ProxyClassFactory {

    public Class createProxyClass(Class basicClass) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(basicClass);
        return proxyFactory.createClass();
    }
}
