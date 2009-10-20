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

public class InputCode {

  /**
   * The name of this code.
   */
  private final String name;

  /**
   * The code.
   */
  private final int code;

  /**
   * The log level for this code.
   */
  private final String logLevel;

  /**
   * The error message for this code.
   */
  private final String message;

  /**
   * Creates a new error code with the specified name, code,
   * log level and error message.
   *
   * @param name the name of the new code.
   * @param code the code itself.
   * @param logLevel the level of severity of this error.
   * @param message the error message for this code.
   */
  public InputCode(final String name, final int code,
                   final String logLevel, final String message) {
    this.name = name;
    this.code = code;
    this.logLevel = logLevel;
    this.message = message;
  }

  /**
   * Returns the name of this code.
   *
   * @return the name of the code.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the code.
   *
   * @return the code.
   */
  public int getCode() {
    return code;
  }

  /**
   * Returns the severity of this code.
   *
   * @return the log level severity of the code.
   */
  public String getLogLevel() {
    return logLevel;
  }

  /**
   * Returns the error message for this code.
   *
   * @return the error message for this code.
   */
  public String getMessage() {
    return message;
  }

  /**
   * Returns a textual representation of this code.
   *
   * @return a textual representation.
   */
  public String toString() {
    return getClass().getName() +
      "[name=" + name +
      ",code=" + code +
      ",logLevel=" + logLevel +
      ",message=" + message +
      "]";
  }

}
