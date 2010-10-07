/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

package com.sun.corba.se.spi.ior.iiop ;

import com.sun.corba.se.spi.ior.IOR ;
import com.sun.corba.se.spi.ior.iiop.IIOPProfile;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orb.ORBVersion;
import com.sun.corba.se.spi.orb.ORBVersionFactory;

import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.spi.orbutil.ORBConstants;

import org.glassfish.gmbal.ManagedData ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.Description ;

@ManagedData
@Description( "The maximum GIOP version supported by this IOR" )
public class GIOPVersion {

    // Static fields

    public static final GIOPVersion V1_0 = new GIOPVersion((byte)1, (byte)0);
    public static final GIOPVersion V1_1 = new GIOPVersion((byte)1, (byte)1);
    public static final GIOPVersion V1_2 = new GIOPVersion((byte)1, (byte)2);
    public static final GIOPVersion V1_3 = new GIOPVersion((byte)1, (byte)3);

    // Major version 13 is used to denote special encodings.
    // Minor version 00 is unused.
    // Minor version [01-FF] specifies Java serialization encoding version.
    public static final GIOPVersion V13_XX =
	new GIOPVersion((byte)13, (byte)ORBConstants.JAVA_ENC_VERSION);

    public static final GIOPVersion DEFAULT_VERSION = V1_2;

    public static final int VERSION_1_0 = 0x0100;
    public static final int VERSION_1_1 = 0x0101;
    public static final int VERSION_1_2 = 0x0102;
    public static final int VERSION_1_3 = 0x0103;
    public static final int VERSION_13_XX = 
	(13 << 8) | ORBConstants.JAVA_ENC_VERSION;

    // Instance variables

    private byte major = (byte) 0;
    private byte minor = (byte) 0;

    // Constructor

    public GIOPVersion() {}

    public GIOPVersion(byte majorB, byte minorB) {
        this.major = majorB;
        this.minor = minorB;
    }

    public GIOPVersion(int major, int minor) {
        this.major = (byte)major;
        this.minor = (byte)minor;
    }

    // Accessor methods

    @ManagedAttribute
    @Description( "The Major GIOP version (almost always 1)" )
    public byte getMajor() {
        return this.major;
    }

    @ManagedAttribute
    @Description( "The Minor GIOP version (almost always 0, 1, or 2."
        + " This ORB almost always uses 2" )
    public byte getMinor() {
        return this.minor;
    }

    // General methods

    public boolean equals(GIOPVersion gv){
        if (gv == null) {
            return false ;
        } 

        return gv.major == this.major && gv.minor == this.minor ;
    }

    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof GIOPVersion))
            return equals((GIOPVersion)obj);
        else
            return false;
    }

    public int hashCode() 
    {
	return 37*major + minor ;
    }

    public boolean lessThan(GIOPVersion gv) {
        if (this.major < gv.major) {
            return true;
        } else if (this.major == gv.major) {
            if (this.minor < gv.minor) {
                return true;
            }
        }

        return false;
    }

    public int intValue()
    {
        return (major << 8 | minor);
    }

    public String toString()
    {
        return major + "." + minor;
    }

    public static GIOPVersion getInstance(byte major, byte minor)
    {
        switch(((major << 8) | minor)) {
            case VERSION_1_0:
                return GIOPVersion.V1_0;
            case VERSION_1_1:
                return GIOPVersion.V1_1;
            case VERSION_1_2:
                return GIOPVersion.V1_2;
            case VERSION_1_3:
                return GIOPVersion.V1_3;
            case VERSION_13_XX:
                return GIOPVersion.V13_XX;
            default:
                return new GIOPVersion(major, minor);
        }
    }

    public static GIOPVersion parseVersion(String s)
    {
        int dotIdx = s.indexOf('.');

        if (dotIdx < 1 || dotIdx == s.length() - 1)
            throw new NumberFormatException("GIOP major, minor, and decimal point required: " + s);

        int major = Integer.parseInt(s.substring(0, dotIdx));
        int minor = Integer.parseInt(s.substring(dotIdx + 1, s.length()));

        return GIOPVersion.getInstance((byte)major, (byte)minor);
    }

    /**
     * This chooses the appropriate GIOP version.
     *
     * @return smallest(profGIOPVersion, orbGIOPVersion).
     */
    public static GIOPVersion chooseRequestVersion(ORB orb, IOR ior ) {

        GIOPVersion orbVersion = orb.getORBData().getGIOPVersion();
	IIOPProfile prof = ior.getProfile() ;
        GIOPVersion profVersion = prof.getGIOPVersion();

        // Check if the profile is from a legacy Sun ORB.

        ORBVersion targetOrbVersion = prof.getORBVersion();
        if (!(targetOrbVersion.equals(ORBVersionFactory.getFOREIGN())) &&
                targetOrbVersion.lessThan(ORBVersionFactory.getNEWER())) {
            // we are dealing with a SUN legacy orb which emits 1.1 IORs,
            // in spite of being able to handle only GIOP 1.0 messages.
            return V1_0;
        }

        // Now the target has to be (FOREIGN | NEWER*)

        byte prof_major = profVersion.getMajor();
        byte prof_minor = profVersion.getMinor();

        byte orb_major = orbVersion.getMajor();
        byte orb_minor = orbVersion.getMinor();

        if (orb_major < prof_major) {
            return orbVersion;
        } else if (orb_major > prof_major) {
            return profVersion;
        } else { // both major version are the same
            if (orb_minor <= prof_minor) {
                return orbVersion;
            } else {
                return profVersion;
            }
        }
    }

    public boolean supportsIORIIOPProfileComponents()
    {
        return getMinor() > 0 || getMajor() > 1;
    }

    // IO methods

    public void read(org.omg.CORBA.portable.InputStream istream) {
        this.major = istream.read_octet();
        this.minor = istream.read_octet();
    }

    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        ostream.write_octet(this.major);
        ostream.write_octet(this.minor);
    }
}
