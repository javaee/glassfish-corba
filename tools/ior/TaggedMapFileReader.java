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
package tools.ior;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import org.omg.IOP.*;
import org.omg.IOP.CodecFactoryPackage.*;

/**
 * A tagged map file is currently defined as follows:
 *
 * Space delimited entries, one per line, in the following
 * format:
 *
 * <tag number> <class name> [<encoding>] 
 *
 * <class name> is either the name of an EncapsHandler,
 * or the name of an IDL Helper class.  If you have a Helper
 * for a certain tagged component or profile, you don't need
 * to write your own EncapsHandler.
 *
 * If the class is a Helper, you can optionally specify
 * what encoding to use.  If you provide your own handler,
 * you are responsible for using the CodecFactory to
 * create the correct codec.
 *
 * If <encoding> is unspecified, it defaults to the GIOP 1.0
 * CDR encapsulation encoding.  Otherwise, use this format:
 *
 * ENCODING_CDR_ENCAPS <major> <minor>
 *              or
 * <short format> <major> <minor>
 *
 * Where <short format> is the number to be used in an
 * org.omg.IOP.Encoding.
 *
 * Any lines starting with double slashes are ignored.
 */
public class TaggedMapFileReader
{
    private static final Encoding DEFAULT_ENCODING
        = new Encoding(ENCODING_CDR_ENCAPS.value, (byte)1, (byte)0);

    /**
     * See above for how to optionally specify an encoding
     * for use with helper classes.
     */
    private Encoding parseEncodingForHelper(StringTokenizer strTok) {
            
        Encoding encoding = DEFAULT_ENCODING;

        if (strTok.hasMoreTokens()) {
            
            String encodingStr = strTok.nextToken();
            String majorStr = strTok.nextToken();
            String minorStr = strTok.nextToken();

            short encodingNum;

            if (encodingStr.equals("ENCODING_CDR_ENCAPS"))
                encodingNum = ENCODING_CDR_ENCAPS.value;
            else
                encodingNum = Short.parseShort(encodingStr);
            
            encoding = new Encoding(encodingNum,
                                    Byte.parseByte(majorStr),
                                    Byte.parseByte(minorStr));
        }

        return encoding;
    }

    /**
     * Create a TagHelperHandler which will delegate to the
     * given helper class, and unmarshal with a Codec of
     * the specified encoding.
     */
    private EncapsHandler createTagHelperHandler(String helperClassName,
                                                 Encoding encoding,
                                                 Utility util)
        throws ClassNotFoundException, 
               IllegalAccessException,
               IllegalArgumentException,
               InvocationTargetException,
               NoSuchMethodException,
               UnknownEncoding,
               SecurityException {

        Codec codec = util.getCodecFactory().create_codec(encoding);

        return new TagHelperHandler(helperClassName, codec);
    }

    /**
     * Parse a line of text, create the appropriate
     * EncapsHandler, and add it to the Map.
     */
    private void parseLine(String fullLine,
                           Map map,
                           Utility util) {
        
        StringTokenizer strTok 
            = new StringTokenizer(fullLine);

        String number = strTok.nextToken();
        
        // Allow comment lines
        if (number.startsWith("//"))
            return;

        Integer tag = Integer.valueOf(number);

        String className = strTok.nextToken();
        
        try {

            EncapsHandler handler;

            if (className.endsWith("Helper")) {
                handler 
                    = createTagHelperHandler(className,
                                             parseEncodingForHelper(strTok),
                                             util);
            } else {
                handler = (EncapsHandler)Class.forName(className).newInstance();
            }

            map.put(tag, handler);

        } catch (Exception ex) {
            System.out.println("Error parsing line: " + fullLine);
            ex.printStackTrace();
        }
    }

    /**
     * Read the given file, creating EncapsHandlers for each
     * valid line of input.
     */
    public void readMapFile(String fileName, Map map, Utility util)
        throws IOException {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);

            BufferedReader reader
                = new BufferedReader(new InputStreamReader(fis));

            do {

                String input = reader.readLine();
                if (input == null)
                    break;

                // Skip blank lines
                if (input.length() == 0)
                    continue;

                parseLine(input, map, util);

            } while (true);

        } catch (FileNotFoundException fnfe) {
            // Silent, non-fatal
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ioe) {}
            }
        }
    }
}

