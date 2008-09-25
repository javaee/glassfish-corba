import java.io.Serializable;

public interface Descriptor extends Serializable, Cloneable
{
    public Object getFieldValue(String fieldName) ;

    public void setField(String fieldName, Object fieldValue) ;

    public String[] getFields();
    public String[] getFieldNames();
    public Object[] getFieldValues(String... fieldNames);
    public void removeField(String fieldName);
    public void setFields(String[] fieldNames, Object[] fieldValues) ;
    public Object clone() ;
    public boolean isValid() ;
    public boolean equals(Object obj);
    public int hashCode();
}
