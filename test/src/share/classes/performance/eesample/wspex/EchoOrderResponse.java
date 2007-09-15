
package performance.eesample.wspex ;

import java.io.Serializable;

public class EchoOrderResponse implements Serializable {

    protected Order echoOrderResponse;

    /**
     * Gets the value of the echoOrderResponse property.
     * 
     * @return
     *     possible object is
     *     {@link Order }
     *     
     */
    public Order getEchoOrderResponse() {
        return echoOrderResponse;
    }

    /**
     * Sets the value of the echoOrderResponse property.
     * 
     * @param value
     *     allowed object is
     *     {@link Order }
     *     
     */
    public void setEchoOrderResponse(Order value) {
        this.echoOrderResponse = value;
    }

}
