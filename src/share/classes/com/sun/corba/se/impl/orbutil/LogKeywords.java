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
package com.sun.corba.se.impl.orbutil;
/**
 * All the Keywords that will be used in Logging Messages for CORBA need to
 * be defined here. The LogKeywords will be useful for searching log messages
 * based on the standard keywords, it is also useful to work with LogAnalyzing
 * tools.
 * We will try to standardize these keywords in JSR 117 Logging
 */
public class LogKeywords {

    /** 
     ** Keywords for Lifecycle Loggers. 
     ** _REVISIT_ After it is clearly defined in JSR 117
     **/
    public final static String LIFECYCLE_CREATE     = "<<LIFECYCLE CREATE>>";
    public final static String LIFECYCLE_INITIALIZE = "<<LIFECYCLE INITIALIZE>>";
    public final static String LIFECYCLE_SHUTDOWN   = "<<LIFECYCLE SHUTDOWN>>";
    public final static String LIFECYCLE_DESTROY    = "<<LIFECYCLE DESTROY>>";


    public final static String LIFECYCLE_CREATE_SUCCESS = 
        LIFECYCLE_CREATE + "<<SUCCESS>>";
    public final static String LIFECYCLE_CREATE_FAILURE = 
        LIFECYCLE_CREATE + "<<FAILURE>>";
    public final static String LIFECYCLE_INITIALIZE_SUCCESS = 
        LIFECYCLE_INITIALIZE + "<<SUCCESS>>";
    public final static String LIFECYCLE_INITIALIZE_FAILURE = 
        LIFECYCLE_INITIALIZE + "<<FAILURE>>";
    public final static String LIFECYCLE_SHUTDOWN_SUCCESS = 
        LIFECYCLE_SHUTDOWN + "<<SUCCESS>>";
    public final static String LIFECYCLE_SHUTDOWN_FAILURE = 
        LIFECYCLE_SHUTDOWN + "<<FAILURE>>";
    public final static String LIFECYCLE_DESTROY_SUCCESS = 
        LIFECYCLE_DESTROY + "<<SUCCESS>>";
    public final static String LIFECYCLE_DESTROY_FAILURE = 
        LIFECYCLE_DESTROY + "<<FAILURE>>";

    /**
     ** Keywords for Naming Read Loggers.
     **/ 
    public final static String NAMING_RESOLVE       = "<<NAMING RESOLVE>>";
    public final static String NAMING_LIST          = "<<NAMING LIST>>";

    public final static String NAMING_RESOLVE_SUCCESS =
        NAMING_RESOLVE + "<<SUCCESS>>";
    public final static String NAMING_RESOLVE_FAILURE =
        NAMING_RESOLVE + "<<FAILURE>>";
    public final static String NAMING_LIST_SUCCESS =
        NAMING_LIST + "<<SUCCESS>>";
    public final static String NAMING_LIST_FAILURE =
        NAMING_LIST + "<<FAILURE>>";

    /**
     ** Keywords for Naming Update Loggers.
     **/
    public final static String NAMING_BIND          = "<<NAMING BIND>>";
    public final static String NAMING_UNBIND        = "<<NAMING UNBIND>>";
    public final static String NAMING_REBIND        = "<<NAMING REBIND>>";

    public final static String NAMING_BIND_SUCCESS =
        NAMING_BIND + "<<SUCCESS>>";
    public final static String NAMING_BIND_FAILURE =
        NAMING_BIND + "<<FAILURE>>";
    public final static String NAMING_UNBIND_SUCCESS =
        NAMING_UNBIND + "<<SUCCESS>>";
    public final static String NAMING_UNBIND_FAILURE =
        NAMING_UNBIND + "<<FAILURE>>";
    public final static String NAMING_REBIND_SUCCESS =
        NAMING_REBIND + "<<SUCCESS>>";
    public final static String NAMING_REBIND_FAILURE =
        NAMING_REBIND + "<<FAILURE>>";
}

    
    

     

