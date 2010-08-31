/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * 
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 * 
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.corba.se.spi.orbutil.logex;

import com.sun.corba.se.spi.orbutil.misc.OperationTracer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/** Given an annotated interface, return a Proxy that implements that interface.
 * Interface must be annotated with @ExceptionWrapper( String idPrefix, String loggerName ).
 * id prefix defaults to empty, loggerName defaults to the package name of the annotated
 * class.
 *
 * The behavior of the implementation of each method on the interface is determined
 * in part by its return type as follows:
 * <ul>
 * <li>void.  Such a method can only log a message.</li>
 * <li>String. Such a method may log a message, and also returns the message.</li>
 * <li>A subclass of Exception.  Such a method may log a message, and also returns
 * an exception containing the message.
 * </ul>
 *
 * Each method may be annotated as follows:
 *
 * <ul>
 * <li>@Message( String value ).  This defines the message to be placed in a resource
 * bundle (generated at build time by a separate tool).  The key to the resource
 * bundle is <loggerName>.<methodName>.  The message is prepended with the 
 * idPrefix and the id from the @Log annotation (if @Log is present, otherwise nothing
 * is prepended to the message).  If this annotation is not present, a default message
 * is created from the method name and the arguments.
 * <li>@Log( LogLevel level, int id ).  The presence of this annotation indicates that
 * a log record must be generated, and logger IF the appropriate logger is enabled at
 * the given level (note that LogLevel is an enum used for the annotation, each member
 * of which returns the java.util.logging.Level from a getLevel() method).
 * </ul>
 * 
 * In addition, the @Chain annotation may be used on a method parameter (whose type
 * must be a subclass of Throwable) of a method that returns an exception
 * to indicate that the parameter should be the cause of the returned exception.
 * All other method parameters are used as arguments in formatting the message.
 *
 * @author ken
 */
public class WrapperGenerator {
    private WrapperGenerator() {}

    // Find the outer index in pannos for which the element array
    // contains an annotation of type cls.
    private static int findAnnotatedParameter( Annotation[][] pannos,
        Class<? extends Annotation> cls ) {
        for (int ctr1=0; ctr1<pannos.length; ctr1++ ) {
            final Annotation[] annos = pannos[ctr1] ;
            for (int ctr2=0; ctr2< annos.length; ctr2++ ) {
                Annotation anno = annos[ctr2] ;
                if (cls.isInstance(anno)) {
                    return ctr1 ;
                }
            }
        }

        return -1 ;
    }

    private static Object[] getWithSkip( Object[] args, int skip ) {
        if (skip >= 0) {
            Object[] result = new Object[args.length-1] ;
            int rindex = 0 ;
            for (int ctr=0; ctr<args.length; ctr++) {
                if (ctr != skip) {
                    result[rindex++] = args[ctr] ;
                }
            }
            return result ;
        } else {
            return args ;
        }
    }

    private static String getMessage( Method method, int numParams, 
        String idPrefix, int logId ) {

        final Message message = method.getAnnotation( Message.class ) ;
        final StringBuilder sb = new StringBuilder() ;
        sb.append( idPrefix ) ;
        sb.append( logId ) ;
        sb.append( ": " ) ;
                    
        if (message == null) {
            sb.append( method.getName() ) ;
            sb.append( ' ' ) ;
            for (int ctr=0; ctr<numParams; ctr++) {
                if (ctr>0) {
                    sb.append( ", " ) ;
                }

                sb.append( "arg" ) ;
                sb.append( ctr ) ;
                sb.append( "={" + ctr + "}" ) ;
            }
        } else {
            sb.append( message.value() ) ;
        }

        return sb.toString() ;
    }

    private static void inferCaller( LogRecord lrec ) {
	// Private method to infer the caller's class and method names

	// Get the stack trace.
	StackTraceElement stack[] = (new Throwable()).getStackTrace();
	StackTraceElement frame = null ;
	String wcname = "$Proxy" ; // Is this right?  Do we always have Proxy$n here?
	String baseName = WrapperGenerator.class.getName() ;
	String nestedName = WrapperGenerator.class.getName() + "$1" ;

	// The top of the stack should always be a method in the wrapper class,
	// or in this base class.
	// Search back to the first method not in the wrapper class or this class.
	int ix = 0;
	while (ix < stack.length) {
	    frame = stack[ix];
	    String cname = frame.getClassName();
	    if (!cname.contains(wcname) && !cname.equals(baseName)
                && !cname.equals(nestedName))  {
		break;
	    }

	    ix++;
	}

	// Set the class and method if we are not past the end of the stack
	// trace
	if (ix < stack.length) {
	    lrec.setSourceClassName(frame.getClassName());
	    lrec.setSourceMethodName(frame.getMethodName());
	}
    }

    private static Exception makeException( Class<?> rtype, String msg ) {
        try {
            Constructor cons = rtype.getConstructor(String.class);
            return (Exception) cons.newInstance(msg);
        } catch (InstantiationException ex) {
            throw new RuntimeException( ex ) ;
        } catch (IllegalAccessException ex) {
            throw new RuntimeException( ex ) ;
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException( ex ) ;
        } catch (InvocationTargetException ex) {
            throw new RuntimeException( ex ) ;
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException( ex ) ;
        } catch (SecurityException ex) {
            throw new RuntimeException( ex ) ;
        }
    }

