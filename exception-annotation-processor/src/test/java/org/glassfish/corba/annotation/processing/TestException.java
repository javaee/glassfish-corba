package org.glassfish.corba.annotation.processing;

import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Message;

@ExceptionWrapper(idPrefix = "TEST")
public interface TestException {

    @Message( "this is a test" )
    void doSomething();

}
