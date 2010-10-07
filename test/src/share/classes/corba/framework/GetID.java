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

package corba.framework;

/**
 * Generates unique test-ids 
 *
 * The GetID class generates a test-ID for identifying tests in the 
 * log files. The ID is generated by replacing the '.'s in the
 * full-qualified package name by '_'s, and then appending a user-specified
 * string Id. Since, the fully qualified package name is unique across a 
 * test-suite, this results in unique IDs.
 *
 * @author Nandkumar Kesavan (nanduk@eng.sun.com)
 * @version 1.1, 04/04/01
 */
public class GetID {
 
   /**
    * Generates a unique ID for a test from the package name and a 
    * user-specified id.
    *
    * @param  o   an instance of the test class
    * @param  id  a user-specified id to be appended 
    *             to the id generated by this method.
    *             <em>Note: Usually, one would specify a string,</em>
    *             <em>representive of the test case. </em>
    * @return     a unique string ID for the test 
    */
   public static String generateID(Object o, String id)
   {
      Package p = o.getClass().getPackage();
      String packageName = p.getName();
     
      //Substitute all .'s with _'s. Since . are illegal in 
      //class or package names, they wouldn't occur anywhere
      //other than as package separators. 
      String ID = packageName.replace('.','_');

      //Append the non-empty user id
	  if (!id.equals(""))
      	ID = ID + "_" + id;
      return ID; 
   }

   /**
    * Generates a unique ID for a test from the class name.
    *
    * @param  o   an instance of the test class
    * @return     a unique string ID for the test 
    */
   public static String generateID(Object o)
   {
      Class c = o.getClass();
      String className = c.getName();

      //Substitute all .'s with _'s. Since . are illegal in
      //class or package names, they wouldn't occur anywhere
      //other than as package separators.
      String ID = className.replace('.','_');
      return ID;
   }
}
