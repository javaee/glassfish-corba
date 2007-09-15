
package performance.eesample.wspex ;

import java.io.Serializable;

public class Struct implements Serializable {

    protected int varInt;
    protected float varFloat;
    protected String varString;

    /**
     * Gets the value of the varInt property.
     * 
     */
    public int getVarInt() {
        return varInt;
    }

    /**
     * Sets the value of the varInt property.
     * 
     */
    public void setVarInt(int value) {
        this.varInt = value;
    }

    /**
     * Gets the value of the varFloat property.
     * 
     */
    public float getVarFloat() {
        return varFloat;
    }

    /**
     * Sets the value of the varFloat property.
     * 
     */
    public void setVarFloat(float value) {
        this.varFloat = value;
    }

    /**
     * Gets the value of the varString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVarString() {
        return varString;
    }

    /**
     * Sets the value of the varString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVarString(String value) {
        this.varString = value;
    }

}
