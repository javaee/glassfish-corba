package javax.management;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

import javax.management.modelmbean.DescriptorSupport;

public class MBeanFeatureInfo implements Serializable, DescriptorRead { 
    static final long serialVersionUID = 3952882688968447265L;

    protected String name;
    protected String description;
    
    private transient Descriptor descriptor;

    public MBeanFeatureInfo(String name, String description) {
        this(name, description, null);
    }

    public MBeanFeatureInfo(String name, String description,
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
	if (!(o instanceof MBeanFeatureInfo))
	    return false;
	MBeanFeatureInfo p = (MBeanFeatureInfo) o;
	return (p.getName().equals(getName()) &&
		p.getDescription().equals(getDescription()) &&
                p.getDescriptor().equals(getDescriptor()));
    }

    public int hashCode() {
	return getName().hashCode() ^ getDescription().hashCode() ^
               getDescriptor().hashCode();
    }

    /**
     * Serializes an {@link MBeanFeatureInfo} to an {@link ObjectOutputStream}.
     * @serialData
     * For compatibility reasons, an object of this class is serialized as follows.
     * <ul>
     * The method {@link ObjectOutputStream#defaultWriteObject defaultWriteObject()}
     * is called first to serialize the object except the field {@code descriptor}
     * which is declared as transient. The field {@code descriptor} is serialized
     * as follows:
     *     <ul>
     *     <li>If {@code descriptor} is an instance of the class
     *        {@link ImmutableDescriptor}, the method {@link ObjectOutputStream#write
     *        write(int val)} is called to write a byte with the value {@code 1},
     *        then the method {@link ObjectOutputStream#writeObject writeObject(Object obj)}
     *        is called twice to serialize the field names and the field values of the
     *        {@code descriptor}, respectively as a {@code String[]} and an
     *        {@code Object[]};</li>
     *     <li>Otherwise, the method {@link ObjectOutputStream#write write(int val)}
     * is called to write a byte with the value {@code 0}, then the method
     * {@link ObjectOutputStream#writeObject writeObject(Object obj)} is called
     * to serialize directly the field {@code descriptor}.
     *     </ul>
     * </ul>
     * @since 1.6
     */
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

    /**
     * Deserializes an {@link MBeanFeatureInfo} from an {@link ObjectInputStream}.
     * @serialData
     * For compatibility reasons, an object of this class is deserialized as follows.
     * <ul>
     * The method {@link ObjectInputStream#defaultReadObject defaultReadObject()}
     * is called first to deserialize the object except the field
     * {@code descriptor}, which is not serialized in the default way. Then the method
     * {@link ObjectInputStream#read read()} is called to read a byte, the field
     * {@code descriptor} is deserialized according to the value of the byte value:
     *    <ul>
     *    <li>1. The method {@link ObjectInputStream#readObject readObject()}
     *       is called twice to obtain the field names (a {@code String[]}) and
     *       the field values (a {@code Object[]}) of the {@code descriptor}.
     *       The two obtained values then are used to construct
     *       an {@link ImmutableDescriptor} instance for the field
     *       {@code descriptor};</li>
     *    <li>0. The value for the field {@code descriptor} is obtained directly
     *       by calling the method {@link ObjectInputStream#readObject readObject()}.
     *       If the obtained value is null, the field {@code descriptor} is set to
     *       {@link ImmutableDescriptor#EMPTY_DESCRIPTOR EMPTY_DESCRIPTOR};</li>
     *    <li>-1. This means that there is no byte to read and that the object is from
     *       an earlier version of the JMX API. The field {@code descriptor} is set
     *       to {@link ImmutableDescriptor#EMPTY_DESCRIPTOR EMPTY_DESCRIPTOR}</li>
     *    <li>Any other value. A {@link StreamCorruptedException} is thrown.</li>
     *    </ul>
     * </ul>
     * @since 1.6
     */
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
