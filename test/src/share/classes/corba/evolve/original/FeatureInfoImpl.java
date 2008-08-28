import corba.evolve.FeatureInfo ;

public class FeatureInfoImpl implements FeatureInfo, java.io.Serializable  { 
    /* Serial version */
    static final long serialVersionUID = 3952882688968447265L;

    protected String name;
    protected String description;
    
    public FeatureInfoImpl(String name, String description)
	    throws IllegalArgumentException {
	this.name = name;    
	this.description = description;
    }

    public String getName() {
	return name;
    }
    
    public String getDescription() {
	return description;
    }  

    public boolean equals(Object o) {
	if (o == this)
	    return true;
	if (!(o instanceof FeatureInfoImpl))
	    return false;
	FeatureInfoImpl p = (FeatureInfoImpl) o;
	return (p.getName().equals(getName()) &&
		p.getDescription().equals(getDescription()));
    }

    public int hashCode() {
	return getName().hashCode() ^ getDescription().hashCode();
    }
}
