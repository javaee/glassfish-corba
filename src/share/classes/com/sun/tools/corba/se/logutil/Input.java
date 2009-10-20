/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.tools.corba.se.logutil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.LinkedList;
import java.util.Queue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Input {

  /**
   * The name of the package this class will inhabit.
   */
  private String packageName;

  /**
   * The name of the generated class.
   */
  private String className;

  /**
   * The name of the group of exceptions handled by the class.
   */
  private String groupName;

  /**
   * The group of exceptions.
   */
  private Queue<InputException> exceptions;

  /**
   * Represents the current state of parsing the input.
   */
  private enum State
  {
    OUTER,
    IN_CLASS,
    IN_EXCEPTION_LIST
  };

  /**
   * Regular expression to match each code line.
   */
  private static final Pattern EXCEPTION_INFO_REGEX =
    Pattern.compile("(\\w+)\\s*(\\d+)\\s*(\\w+)");

  /**
   * Parses the specified file to create a new {@link Input}
   * object.
   *
   * @param filename the file to parse.
   * @throws FileNotFoundException if the file can't be found.
   * @throws IOException if an I/O error occurs.
   */
  public Input(final String filename)
  throws FileNotFoundException, IOException {
    BufferedReader r =
      new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
    State state = State.OUTER;
    InputException current = null;
    exceptions = new LinkedList<InputException>();
    String line;
    while ((line = r.readLine()) != null) {
      // Skip ; comments
      if (line.startsWith(";"))
        continue;

      int index = line.indexOf("(");
      if (index == -1)
        continue;

      switch (state) {
      case OUTER:
        state = State.IN_CLASS;
        String[] classInfo = line.substring(index).split(" ");
        packageName = classInfo[0].substring(2, classInfo[0].length() - 1);
        className = classInfo[1].substring(1, classInfo[1].length() - 1);
        groupName = classInfo[2];
        break;
      case IN_CLASS:
        state = State.IN_EXCEPTION_LIST;
        break;
      case IN_EXCEPTION_LIST:
        boolean inQuote = false;
        boolean inCode = false;
        boolean end = false;
        int start = index + 1;
        Queue<String> lines = new LinkedList<String>();
        for (int a = start; a < line.length(); ++a) {
          if (line.charAt(a) == '(' && !inCode && !inQuote) {
            if (current == null)
              current =
                new InputException(line.substring(start, a).trim());
            start = a + 1;
            inCode = true;
          }
          if (line.charAt(a) == '"')
            inQuote = !inQuote;
          if (line.charAt(a) == ')' && !inQuote) {
            if (inCode) {
              lines.offer(line.substring(start, a));
              inCode = false;
            } else
              end = true;
          }
          if (!end && a == line.length() - 1)
            line += r.readLine();
        }
        for (String l : lines) {
          int stringStart = l.indexOf("\"") + 1;
          int stringEnd = l.indexOf("\"", stringStart);
          Matcher matcher = EXCEPTION_INFO_REGEX.matcher(l.substring(0, stringStart));
          if (matcher.find())
            current.add(new InputCode(matcher.group(1),
                                      Integer.parseInt(matcher.group(2)),
                                      matcher.group(3),
                                      l.substring(stringStart, stringEnd)));
        }
        exceptions.offer(current);
        current = null;
        break;
      }
    }
  }

  /**
   * Returns the name of this group of exceptions.
   *
   * @return the name of this group of exceptions.
   */
  public String getGroupName()
  {
    return groupName;
  }

  /**
   * Returns the name of the package this class will go in.
   *
   * @return the name of the package.
   */
  public String getPackageName()
  {
    return packageName;
  }

  /**
   * Returns the name of the generated class.
   *
   * @return the name of the class.
   */
  public String getClassName()
  {
    return className;
  }

  /**
   * Returns the exceptions contained in this class.
   *
   * @return the exceptions.
   */
  public Queue<InputException> getExceptions() {
    return exceptions;
  }

  /**
   * Returns a textual representation of this input.
   *
   * @return a textual representation.
   */
  public String toString() {
    return getClass().getName() +
      "[packageName=" + packageName +
      ",className=" + className +
      ",groupName=" + groupName +
      ",exceptions=" + exceptions +
      "]";
  }

}
