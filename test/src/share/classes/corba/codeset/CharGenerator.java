/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package corba.codeset;

/**
 * Utility class for generating characters in Latin-1 and Unicode.
 */
public class CharGenerator
{
    public static char[] getLatin1Chars() {
        return CharGenerator.getChars(new LatinSelector());
    }

    public static char[] getSomeUnicodeChars() {
        return CharGenerator.getChars(new WideSelector());
    }

    public static char[] getChars(CharSelector selector) {
        StringBuffer sbuf = new StringBuffer();
        for (char ch = Character.MIN_VALUE; ch < Character.MAX_VALUE; ch++) {
            if (selector.testThisCharacter(ch))
                sbuf.append(ch);
        }

        char[] result = new char[sbuf.length()];

        sbuf.getChars(0, result.length, result, 0);

        return result;
    }

    private static abstract class CharSelector
    {
        public abstract boolean testThisCharacter(char ch);
    }
    
    private static class LatinSelector extends CharSelector
    {
        public boolean testThisCharacter(char ch)
        {
            Character.UnicodeBlock blk = Character.UnicodeBlock.of(ch);
            
            return (blk != null &&
                    blk == Character.UnicodeBlock.BASIC_LATIN);
        }
    }

    private static class WideSelector extends LatinSelector
    {
        public boolean testThisCharacter(char ch)
        {
            Character.UnicodeBlock blk = Character.UnicodeBlock.of(ch);
            return (blk != null && 
                    (blk == Character.UnicodeBlock.KATAKANA ||
                     blk == Character.UnicodeBlock.HIRAGANA ||
                     blk == Character.UnicodeBlock.LATIN_1_SUPPLEMENT ||
                     blk == Character.UnicodeBlock.LATIN_EXTENDED_A ||
                     blk == Character.UnicodeBlock.LATIN_EXTENDED_B ||
                     blk == Character.UnicodeBlock.LATIN_EXTENDED_ADDITIONAL ||
                     blk == Character.UnicodeBlock.NUMBER_FORMS ||
                     blk == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS ||
                     blk == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
                     blk == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION ||
                     blk == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                     blk == Character.UnicodeBlock.ARABIC ||
                     super.testThisCharacter(ch)));
        }
    }
}
