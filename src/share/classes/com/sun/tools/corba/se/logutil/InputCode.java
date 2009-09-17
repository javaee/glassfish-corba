/*
 * Copyright 2008-2009 Sun Microsystems, Inc.  All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
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
