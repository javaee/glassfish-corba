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
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

import corba.evolve.FeatureInfo ;

public class FeatureInfoImpl implements FeatureInfo, Serializable { 
    static final long serialVersionUID = 3952882688968447265L;

    protected String name;
    protected String description;
    
    private transient Descriptor descriptor;

    public FeatureInfoImpl(String name, String description) {
        this(name, description, null);
    }

    public FeatureInfoImpl(String name, String description,
                            Descriptor descriptor) {
        this.name = name;
        this.description = description;
        this.descriptor = descriptor;
    }

    public String getName() {
	return name;
    }
    
    public String getDescription() {
	return description;
    }
    
    public Descriptor getDescriptor() {
        return (Descriptor) ImmutableDescriptor.nonNullDescriptor(descriptor).clone();
    }

    public boolean equals(Object o) {
	if (o == this)
	    return true;
	if (!(o instanceof FeatureInfoImpl))
	    return false;
	FeatureInfoImpl p = (FeatureInfoImpl) o;
	return (p.getName().equals(getName()) &&
		p.getDescription().equals(getDescription()) &&
                p.getDescriptor().equals(getDescriptor()));
    }

    public int hashCode() {
	return getName().hashCode() ^ getDescription().hashCode() ^
               getDescriptor().hashCode();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

	if (descriptor != null &&
	    descriptor.getClass() == ImmutableDescriptor.class) {
	    
	    out.write(1);

	    final String[] names = descriptor.getFieldNames();

	    out.writeObject(names);
	    out.writeObject(descriptor.getFieldValues(names));
	} else {
	    out.write(0);

	    out.writeObject(descriptor);
	}
    }

    private void readObject(ObjectInputStream in)
	throws IOException, ClassNotFoundException {

	in.defaultReadObject();

	switch (in.read()) {
	case 1:
	    final String[] names = (String[])in.readObject();

	    if (names.length == 0) {
		descriptor = ImmutableDescriptor.EMPTY_DESCRIPTOR;
	    } else {
		final Object[] values = (Object[])in.readObject();
		descriptor = new ImmutableDescriptor(names, values);
	    }

	    break;
	case 0:
	    descriptor = (Descriptor)in.readObject();

	    if (descriptor == null) {
		descriptor = ImmutableDescriptor.EMPTY_DESCRIPTOR;
	    }

	    break;
	case -1: // from an earlier version of the JMX API
	    descriptor = ImmutableDescriptor.EMPTY_DESCRIPTOR;

	    break;
	default:
	    throw new StreamCorruptedException("Got unexpected byte.");
	}
    }
}
