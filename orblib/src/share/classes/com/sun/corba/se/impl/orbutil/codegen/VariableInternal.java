/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.corba.se.impl.orbutil.codegen;

import com.sun.corba.se.spi.orbutil.codegen.Variable;

/**
 *
 * @author ken
 */
public interface VariableInternal extends ExpressionInternal, Variable {
    /** Returns true if this variable is still in scope.
     * Only variables still in scope may be referenced in
     * expressions.
     */
    boolean isAvailable() ;

    /** Mark the variable so that it is no longer in scope.
     */
    void close() ;
}
