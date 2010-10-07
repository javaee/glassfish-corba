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

package com.sun.corba.se.impl.dynamicany;

import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.portable.OutputStream;

import org.omg.DynamicAny.*;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;

import com.sun.corba.se.spi.orbutil.ORBConstants ;

import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.impl.logging.ORBUtilSystemException ;

abstract class DynAnyImpl extends org.omg.CORBA.LocalObject implements DynAny
{
    private static final long serialVersionUID = 7435214669604617358L;

    protected static final int NO_INDEX = -1;
    // A DynAny is destroyable if it is the root of a DynAny hierarchy.
    protected static final byte STATUS_DESTROYABLE = 0;
    // A DynAny is undestroyable if it is a node in a DynAny hierarchy other than the root.
    protected static final byte STATUS_UNDESTROYABLE = 1;
    // A DynAny is destroyed if its root has been destroyed.
    protected static final byte STATUS_DESTROYED = 2;

    //
    // Instance variables
    //

    protected ORB orb = null;
    protected ORBUtilSystemException wrapper ;

    // An Any is used internally to implement the basic DynAny.
    // It stores the DynAnys TypeCode.
    // For primitive types it is the only representation.
    // For complex types it is the streamed representation.
    protected Any any = null;
    // Destroyable is the default status for free standing DynAnys.
    protected byte status = STATUS_DESTROYABLE;
    protected int index = NO_INDEX;

    //
    // Constructors
    //

    protected DynAnyImpl() {
	wrapper = ORB.getStaticLogWrapperTable().get_RPC_PRESENTATION_ORBUtil() ;
    }

    protected DynAnyImpl(ORB orb, Any any, boolean copyValue) {
        this.orb = orb;
	wrapper = orb.getLogWrapperTable().get_RPC_PRESENTATION_ORBUtil() ;

        if (copyValue) {
            this.any = DynAnyUtil.copy(any, orb);
        } else {
            this.any = any;
        }
        // set the current position to 0 if any has components, otherwise to -1.
        index = NO_INDEX;
    }

    protected DynAnyImpl(ORB orb, TypeCode typeCode) {
        this.orb = orb;
	wrapper = orb.getLogWrapperTable().get_RPC_PRESENTATION_ORBUtil() ;

        this.any = DynAnyUtil.createDefaultAnyOfType(typeCode, orb);
    }

    protected DynAnyFactory factory() {
        try {
            return (DynAnyFactory)orb.resolve_initial_references( 
		ORBConstants.DYN_ANY_FACTORY_NAME );
        } catch (InvalidName in) {
            throw new RuntimeException("Unable to find DynAnyFactory");
        }
    }

    protected Any getAny() {
        return any;
    }

    // Uses getAny() if this is our implementation, otherwise uses to_any()
    // which copies the Any.
    protected Any getAny(DynAny dynAny) {
        if (dynAny instanceof DynAnyImpl) {
            return ((DynAnyImpl)dynAny).getAny();
        } else {
            return dynAny.to_any();
        }
    }

    protected void writeAny(OutputStream out) {
        //System.out.println(this + " writeAny of type " + type().kind().value());
        any.write_value(out);
    }

    protected void setStatus(byte newStatus) {
        status = newStatus;
    }

    protected void clearData() {
        // This clears the data part of the Any while keeping the TypeCode info.
        any.type(any.type());
    }

    //
    // DynAny interface methods
    //

    public org.omg.CORBA.TypeCode type() {
        if (status == STATUS_DESTROYED) {
	    throw wrapper.dynAnyDestroyed() ;
        }
        return any.type();
    }

    // Makes a copy of the Any value inside the parameter
    public void assign (org.omg.DynamicAny.DynAny dyn_any)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if (status == STATUS_DESTROYED) {
	    throw wrapper.dynAnyDestroyed() ;
        }
        if ((any != null) && (! any.type().equal(dyn_any.type()))) {
            throw new TypeMismatch();
        }
        any = dyn_any.to_any();
    }

    // Makes a copy of the Any parameter
    public void from_any (org.omg.CORBA.Any value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
	    throw wrapper.dynAnyDestroyed() ;
        }
        if ((any != null) && (! any.type().equal(value.type()))) {
            throw new TypeMismatch();
        }
        // If the passed Any does not contain a legal value
        // (such as a null string), the operation raises InvalidValue.
        Any tempAny = null;
        try {
            tempAny = DynAnyUtil.copy(value, orb);
        } catch (Exception e) {
            throw new InvalidValue();
        }
        if ( ! DynAnyUtil.isInitialized(tempAny)) {
            throw new InvalidValue();
        }
        any = tempAny;
   }

    public abstract org.omg.CORBA.Any to_any();
    public abstract boolean equal (org.omg.DynamicAny.DynAny dyn_any);
    public abstract void destroy();
    public abstract org.omg.DynamicAny.DynAny copy();

    // Needed for org.omg.CORBA.Object

    private String[] __ids = { "IDL:omg.org/DynamicAny/DynAny:1.0" };

    public String[] _ids() {
        return __ids;
    }
}
