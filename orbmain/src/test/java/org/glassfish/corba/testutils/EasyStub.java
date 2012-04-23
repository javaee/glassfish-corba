package org.glassfish.corba.testutils;

import org.easymock.EasyMock;
import org.easymock.IMockBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class EasyStub {
    public static <T> T stub(Class<T> aClass) {
        IMockBuilder<T> mockBuilder = EasyMock.createMockBuilder(aClass).withConstructor().addMockedMethods(getAbstractMethods(aClass));
        T aStub = mockBuilder.createMock();
        EasyMock.replay(aStub);
        return aStub;
    }

    private static <T> Method[] getAbstractMethods(Class<T> aClass) {
        List<Method> abstractMethods = new ArrayList<Method>();
        for (Method method : aClass.getMethods())
            if (Modifier.isAbstract(method.getModifiers()))
                abstractMethods.add(method);

        return abstractMethods.toArray(new Method[abstractMethods.size()]);
    }
}
