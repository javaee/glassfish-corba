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

import java.util.LinkedList;
import java.util.Queue;

public class InputException {

  /**
   * The name of this exception.
   */
  private final String name;

  /**
   * The codes associated with this exception.
   */
  private final Queue<InputCode> codes;

  /**
   * Constructs a new {@link InputException} with the
   * specified name.
   *
   * @param name the name of the new exception;
   */
  public InputException(final String name) {
    this.name = name;
    codes = new LinkedList<InputCode>();
  }

  /**
   * Adds a new code to this exception.
   *
   * @param c the code to add.
   */
  public void add(InputCode c)
  {
    codes.offer(c);
  }

  /**
   * Returns the name of this exception.
   *
   * @return the exception's name.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the codes associated with this exception.
   *
   * @return the exception's codes.
   */
  public Queue<InputCode> getCodes() {
    return codes;
  }

  /**
   * Returns a textual representation of this exception.
   *
   * @return a textual representation.
   */
  public String toString() {
    return getClass().getName()
      + "[name=" + name
      + ",codes=" + codes
      + "]";
  }

}
