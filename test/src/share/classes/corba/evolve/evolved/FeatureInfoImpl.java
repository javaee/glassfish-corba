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
