/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

package hopper.h4486041;

import org.omg.CORBA.*;
import org.omg.CORBA.portable.*;
import org.omg.CORBA.ORBPackage.InvalidName;
import java.util.Properties;
import java.applet.Applet;

public class TestORB
    extends
        ORB
{
    public static final String ThrowError = "ThrowError";

    protected void set_parameters(String[] args, Properties props)
    {
        System.out.println("\tTestORB.set_parameters - Enter");
        if (props.get(ThrowError) != null) {
            System.out.println("\tTestORB.set_parameters - Exception Exit");
            throw new ORBInitException("You asked for it.");
        }
        System.out.println("\tTestORB.set_parameters - Normal Exit");
    }
    protected void set_parameters(Applet app, Properties props)
    {
    }
    public String[] list_initial_services()
    {
        return null;
    }
    public org.omg.CORBA.Object resolve_initial_references(String object_name)
        throws InvalidName
    {
        return null;
    }
    public String object_to_string(org.omg.CORBA.Object obj)
    {
        return null;
    }
    public org.omg.CORBA.Object string_to_object(String str)
    {
        return null;
    }
    public NVList create_list(int count)
    {
        return null;
    }
    public NamedValue create_named_value(String s, Any any, int flags)
    {
        return null;
    }
    public ExceptionList create_exception_list()
    {
        return null;
    }
    public ContextList create_context_list()
    {
        return null;
    }
    public Context get_default_context()
    {
        return null;
    }
    public Environment create_environment()
    {
        return null;
    }
    public org.omg.CORBA.portable.OutputStream create_output_stream()
    {
        return null;
    }
    public void send_multiple_requests_oneway(Request[] req)
    {
    }
    public void send_multiple_requests_deferred(Request[] req)
    {
    }
    public boolean poll_next_response()
    {
        return false;
    }
    public Request get_next_response() throws WrongTransaction
    {
        return null;
    }
    public TypeCode get_primitive_tc(TCKind tcKind)
    {
        return null;
    }
    public TypeCode create_struct_tc(String id, String name,
                                     StructMember[] members)
    {
        return null;
    }
    public TypeCode create_union_tc(String id, String name,
                                    TypeCode discriminator_type,
                                    UnionMember[] members)
    {
        return null;
    }
    public TypeCode create_enum_tc(String id, String name, String[] members)
    {
        return null;
    }
    public TypeCode create_alias_tc(String id, String name,
                                    TypeCode original_type)
    {
        return null;
    }
    public TypeCode create_exception_tc(String id, String name,
                                        StructMember[] members)
    {
        return null;
    }
    public TypeCode create_interface_tc(String id, String name)
    {
        return null;
    }
    public TypeCode create_string_tc(int bound)
    {
        return null;
    }
    public TypeCode create_wstring_tc(int bound)
    {
        return null;
    }
    public TypeCode create_sequence_tc(int bound, TypeCode element_type)
    {
        return null;
    }
    public TypeCode create_recursive_sequence_tc(int bound, int offset)
    {
        return null;
    }
    public TypeCode create_array_tc(int length, TypeCode element_type)
    {
        return null;
    }
    public Any create_any()
    {
        return null;
    }
}

// End of file.

