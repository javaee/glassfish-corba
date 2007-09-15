
package performance.eesample.wspex ;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ArrayOfLineItem implements Serializable {

    protected List<LineItem> lineItem;

    /**
     * Gets the value of the lineItem property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lineItem property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLineItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LineItem }
     * 
     * 
     */
    public List<LineItem> getLineItem() {
        if (lineItem == null) {
            lineItem = new ArrayList<LineItem>();
        }
        return this.lineItem;
    }

}
