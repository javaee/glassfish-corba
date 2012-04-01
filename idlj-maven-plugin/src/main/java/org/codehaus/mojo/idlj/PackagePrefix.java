package org.codehaus.mojo.idlj;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file 
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY 
 * KIND, either express or implied.  See the License for the 
 * specific language governing permissions and limitations 
 * under the License.
 */

/**
 * @author Alan D. Cabrera <adc@apache.org>
 * @version $Revision: 9341 $ $Date: 2009-04-03 17:30:22 -0400 (Fri, 03 Apr 2009) $
 */
public class PackagePrefix
{
    /**
     * The simple name of either a top-level module, or an IDL type defined outside of any module
     * 
     * @parameter type
     */
    private String type;

    /**
     * The generated Java package name with <i>prefix</i> for all files generated for that type
     * 
     * @parameter prefix
     */
    private String prefix;

    /**
     * @return the name of either a top-level module or IDL type to match to use this prefix
     */
    public String getType()
    {
        return type;
    }

    /**
     * @return the package name that will applied to all the idl types that match the criteria
     */
    public String getPrefix()
    {
        return prefix;
    }
}
