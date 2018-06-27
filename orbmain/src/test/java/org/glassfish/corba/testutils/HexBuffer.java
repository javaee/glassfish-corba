/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.corba.testutils;

import java.util.List;

public class HexBuffer {
    public static final int BYTES_PER_LINE = 32;
    public static final int SPACES_PER_LINE = 2*BYTES_PER_LINE + BYTES_PER_LINE/4;

    public static void dumpBuffer(byte[] bytes) {
        for (int i = 0; i < bytes.length; i+= BYTES_PER_LINE)
            dumpLine( i, subBuffer(bytes, i, i + BYTES_PER_LINE) );
    }

    private static void dumpLine(int start, byte[] bytes) {
        StringBuilder sb = new StringBuilder(String.format("%4d: ", start));
        int width = 0;
        for (int i = 0; i < bytes.length;) {
            sb.append(String.format("%02X", bytes[i]));
            width += 2;
            if ((++i % 4) == 0) {
                sb.append(' ');
                width++;
            }
        }
        while (width++ < SPACES_PER_LINE)
            sb.append(' ');
        sb.append(' ');
        for (byte aByte : bytes) {
            sb.append(aByte < ' ' ? ' ' : (char)aByte);
        }
        System.out.println(sb.toString());
    }

    private static byte[] subBuffer(byte[] input, int start, int limit) {
        int end = Math.min(limit, input.length);
        byte[] result = new byte[end-start];
        System.arraycopy(input, start, result, 0, result.length);
        return result;
    }

    public static void dumpBuffers(List<byte[]> list) {
        for (byte[] buffer : list) {
            dumpBuffer(buffer);
            System.out.println();
        }
    }
}
