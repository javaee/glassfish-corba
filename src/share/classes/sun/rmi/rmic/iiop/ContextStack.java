/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package sun.rmi.rmic.iiop;

import sun.tools.java.CompilerError;

/**
 * ContextStack provides a mechanism to record parsing state.
 * @author      Bryan Atsatt
 */
public class ContextStack {

    // Context codes.
    
    public static final int TOP = 1;

    public static final int METHOD = 2;
    public static final int METHOD_RETURN = 3;
    public static final int METHOD_ARGUMENT = 4;
    public static final int METHOD_EXCEPTION = 5;

    public static final int MEMBER = 6;
    public static final int MEMBER_CONSTANT = 7;
    public static final int MEMBER_STATIC = 8;
    public static final int MEMBER_TRANSIENT = 9;

    public static final int IMPLEMENTS = 10;
    public static final int EXTENDS = 11;

    // String versions of context codes.
    
    private static final String[] CODE_NAMES = {
        "UNKNOWN ",
        "Top level type ",
        "Method ",
        "Return parameter ",
        "Parameter ",
        "Exception ",
        "Member ",
        "Constant member ",
        "Static member ",
        "Transient member ",
        "Implements ",
        "Extends ",
    };
    // Member data.
    
    private int currentIndex = -1;
    private int maxIndex = 100;
    private TypeContext[] stack = new TypeContext[maxIndex];
    private int newCode = TOP;
    private BatchEnvironment env = null;
    private boolean trace = false;
    private TypeContext tempContext = new TypeContext();

    private static final String TRACE_INDENT = "   ";
    
    /**
     * Constructor.
     */
    public ContextStack (BatchEnvironment env) {
        this.env = env;
        env.contextStack = this;
    }
 
    /**
     * Return true if env.nerrors > 0.
     */
    public boolean anyErrors () {
        return env.nerrors > 0;
    }
        
    /**
     * Enable/disable tracing.
     */
    public void setTrace(boolean trace) {
        this.trace = trace;
    }
        
    /**
     * Check trace flag.
     */
    public boolean isTraceOn() {
        return trace;
    }

    /**
     * Get the environment.
     */
    public BatchEnvironment getEnv() {
        return env;
    }
   
    /**
     * Set the new context.
     */
    public void setNewContextCode(int code) {
        newCode = code;
    }
        
    /**
     * Get the current context code.
     */
    public int getCurrentContextCode() {
        return newCode;
    }
 

    /**
     * If tracing on, write the current call stack (not the context stack) to
     * System.out.
     */
    final void traceCallStack () {
        if (trace) dumpCallStack();
    }
    
    public final static void dumpCallStack() {
        new Error().printStackTrace(System.out);
    }
    
    /**
     * Print a line indented by stack depth.
     */
    final private void tracePrint (String text, boolean line) {
        int length = text.length() + (currentIndex * TRACE_INDENT.length());
        StringBuffer buffer = new StringBuffer(length);
        for (int i = 0; i < currentIndex; i++) {
            buffer.append(TRACE_INDENT);   
        }
        buffer.append(text);
        if (line) {
            buffer.append("\n");
        }
        System.out.print(buffer.toString());
    }
    
    /**
     * If tracing on, print a line.
     */
    final void trace (String text) {
        if (trace) {
            tracePrint(text,false);
        }
    }
    
    /**
     * If tracing on, print a line followed by a '\n'.
     */
    final void traceln (String text) {
        if (trace) {
            tracePrint(text,true);
        }
    }
    
    /**
     * If tracing on, print a pre-mapped ContextElement.
     */
    final void traceExistingType (Type type) {
        if (trace) {
            tempContext.set(newCode,type);
            traceln(toResultString(tempContext,true,true));
        }
    }
    
    /**
     * Push a new element on the stack.
     * @return the new element.
     */
    public TypeContext push (ContextElement element) {

        currentIndex++;

        // Grow array if need to...

        if (currentIndex == maxIndex) {
            int newMax = maxIndex * 2;
            TypeContext[] newStack = new TypeContext[newMax];
            System.arraycopy(stack,0,newStack,0,maxIndex);
            maxIndex = newMax;
            stack = newStack;
        }

        // Make sure we have a context object to use at this position...
        
        TypeContext it = stack[currentIndex];

        if (it == null) {
            it = new TypeContext();
            stack[currentIndex] = it;
        }
        
        // Set the context object...

        it.set(newCode,element);
        
        // Trace...
        
        traceln(toTrialString(it));
        
        // Return...
        
        return it;
    }

