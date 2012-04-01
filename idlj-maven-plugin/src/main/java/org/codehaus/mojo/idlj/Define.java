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
 * This class is used to rappresent the definition of a <b>define</b>
 * 
 * @author Anders Hessellund Jensen <ahj@trifork.com>
 * @version $Id: Define.java 9341 2009-04-03 21:30:22Z aheritier $
 */
public class Define
{
    /**
     * The symbol to define
     * 
     * @parameter symbol
     */
    private String symbol;

    /**
     * The value of the symbol. This is optional.
     * 
     * @parameter value
     */
    private String value;

    /**
     * @return The name of the symbol defined
     */
    public String getSymbol()
    {
        return symbol;
    }

    /**
     * @return The value of the symbol defined
     */
    public String getValue()
    {
        return value;
    }
}
