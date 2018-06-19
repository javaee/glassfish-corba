/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

// Stub class generated by rmic, do not edit.
// Contents subject to change without notice.

package org.glassfish.rmic.classes.preinvokepostinvoke;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnexpectedException;
import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Util;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.CORBA.portable.ServantObject;


public class _Interface_Stub extends Stub implements Interface {
    
    private static final String[] _type_ids = {
        "RMI:org.glassfish.rmic.classes.preinvokepostinvoke.Interface:0000000000000000"
    };
    
        public String[] _ids() { 
            return (String[]) _type_ids.clone();
        }
        
        public String o1(String arg0) throws java.rmi.RemoteException {
            if (!Util.isLocal(this)) {
                try {
                    org.omg.CORBA_2_3.portable.InputStream in = null;
                    try {
                        org.omg.CORBA_2_3.portable.OutputStream out = 
                            (org.omg.CORBA_2_3.portable.OutputStream)
                            _request("o1", true);
                        out.write_value(arg0,String.class);
                        in = (org.omg.CORBA_2_3.portable.InputStream)_invoke(out);
                        return (String) in.read_value(String.class);
                    } catch (ApplicationException ex) {
                        in = (org.omg.CORBA_2_3.portable.InputStream) ex.getInputStream();
                        String $_id = in.read_string();
                        throw new UnexpectedException($_id);
                    } catch (RemarshalException ex) {
                        return o1(arg0);
                    } finally {
                        _releaseReply(in);
                    }
                } catch (SystemException ex) {
                    throw Util.mapSystemException(ex);
                }
            } else {
                ServantObject so = _servant_preinvoke("o1",Interface.class);
                if (so == null) {
                    return o1(arg0);
                }
                try {
                    return ((Interface)so.servant).o1(arg0);
                } catch (Throwable ex) {
                    Throwable exCopy = (Throwable)Util.copyObject(ex,_orb());
                    throw Util.wrapException(exCopy);
                } finally {
                    _servant_postinvoke(so);
                }
            }
        }
        
        public String o2(String arg0) throws java.rmi.RemoteException {
            if (!Util.isLocal(this)) {
                try {
                    org.omg.CORBA_2_3.portable.InputStream in = null;
                    try {
                        org.omg.CORBA_2_3.portable.OutputStream out = 
                            (org.omg.CORBA_2_3.portable.OutputStream)
                            _request("o2", true);
                        out.write_value(arg0,String.class);
                        in = (org.omg.CORBA_2_3.portable.InputStream)_invoke(out);
                        return (String) in.read_value(String.class);
                    } catch (ApplicationException ex) {
                        in = (org.omg.CORBA_2_3.portable.InputStream) ex.getInputStream();
                        String $_id = in.read_string();
                        throw new UnexpectedException($_id);
                    } catch (RemarshalException ex) {
                        return o2(arg0);
                    } finally {
                        _releaseReply(in);
                    }
                } catch (SystemException ex) {
                    throw Util.mapSystemException(ex);
                }
            } else {
                ServantObject so = _servant_preinvoke("o2",Interface.class);
                if (so == null) {
                    return o2(arg0);
                }
                try {
                    return ((Interface)so.servant).o2(arg0);
                } catch (Throwable ex) {
                    Throwable exCopy = (Throwable)Util.copyObject(ex,_orb());
                    throw Util.wrapException(exCopy);
                } finally {
                    _servant_postinvoke(so);
                }
            }
        }
    }