    /**
     * Pop an element from the stack.
     * @return the new current element or null if top.
     */
    public TypeContext pop (boolean wasValid) {

        if (currentIndex < 0) {
            throw new CompilerError("Nothing on stack!");
        }
        
        newCode = stack[currentIndex].getCode();
        traceln(toResultString(stack[currentIndex],wasValid,false));
        
        Type last = stack[currentIndex].getCandidateType();
        if (last != null) {
        
            // Set status...
           
            if (wasValid) {
                last.setStatus(Constants.STATUS_VALID);
            } else {
                last.setStatus(Constants.STATUS_INVALID);
            }          
        }
        
        currentIndex--;       
        
        if (currentIndex < 0) {
            
            // Done parsing, so update the invalid types
            // if this type was valid...

            if (wasValid) {
                Type.updateAllInvalidTypes(this);
            }
            return null;
        } else {
            return stack[currentIndex];
        }
    }

    /**
     * Get the current size.
     */
    public int size () {
        return currentIndex + 1;
    }

    /**
     * Get a specific context.
     */
    public TypeContext getContext (int index) {

        if (currentIndex < index) {
            throw new Error("Index out of range");
        }
        return stack[index];
    }

    /**
     * Get the current top context.
     */
    public TypeContext getContext () {

        if (currentIndex < 0) {
            throw new Error("Nothing on stack!");
        }
        return stack[currentIndex];
    }

    /**
     * Is parent context a value type?
     */
    public boolean isParentAValue () {
        
        if (currentIndex > 0) {
            return stack[currentIndex - 1].isValue();
        } else {
            return false;
        }
    }

    /**
     * Get parent context. Null if none.
     */
    public TypeContext getParentContext () {
        
        if (currentIndex > 0) {
            return stack[currentIndex - 1];
        } else {
            return null;
        }
    }
    
    /**
     * Get a string for the context name...
     */
    public String getContextCodeString () {
        
        if (currentIndex >= 0) {
            return CODE_NAMES[newCode];
        } else {
            return CODE_NAMES[0];
        }
    }
    
    /**
     * Get a string for the given context code...
     */
    public static String getContextCodeString (int contextCode) {
        return CODE_NAMES[contextCode];
    }

    private String toTrialString(TypeContext it) {
        int code = it.getCode();
        if (code != METHOD && code != MEMBER) {
            return it.toString() + " (trying " + it.getTypeDescription() + ")";
        } else {
            return it.toString();
        }
    }
    
    private String toResultString (TypeContext it, boolean result, boolean preExisting) {
        int code = it.getCode();        
        if (code != METHOD && code != MEMBER) {
            if (result) {
                String str = it.toString() + " --> " + it.getTypeDescription();
                if (preExisting) {
                    return str + " [Previously mapped]";
                } else {
                    return str;
                }
            }
        } else {
            if (result) {
                return it.toString() + " --> [Mapped]";
            }
        }
        return it.toString() + " [Did not map]";
    }  
    
    public void clear () {
        for (int i = 0; i < stack.length; i++) {
            if (stack[i] != null) stack[i].destroy();   
        }
    }
}


class TypeContext {

    public void set(int code, ContextElement element) {
        this.code = code;
        this.element = element;
        if (element instanceof ValueType) {
            isValue = true;
        } else {
            isValue = false;
        }
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return element.getElementName();
    }

    public Type getCandidateType() {
        if (element instanceof Type) {
            return (Type) element;
        } else {
            return null;
        }
}

public String getTypeDescription() {
    if (element instanceof Type) {
        return ((Type) element).getTypeDescription();
    } else {
        return "[unknown type]";
    }
}

public String toString () {
    if (element != null) {
        return ContextStack.getContextCodeString(code) + element.getElementName();
    } else {
        return ContextStack.getContextCodeString(code) + "null";
    }
}

public boolean isValue () {
    return isValue;
}

    public boolean isConstant () {
        return code == ContextStack.MEMBER_CONSTANT;
    }
    
    public void destroy() {
        if (element instanceof Type) {
            ((Type)element).destroy();
        }
        element = null;
    }
    
    private int code = 0;
    private ContextElement element = null;
    private boolean isValue = false;
}
