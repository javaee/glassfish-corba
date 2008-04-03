/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2000-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package corba.framework.junitreport;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import java.util.Map;
import java.util.HashMap;
import java.util.Set ;
import java.util.HashSet ;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Date;
import java.util.TimeZone;

import java.text.SimpleDateFormat;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Prints XML output of the test to a specified Writer.
 *
 * @see FormatterElement
 */

public class XMLJUnitReportWriter implements JUnitReportWriter, XMLConstants {

    /** constant for unnnamed testsuites/cases */
    private static final String UNKNOWN = "unknown";

    private static DocumentBuilder getDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception exc) {
            throw new ExceptionInInitializerError(exc);
        }
    }

    /**
     * The XML document.
     */
    private Document doc;
    /**
     * The wrapper for the whole testsuite.
     */
    private Element rootElement;
    /**
     * Element for the current test.
     */
    private Map<TestDescription,Element> testElements = new HashMap<TestDescription,Element>();
    /**
     * tests that failed.
     */
    private Set<TestDescription> failedTests = new HashSet<TestDescription>() ;
    /**
     * Timing helper.
     */
    private Map<TestDescription,Long> testStarts = new HashMap<TestDescription,Long>();
    /**
     * Where to write the log to.
     */
    private OutputStream out;
    /*
     * Whether or not we should filter stack traces in the report.
     */
    private boolean filterTrace ;

    private int runCount ;
    private int failureCount ;
    private int errorCount ;
    private long startTime ;

    /** No arg constructor. */
    public XMLJUnitReportWriter() {
	this(true) ;
    }

    public XMLJUnitReportWriter( boolean filter ) {
	this.filterTrace = filter ;
    }

    public void setOutput(OutputStream out) {
        this.out = out;
    }

    public void setSystemOutput(String out) {
        formatOutput(SYSTEM_OUT, out);
    }

    public void setSystemError(String out) {
        formatOutput(SYSTEM_ERR, out);
    }

    public void startTestSuite(String name, Properties props ) {
        runCount = 0 ;
        failureCount = 0 ;
        errorCount = 0 ;
        startTime = System.currentTimeMillis() ;

        doc = getDocumentBuilder().newDocument();
        rootElement = doc.createElement(TESTSUITE);
        rootElement.setAttribute(ATTR_NAME, name == null ? UNKNOWN : name);

        //add the timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        sdf.setTimeZone(gmt);
        sdf.setLenient(true);
	final String timestamp = sdf.format( new Date() ) ;
        rootElement.setAttribute(TIMESTAMP, timestamp);
	
        //and the hostname.
        rootElement.setAttribute(HOSTNAME, getHostname());

        // Output properties
        Element propsElement = doc.createElement(PROPERTIES);
        rootElement.appendChild(propsElement);
        if (props != null) {
            Enumeration e = props.propertyNames();
            while (e.hasMoreElements()) {
                String str = (String) e.nextElement();
                Element propElement = doc.createElement(PROPERTY);
                propElement.setAttribute(ATTR_NAME, str);
                propElement.setAttribute(ATTR_VALUE, props.getProperty(str));
                propsElement.appendChild(propElement);
            }
        }
    }

    private String getHostname()  {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    public void endTestSuite( ) {
        long runTime = System.currentTimeMillis() - startTime ;
        rootElement.setAttribute(ATTR_TESTS, "" + runCount);
        rootElement.setAttribute(ATTR_FAILURES, "" + failureCount);
        rootElement.setAttribute(ATTR_ERRORS, "" + errorCount);
        rootElement.setAttribute(ATTR_TIME, "" + runTime / 1000.0);
        if (out == null) {
	    throw new RuntimeException( "Error: output file is null!" ) ;
        } else {
            Writer wri = null;
            try {
                wri = new BufferedWriter(new OutputStreamWriter(out, "UTF8"));
                wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
                (new DOMElementWriter()).write(rootElement, wri, 0, "  ");
                wri.flush();
            } catch (IOException exc) {
                throw new RuntimeException("Unable to write log file", exc);
            } finally {
                if (out != System.out && out != System.err) {
		    if (wri != null) {
			try {
			    wri.close() ;
			} catch (IOException ioexc) {
			    // ignore
			}
		    }
                }
            }
        }
    }

    public void startTest(TestDescription t) {
        testStarts.put(t, new Long(System.currentTimeMillis()));
    }

    public void endTest(TestDescription test) {
        runCount++ ;

        // Fix for bug #5637 - if a junit.extensions.TestSetup is
        // used and throws an exception during setUp then startTest
        // would never have been called
        if (!testStarts.containsKey(test)) {
            startTest(test);
        }

        Element currentTest = null;
        if (!failedTests.contains(test)) {
            currentTest = doc.createElement(TESTCASE);

            String n = test.getName();
            currentTest.setAttribute(ATTR_NAME, n == null ? UNKNOWN : n);
	    
            // a TestSuite can contain Tests from multiple classes,
            // even tests with the same name - disambiguate them.
            currentTest.setAttribute(ATTR_CLASSNAME, test.getClassName());

            rootElement.appendChild(currentTest);
            testElements.put(test, currentTest);
        } else {
            currentTest = (Element) testElements.get(test);
        }

        long l = testStarts.get(test);
        currentTest.setAttribute(ATTR_TIME, 
	    "" + ((System.currentTimeMillis() - l) / 1000.0));
    }

    public void addFailure(TestDescription test, Throwable t) {
        failureCount++ ;
        formatError(FAILURE, test, t);
    }

    public void addError(TestDescription test, Throwable t) {
        errorCount++ ;
        formatError(ERROR, test, t);
    }

    private void formatError(String type, TestDescription test, Throwable t) {
        if (test != null) {
            endTest(test);
            failedTests.add(test);
        }

        Element nested = doc.createElement(type);
        Element currentTest = null;
        if (test != null) {
            currentTest = (Element) testElements.get(test);
        } else {
            currentTest = rootElement;
        }

        currentTest.appendChild(nested);

        String message = t.getMessage();
        if (message != null && message.length() > 0) {
            nested.setAttribute(ATTR_MESSAGE, t.getMessage());
        }
        nested.setAttribute(ATTR_TYPE, t.getClass().getName());

        String strace = getFilteredTrace(t);
        Text trace = doc.createTextNode(strace);
        nested.appendChild(trace);
    }

    private void formatOutput(String type, String output) {
        Element nested = doc.createElement(type);
        rootElement.appendChild(nested);
        nested.appendChild(doc.createCDATASection(output));
    }

    private String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        pw.close();
        return sw.toString();
    }

    private String getFilteredTrace(Throwable t) {
        String trace = getStackTrace(t);
        return filterStack(trace);
    }

    private static final String[] DEFAULT_TRACE_FILTERS = new String[] {
                "junit.framework.TestCase",
                "junit.framework.TestResult",
                "junit.framework.TestSuite",
                "junit.framework.Assert.", // don't filter AssertionFailure
                "junit.swingui.TestRunner",
                "junit.awtui.TestRunner",
                "junit.textui.TestRunner",
                "java.lang.reflect.Method.invoke(",
                "sun.reflect.",
                "org.apache.tools.ant.",
                // JUnit 4 support:
                "org.junit.",
                "junit.framework.JUnit4TestAdapter",
                // See wrapListener for reason:
                "Caused by: java.lang.AssertionError",
                " more",
        };

    private String filterStack(String stack) {
        if (!filterTrace) {
            return stack;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        StringReader sr = new StringReader(stack);
        BufferedReader br = new BufferedReader(sr);

        String line;
        try {
            while ((line = br.readLine()) != null) {
                if (!filterLine(line)) {
                    pw.println(line);
                }
            }
        } catch (Exception e) {
            return stack; // return the stack unfiltered
        }
        return sw.toString();
    }

    private boolean filterLine(String line) {
        for (int i = 0; i < DEFAULT_TRACE_FILTERS.length; i++) {
            if (line.indexOf(DEFAULT_TRACE_FILTERS[i]) != -1) {
                return true;
            }
        }
        return false;
    }

} // XMLJUnitReportFormatter
