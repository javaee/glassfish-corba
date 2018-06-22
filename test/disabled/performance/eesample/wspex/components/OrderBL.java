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

package performance.eesample.wspex.components;

import performance.eesample.wspex.*;

import java.util.*;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class OrderBL {
    
    static DatatypeFactory df;

    public OrderBL() {
    }
    
    public Order GetOrder(int orderId, int customerId) {
        int id = 1;
        
        Address ship = new Address();
        ship.setFirstName("Ship FirstName "+ id);
        ship.setLastName("Ship LastName " + id);
        ship.setAddress1( "Ship StreetAddres " + id);
        ship.setAddress2("Street Address Line 2 " + id);
        ship.setCity( "City " + id);
        ship.setState( "State " + id);
        ship.setZip( "12345");
        
        Address bill = new Address();
        bill.setFirstName("Bill FirstName "+ id);
        bill.setLastName("Bill LastName " + id);
        bill.setAddress1( "Bill StreetAddres " + id);
        bill.setAddress2("Street Address Line 2 " + id);
        bill.setCity( "City " + id);
        bill.setState( "State " + id);
        bill.setZip( "12345");        
        
        Customer customer = new Customer();
        customer.setCustomerId(customerId) ;
        customer.setContactFirstName("FirstName " + id);
        customer.setContactLastName( "LastName " + id);
        customer.setContactPhone(Integer.toString(id));
        
        try {
           if( df == null )
            df = DatatypeFactory.newInstance();
        }
        catch(javax.xml.datatype.DatatypeConfigurationException ex) {
        }
        
        XMLGregorianCalendar date = df.newXMLGregorianCalendar();
        date.setYear(2005);
        date.setMonth(DatatypeConstants.MARCH);
        date.setDay(29);
        date.setTime(11,11,11);
        
        customer.setLastActivityDate(date) ;
        customer.setCreditCardNumber(""+id);
        customer.setCreditCardExpirationDate( ""+id) ;
        customer.setBillingAddress(bill) ;
        customer.setShippingAddress(ship) ;       
        
        int numberLineItems = 50;
        ArrayOfLineItem linearray = new ArrayOfLineItem();
        List<LineItem> lines = linearray.getLineItem();
        
        for(int i = 0; i < numberLineItems; i++) {
            LineItem line = new LineItem();
            line.setOrderId(orderId);
            line.setItemId(i+1);
            line.setProductId(i);
            line.setProductDescription("Test Product " +i);
            line.setOrderQuantity(1);
            line.setUnitPrice((float) 1.00);
            
            lines.add(line);
        }
        
        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderStatus( 1);
        order.setOrderDate(date);
        order.setOrderTotalAmount((float) 50);
        order.setCustomer(customer);
        order.setLineItems(linearray);        
        return order;
    }
}

