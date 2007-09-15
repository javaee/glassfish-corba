
package performance.eesample.wspex ;

import java.io.Serializable;

public class EchoOrderRequest implements Serializable {

    protected Order echoOrderRequest;

    /**
     * Gets the value of the echoOrderRequest property.
     * 
     * @return
     *     possible object is
     *     {@link Order }
     *     
     */
    public Order getEchoOrderRequest() {
        return echoOrderRequest;
    }

    /**
     * Sets the value of the echoOrderRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link Order }
     *     
     */
    public void setEchoOrderRequest(Order value) {
        this.echoOrderRequest = value;
    }

}
