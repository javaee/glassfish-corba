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
/* @(#)DateImpl.java	1.1 99/06/07 */
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package javax.rmi.CORBA.serialization;

public class DateImpl extends Date 
{
    //nk
    private java.util.Date delegate = null;
    //nk

    public  DateImpl ()
    {
	//nk
	delegate = new java.util.Date();
	//nk
    }

    public  DateImpl (long arg0)
    {
	//nk
	delegate = new java.util.Date(arg0);	
	//nk
    }

    public  DateImpl (int arg0, int arg1, int arg2)
    {
	//nk
	delegate = new java.util.Date(arg0, arg1, arg2);
	//nk
    }

    public  DateImpl (int arg0, int arg1, int arg2, int arg3, int arg4)
    {
	//nk
	delegate = new java.util.Date(arg0, arg1, arg2, arg3, arg4);
	//nk
    }

    public  DateImpl (int arg0, int arg1, int arg2, int arg3, int arg4, int arg5)
    {
	//nk
	delegate = new java.util.Date(arg0, arg1, arg2, arg3, arg4, arg5);
	//nk
	
    }

    public  DateImpl (String arg0)
    {
	//nk
	delegate = new java.util.Date(arg0);	
	//nk
    }

    public long UTC (int arg0, int arg1, int arg2, int arg3, int arg4, int arg5)
    {
	//nk
	return delegate.UTC (arg0, arg1, arg2, arg3, arg4, arg5);
	//nk
    }

    public long parse (String arg0)
    {
	//nk
	return delegate.parse(arg0);
	//nk
    }

    public int year ()
    {
	//nk
	return delegate.getYear();
	//nk
    }

    public void year (int newYear)
    {
	//nk
	delegate.setYear(newYear);
	//nk
    }

    public int month ()
    {
	//nk
	return delegate.getMonth();
	//nk
    }

    public void month (int newMonth)
    {
	//nk
	delegate.setMonth(newMonth);
	//nk
    }

    public int date ()
    {
	//nk
	return delegate.getDate();
	//nk
    }

    public void date (int newDate)
    {
	//nk
	delegate.setDate(newDate);
	//nk
    }

    public int day ()
    {
	//nk
	return delegate.getDay();
	//nk
    }

    public int hours ()
    {
	//nk
	return delegate.getHours();
	//nk
	
    }

    public void hours (int newHours)
    {
	//nk
	delegate.setHours(newHours);
	//nk
    }

    public int minutes ()
    {
	//nk
	return delegate.getMinutes();
	//nk
    }

    public void minutes (int newMinutes)
    {
	//nk
	delegate.setMinutes(newMinutes);
	//nk
    }

    public int seconds ()
    {
	//nk
	return delegate.getSeconds();	
	//nk
    }

    public void seconds (int newSeconds)
    {
	//nk
	delegate.setSeconds(newSeconds);	
	//nk
    }

    public long time ()
    {
	//nk
	return delegate.getTime();
	//nk
    }

    public void time (long newTime)
    {
	//nk
	delegate.setTime(newTime);
	//nk
    }

    public boolean before (javax.rmi.CORBA.serialization.Date arg0)
    {
	//nk
	return delegate.before(((DateImpl)arg0).getDelegate());
	//nk
    }

    public boolean after (javax.rmi.CORBA.serialization.Date arg0)
    {
	//nk
	return delegate.after(((DateImpl)arg0).getDelegate());
	//nk
    }

    public boolean _equals (org.omg.CORBA.Any arg0)
    {
	//nk
	return false;
	//nk
    }

    public int _hashCode ()
    {
	//nk
	return delegate.hashCode();
	//nk	
    }

    public String _toString ()
    {
	//nk
	return delegate.toString();
	//nk
    }

    public String toLocaleString ()
    {
	//nk
	return delegate.toLocaleString();
	//nk
    }

    public String toGMTString ()
    {
	//nk
	return delegate.toGMTString();
	//nk
    }

    public int timezoneOffset ()
    {
	//nk
	return delegate.getTimezoneOffset();
	//nk
	
    }

    //nk
    public void setDelegate (java.util.Date delegate)
    {
	this.delegate = delegate;
    }

    public java.util.Date getDelegate() 
    {
	return delegate;
    }	
    //nk
	
  //nk
  //Methods to be implemented for Custom Marshalling
    public void marshal(org.omg.CORBA.DataOutputStream os)
    {
	os.write_octet((byte)1);
	os.write_boolean(false);
	os.write_longlong(delegate.getTime());
    }

    public void unmarshal(org.omg.CORBA.DataInputStream is)
    {
	is.read_octet();
	is.read_boolean();
	delegate = new java.util.Date(is.read_longlong());
    }
    //nk
} // class DateImpl