    private static String handleMessageOnly( Method method, Logger logger,
        Object[] messageParams ) {

        // Just format the message: no exception ID or log level
        // This code is adapted from java.util.logging.Formatter.formatMessage
        String msg = (String)method.getAnnotation( Message.class ).value() ;
        String transMsg ;
        ResourceBundle catalog = logger.getResourceBundle() ;
        try {
            transMsg = catalog.getString( msg ) ;
        } catch (Exception exc) {
            // Ignore exc: hard to report here.
            // Ignore findbugs: regardless of exception, just use msg as result.
            transMsg = msg ;
        }

        String result ;
        if (transMsg.indexOf( "{0" ) >= 0 ) {
            result = java.text.MessageFormat.format( transMsg, messageParams ) ;
        } else {
            result = transMsg ;
        }

        return result ;
    }

    private enum ReturnType { EXCEPTION, STRING, NULL } ;

    private static ReturnType classifyReturnType( Method method ) {
        Class<?> rtype = method.getReturnType() ;
        if (Exception.class.isAssignableFrom(rtype)) {
            return ReturnType.EXCEPTION ;
        } else if (rtype.equals( String.class)) {
            return ReturnType.STRING ;
        } else if (rtype.equals( void.class ) ) {
            return ReturnType.NULL ;
        } else {
            throw new RuntimeException( "Method " + method
                + " has an illegal return type" ) ;
        }
    }

    private static LogRecord makeLogRecord( Level level, String key,
        Object[] args, Logger logger ) {
        LogRecord result = new LogRecord( level, key ) ;
        if (args != null && args.length > 0) {
            result.setParameters( args ) ;
        }

        result.setLoggerName( logger.getName() ) ;
        result.setResourceBundle( logger.getResourceBundle() ) ;
        if (level != Level.INFO) {
            inferCaller( result ) ;
        }

        return result ;
    }
    
    // Note: This is used ONLY to format the message used in the method
    // result, not in the actual log handler.
    // We define this class simply to re-use the code in formatMessage.
    static class ShortFormatter extends Formatter {
        @Override
        public String format( LogRecord record ) {
            StringBuilder sb = new StringBuilder() ;
            sb.append(record.getLevel().getLocalizedName());
            sb.append(": ");
            String message = formatMessage( record ) ;
            sb.append(message);
            return sb.toString() ;
        }
    }

    private final static ShortFormatter formatter = new ShortFormatter() ;

    private static Object handleFullLogging( Log log, Method method, Logger logger,
        String idPrefix, Object[] messageParams, Throwable cause )  {

        final Level level = log.level().getLevel() ;
        final ReturnType rtype = classifyReturnType( method ) ;
        final int len = messageParams == null ? 0 : messageParams.length ;
        final String msgString = getMessage( method, len, idPrefix, log.id() ) ;
        final LogRecord lrec = makeLogRecord( level, msgString,
            messageParams, logger ) ;
        final String message = formatter.format( lrec ) ;

        Exception exc = null ;
        if (rtype == ReturnType.EXCEPTION) {
            exc = makeException( method.getReturnType(), message ) ;
            if (cause != null) {
                exc.initCause( cause ) ;
            }

            if (level != Level.INFO) {
                lrec.setThrown( exc ) ;
            }
        }

        if (logger.isLoggable(level)) {
            final String context = OperationTracer.getAsString() ;
            String newMsg = msgString ;
            if (context.length() > 0) {
                newMsg += "\nCONTEXT:" + context ;
                lrec.setMessage( newMsg ) ;
            }
            logger.log( lrec ) ;
        }

        switch (rtype) {
            case EXCEPTION : return exc ;
            case STRING : return message ;
            default : return null ;
        }
    }

    public static <T> T makeWrapper( final Class<T> cls ) {
        // Must have an interface to use a Proxy.
        if (!cls.isInterface()) {
            throw new IllegalArgumentException( "Class " + cls +
                "is not an interface" ) ;
        }

        ExceptionWrapper ew = cls.getAnnotation( ExceptionWrapper.class ) ;
        final String idPrefix = ew.idPrefix() ;
        String str = ew.loggerName() ;
        if (str.length() == 0) {
            str = cls.getPackage().getName() ;
        }
        final String name = str ;

        InvocationHandler inh = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {

                final Annotation[][] pannos = method.getParameterAnnotations() ;
                final int chainIndex = findAnnotatedParameter( pannos,
                    Chain.class ) ;
                Throwable cause = null ;
                final Object[] messageParams = getWithSkip( args, chainIndex ) ;
                if (chainIndex >= 0) {
                    cause = (Throwable)args[chainIndex] ;
                }

                final Logger logger = Logger.getLogger( name ) ;
                final Class<?> rtype = method.getReturnType() ;
                final Log log = method.getAnnotation( Log.class ) ;

                if (log == null) {
                    if (!rtype.equals( String.class ) ) {
                        throw new IllegalArgumentException(
                            "No @Log annotation present on "
                            + cls.getName() + "." + method.getName() ) ;
                    }

                    return handleMessageOnly( method, logger, messageParams ) ;
                } else {
                    return handleFullLogging( log, method, logger, idPrefix,
                        messageParams, cause ) ;
                }
            }
        } ;

        // Load the Proxy using the same ClassLoader that loaded the interface
        ClassLoader loader = cls.getClassLoader() ;
        Class[] classes = { cls  } ;
        return (T)Proxy.newProxyInstance(loader, classes, inh ) ;
    }
}
