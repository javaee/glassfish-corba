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

package corba.framework;

import java.io.*;
import java.util.*;
import test.*;

/**
 * Collection of utility methods used by CORBATest and its helpers.
 */
public class CORBAUtil
{
    /**
     * Create the specified directory, including any intermediate directories
     * that do not exist.
     *
     *@return true for success, false for failure
     */
    public static boolean mkdir(String name)
    {
        File dir = new File(name);
        return dir.mkdirs();
    }

    /**
     * Open/create a file with the given name in the specified directory.
     * Calls openNewFile(String) with a full path created by combining
     * dir and name with the File.separator.  This also ensures that
     * the directory exists first.
     *
     *@param   dir    Directory of the file
     *@param   name   Name of the file
     *@return  OutputStream for writing to the file
     *@exception  FileNotFoundException  Error creating the file
     */
    public static OutputStream openFile(String dir, 
                                        String name) throws FileNotFoundException
    {
        CORBAUtil.mkdir(dir);
        
        return CORBAUtil.openFile(dir + File.separator + name);
    }
    
    /**
     * Open/create a file at the given full path, appending to it if it is 
     * already there.
     *
     *@param fullpath   Path and name of the file
     *@return  OutputStream for writing to the file
     *@exception  FileNotFoundException   Error creating the file
     */
    public static OutputStream openFile(String fullpath) 
        throws FileNotFoundException
    {
        return new BufferedOutputStream(new FileOutputStream(fullpath, true));
    }
    
    
    /**
     * Convert the given Vector to a String array.  Returns null if the given
     * Vector is null.
     *
     *@param v    Vector of Strings
     *@return     Array of Strings copied from the Vector
     */
    public static String[] toArray(Vector v)
    {
        if (v == null)
            return null;

        String[] result = new String[v.size()];

        v.copyInto(result);

        return result;
    }

    /**
     * Return the package name of the given object.  Sometimes 
     * getPackage() returns null, but Class getName() always returns
     * a fully qualified package and class name.
     *
     *@param   obj   Object to examine (can't be null)
     *@return  String representing the package name of the object or
     *         the empty string if it couldn't be determined/top level
     *         package
     */
    public static String getPackageName(Object obj)
    {
        String name = obj.getClass().getName();

        int lastPeriod = name.lastIndexOf('.');

        // Note: returns empty string rather than null
        if (lastPeriod == -1)
            return "";

        return name.substring(0, lastPeriod);
    }

    /**
     * Combine to string arrays. Returns null if both arguments are null
     *
     *@param array1  First array, can be null
     *@param array2  Second array, can be null
     *
     *@return  Array with all the elements of the previous two
     */
    public static String[] combine(String[] array1,
                                   String[] array2)
    {
        if (array1 == null || array1.length == 0)
            return array2;
        else
            if (array2 == null || array2.length == 0)
                return array1;

        String[] result = new String[array1.length + array2.length];

        System.arraycopy(array1, 0,
                         result, 0,
                         array1.length);
        System.arraycopy(array2, 0,
                         result, array1.length,
                         array2.length);

        return result;
    }

    /**
     * Determines if the given Process has stopped executing.
     *
     *@param   p    Process to examine
     *@return  true if it has stopped, otherwise false
     */
    public static boolean processFinished(Process p)
    {
        try {
            p.exitValue();
            return true;
        } catch (IllegalThreadStateException e) {
            return false;
        }
    }

    /**
     * Find the given file name in one of the provided directories, and
     * return the resulting absolute path of the result.  Neither the
     * filename nor the directory array can be null.
     *
     *@param filename   Name of the file to find
     *@param dirs       Array of directories to examine
     *
     *@return An absolute path represented as a String
     *
     *@exception FileNotFoundException  Couldn't find the file in provided
     *                                  locations
     */
    public static String toAbsolutePath(String filename,
                                        String[] dirs) throws FileNotFoundException
    {
        filename = filename.replace('/', File.separatorChar);
        String result = null;

        for (int i = 0; i < dirs.length; i++) {
            File file = new File(dirs[i]
                                 + File.separator
                                 + filename);

            if (file.exists())
                result = file.getAbsolutePath();
        }

        // Check the current directory
        if (result == null) {
            File file = new File(filename);

            if (file.exists())
                result = file.getAbsolutePath();
        }

        if (result == null) 
            throw new FileNotFoundException(filename);

        return result;
    }

    /**
     * Find the given files in the given set of directories, and convert
     * them to absolute paths.  Neither files nor dirs can be null.
     *
     *@param files  Array of file names to convert to absolute paths
     *@param dirs   Directories to search in
     *
     *@exception FileNotFoundException  At least one requested file
     *                                  couldn't be found in the directories
     *
     */
    public static void toAbsolutePaths(String[] files,
                                       String[] dirs) 
        throws FileNotFoundException
    {
        // No null check
        for (int i = 0; i < files.length; i++) 
            files[i] = CORBAUtil.toAbsolutePath(files[i],
                                                dirs);
    }

    /**
     * Compare the two files, throwing an exception if there was an error
     * or a difference between them.
     *
     *@param filePath1  Complete path to file number one
     *@param filePath2  Complete path to file number two
     *
     *@exception  Exception  Detailed exception describing the first difference
     */
    public static void fileCompare(String filePath1,
                                   String filePath2) throws Exception
    {
        FileReader fr1 = null, fr2 = null;

        File f1 = new File(filePath1);
        if (!f1.exists())
            throw new Exception(f1.getAbsolutePath() + " does not exist!");
        File f2 = new File(filePath2);
        if (!f2.exists())
            throw new Exception(f2.getAbsolutePath() + " does not exist!");

        try {

            fr1 = new FileReader(f1);
            fr2 = new FileReader(f2);
            
            LineNumberReader file1 = new LineNumberReader(fr1);
            LineNumberReader file2 = new LineNumberReader(fr2);
            
            String file1line, file2line;

            do {

                file1line = file1.readLine();
                file2line = file2.readLine();
                
                if (file1line == null && file2line == null)
                    continue;

                if (file1line == null && file2line != null)
                    throw new Exception(filePath1 
                                        + " is shorter than " + filePath2);
                
                if (file2line == null)
                    throw new Exception(filePath2
                                        + " is shorter than " + filePath1);
            
                if (!file1line.equals(file2line))
                    throw new Exception(filePath1 + " and " + filePath2
                                        + " differ on at least line: "
                                        + file1.getLineNumber());
                
            } while (file1line != null && file2line != null);

        } finally {
            if (fr1 != null)
                fr1.close();
            if (fr2 != null)
                fr2.close();
        }
    }
}
