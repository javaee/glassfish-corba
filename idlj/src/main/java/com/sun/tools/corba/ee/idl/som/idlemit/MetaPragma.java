/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
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

package com.sun.tools.corba.ee.idl.som.idlemit;
import com.sun.tools.corba.ee.idl.ForwardEntry;
import com.sun.tools.corba.ee.idl.PragmaHandler;
import com.sun.tools.corba.ee.idl.SymtabEntry;
import com.sun.tools.corba.ee.idl.som.cff.Messages;

import java.util.Vector;
/**
 * This is an implementation that handles 
 * #pragma meta scoped_name string
 * where
 * <UL>
 * <LI>    scoped_name ==  "::" separated scoped name
 * <LI>    string ==  separated identifiers, such as "localonly",
 *          "abstract", or "init". 
 *         D59407: NOTE: any non-white-space is grouped
 *          as part of the identifier.
 * </UL>
 *
 * This pragma handler places a vector of Strings into the dynamicVariable()
 * part of the SymtabEntry. The key to access the dynamicVariable()
 * is MetaPragma.metaKey
 *
 * It is possible to associate a meta pragma with a forward entry.
 * At some point after the parser has completed, 
 * the method processForward(ForwardEntry entry) should be called 
 * for each ForwardEntry so that the meta information can be folded from 
 * the ForwardEntry into the corresponding InterfaceEntry.
 */
public class MetaPragma extends PragmaHandler {
    /* Class variables */

    /* key to access the Cached meta info in SymtabEntry */
    public static int metaKey = SymtabEntry.getVariableKey();


    /**
     * Main entry point for the MetaPragma handler
     * @param pragma string for pragma name
     * @param currentToken next token in the input stream.
     * @return true if this is a meta pragma.
     */
    public boolean process(String pragma, String currentToken) {
        if ( !pragma.equals("meta"))
            return false;

        SymtabEntry entry ;
        String msg;
        try {
            entry = scopedName();
            if ( entry == null){
                /* scoped name not found */
                parseException(Messages.msg("idlemit.MetaPragma.scopedNameNotFound"));
                skipToEOL();
            }
            else {
                msg = (currentToken()+ getStringToEOL());
// System.out.println(entry + ":  " + msg);
                Vector v;
                v = (Vector) entry.dynamicVariable(metaKey);
                if ( v== null){
                    v = new Vector();
                    entry.dynamicVariable(metaKey, v);
                }
                parseMsg(v, msg);
           }
        } catch(Exception e){
// System.out.println("exception in MetaPragma");
        }
        return true;
    }


    /**
     * Fold the meta info from the forward entry into its corresponding
     * interface entry.
     * @param forwardEntry the forward entry to process
     */
    static public void processForward(ForwardEntry forwardEntry){

        Vector forwardMeta;
        try {
            forwardMeta = (Vector)forwardEntry.dynamicVariable(metaKey);
        } catch (Exception e){
            forwardMeta = null;
        }
        SymtabEntry forwardInterface = forwardEntry.type();
        if (forwardMeta != null && forwardInterface!= null) {
            Vector interfaceMeta;
            try {
                 interfaceMeta= (Vector)forwardInterface.dynamicVariable(metaKey);
            } catch ( Exception e){
                 interfaceMeta = null;
            }

            if ( interfaceMeta == null) {
                /* set */
                try {
                    forwardInterface.dynamicVariable(MetaPragma.metaKey, forwardMeta);
                } catch(Exception e){};
            }
            else if (interfaceMeta != forwardMeta) {
                 /* The above check is needed because sometimes
                 a forward entry is processed more the once.
                 Not sure why */
                /* merge */
                for (int i=0; i < forwardMeta.size(); i++){
                    try {
                        Object obj = forwardMeta.elementAt(i);
                        interfaceMeta.addElement(obj);
                    } catch (Exception e){};
                }
            }
         }
    }

    /**
     * parse pragma message and place into vector v.
     * @param v: vector to add message
     * @param msg: string of comma separated message, perhaps with comment.
     * This is implemented as a state machine as follows:
     *
     *  State          token        next             action
     *  -----------------------------------------------------
     *   initial     whitespace     initial          
     *   initial     SlashStar      comment          
     *   initial     SlashSlash     final              
     *   initial     no more        final              
     *   initial     text           text             add to text buffer
     *   initial     StarSlash      initial
     *   comment     StarSlash      initial          
     *   comment     SlashStar      comment
     *   comment     whitespace     comment
     *   comment     SlashSlash     comment          
     *   comment     text           comment
     *   comment     no more        final
     *   text        text           text              add to buffer
     *   text        SlashStar      comment           put in vector
     *   text        whitespace     initial           put in vector
     *   text        SlashSlash     final             put in vector
     *   text        StarSlash      initial           put in vector
     *   text        no more        final             put in vector
     *   
    */
    private static int initialState = 0;
    private static int commentState = 1;
    private static int textState = 2;
    private static int finalState =3;

    private void parseMsg(Vector v, String msg){
        int state = initialState;
        String text = "";
        int index = 0;
        while ( state != finalState ){
             boolean isNoMore = index >= msg.length();
             char ch = ' ';   
             boolean isSlashStar = false;
             boolean isSlashSlash = false;
             boolean isWhiteSpace = false;
             boolean isStarSlash = false;
             boolean isText = false;
             if (!isNoMore ){
                 ch = msg.charAt(index);
                 if (ch == '/' && index+1 < msg.length()){
                     if (msg.charAt(index+1) == '/'){
                         isSlashSlash = true;
                          index++;
                     }
                     else if (msg.charAt(index+1) == '*'){
                         isSlashStar= true;
                         index++;
                     } else isText = true;
                 }
                 else if (ch == '*' && index+1 < msg.length() ){
                     if (msg.charAt(index+1) == '/'){
                         isStarSlash = true;
                         index++;
                     } else isText = true;
                 } 
                 else if ( Character.isSpaceChar(ch) || (ch == ',') // 59601
                              || (ch == ';') ) // 59683
                     isWhiteSpace = true;
                 else isText = true;
            }
   
            if (state == initialState){
                   if (isSlashStar){
                      state = commentState;
                   }
                   else if (isSlashSlash || isNoMore){
                      state = finalState;
                   }
                   else if (isText){
                       state = textState;
                       text = text+ ch;
                   }
             }
             else if (state == commentState){
                   if (isNoMore){
                        state = finalState;
                   }
                   else if ( isStarSlash){
                        state = initialState;
                   }
             }
             else if (state == textState){
                   if (isNoMore || isStarSlash || isSlashSlash ||
                       isSlashStar || isWhiteSpace ){
                       if (!text.equals("")) {
                           v.addElement(text);
// System.err.println("adding " + text);
                           text = "";
                       }
                       if (isNoMore)
                            state = finalState;
                       else if (isSlashStar)
                            state = commentState;
                       else state = initialState;
                   }
                   else if (isText){
                       text = text+ch;
                   }
             }
             index++;
        }
    }

}
