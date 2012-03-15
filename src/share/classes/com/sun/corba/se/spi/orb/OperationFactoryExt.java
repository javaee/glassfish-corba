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
package com.sun.corba.ee.spi.orb ;

import java.lang.reflect.Constructor ;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;

/** Provides an extension to the OperationFactory for convertAction( Class ),
 * which takes a Class with a constructor that takes a String as an argument.
 * It uses the constructor to create an instance of the Class from its argument.
 * <p> 
 * This is split off here to avoid problems with the build depending on the
 * version of OperationFactory that is in Java SE 5.0.
 */
public class OperationFactoryExt {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private OperationFactoryExt() {} 

    private static class ConvertAction implements Operation {
        private Class<?> cls ;
        private Constructor<?> cons ;

        public ConvertAction( Class<?> cls ) {
            this.cls = cls ;
            try {
                cons = cls.getConstructor( String.class ) ;
            } catch (Exception exc) {
                throw wrapper.exceptionInConvertActionConstructor( exc,
                    cls.getName() ) ;
            }
        }

        public Object operate( Object value )
        {
            try {
                return cons.newInstance( value ) ;
            } catch (Exception exc) {
                throw wrapper.exceptionInConvertAction( exc ) ;
            }
        }

        @Override
        public String toString() {
            return "ConvertAction[" + cls.getName() + "]" ;
        }

        @Override
        public boolean equals( Object obj ) 
        {
            if (this==obj) {
                return true;
            }

            if (!(obj instanceof ConvertAction)) {
                return false;
            }

            ConvertAction other = (ConvertAction)obj ;

            return toString().equals( other.toString() ) ;
        }

        @Override
        public int hashCode()
        {
            return toString().hashCode() ;
        }
    }

    public static Operation convertAction( Class<?> cls ) {
        return new ConvertAction( cls ) ;
    }
}
