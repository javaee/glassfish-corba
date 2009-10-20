/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.transport;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set ;
import java.util.HashSet ;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSIENT;

import com.sun.corba.se.spi.transport.CorbaContactInfo ;
import com.sun.corba.se.spi.transport.CorbaContactInfoList ;

import com.sun.corba.se.spi.ior.IOR ;
import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.transport.TcpTimeouts;
import com.sun.corba.se.spi.transport.CorbaContactInfo;
import com.sun.corba.se.spi.transport.CorbaContactInfoList;
import com.sun.corba.se.spi.transport.CorbaContactInfoListIterator;
import com.sun.corba.se.spi.transport.IIOPPrimaryToContactInfo;

import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.protocol.CorbaInvocationInfo;
import com.sun.corba.se.impl.orbutil.ORBUtility;

import com.sun.corba.se.impl.orbutil.newtimer.generated.TimingPoints ;

// REVISIT: create a unit test for this class.

public class CorbaContactInfoListIteratorImpl
    implements
	CorbaContactInfoListIterator
{
    protected ORB orb;
    protected CorbaContactInfoList contactInfoList;
    protected RuntimeException failureException;
    protected ORBUtilSystemException wrapper;
    private TimingPoints tp ;
    private boolean usePRLB ;
    protected TcpTimeouts tcpTimeouts ;
    protected boolean debug;

    // ITERATOR state
    protected Iterator<CorbaContactInfo> effectiveTargetIORIterator;
    protected CorbaContactInfo previousContactInfo;
    protected boolean isAddrDispositionRetry;
    protected boolean retryWithPreviousContactInfo;
    protected IIOPPrimaryToContactInfo primaryToContactInfo;
    protected CorbaContactInfo primaryContactInfo;
    protected List<CorbaContactInfo> listOfContactInfos;
    protected TcpTimeouts.Waiter waiter ;
    // Set of endpoints that have failed since the last successful communication
    // with the IOR.
    protected Set<CorbaContactInfo> failedEndpoints ;
    // End ITERATOR state

    public CorbaContactInfoListIteratorImpl(
        ORB orb,
	CorbaContactInfoList corbaContactInfoList,
	CorbaContactInfo primaryContactInfo,
	List listOfContactInfos,
        boolean usePerRequestLoadBalancing )
    {
	this.orb = orb;
	this.tp = orb.getTimerManager().points() ;
	this.wrapper = orb.getLogWrapperTable().get_RPC_TRANSPORT_ORBUtil() ;
	this.tcpTimeouts = orb.getORBData().getTransportTcpConnectTimeouts() ;
	this.debug = orb.transportDebugFlag;
	this.contactInfoList = corbaContactInfoList;
	this.primaryContactInfo = primaryContactInfo;
	if (listOfContactInfos != null) {
	    // listOfContactInfos is null when used by the legacy
	    // socket factory.  In that case this iterator is NOT used.
            
	    this.effectiveTargetIORIterator = listOfContactInfos.iterator() ;
	}
	// List is immutable so no need to synchronize access.
	this.listOfContactInfos = listOfContactInfos;

	this.previousContactInfo = null;
	this.isAddrDispositionRetry = false;
	this.retryWithPreviousContactInfo = false;

	this.failureException = null;

	this.waiter = tcpTimeouts.waiter() ;
	this.failedEndpoints = new HashSet<CorbaContactInfo>() ;

        this.usePRLB = usePerRequestLoadBalancing ;

        if (usePerRequestLoadBalancing) {
            // We certainly DON'T want sticky behavior if we are using PRLB.
            primaryToContactInfo = null ;
        } else {
            primaryToContactInfo = orb.getORBData().getIIOPPrimaryToContactInfo();
        }
    }

    ////////////////////////////////////////////////////
    //
    // java.util.Iterator
    //

    public boolean hasNext() {
	boolean result = false;
	try {
	    tp.enter_contactInfoListIteratorHasNext() ;

	    if (debug) {
		dprint(".hasNext->:");
	    }

	    if (retryWithPreviousContactInfo) {
		if (debug) {
		    dprint(".hasNext: backoff before retry previous");
		}

		if (waiter.isExpired()) {
		    if (debug) {
			dprint(".hasNext: time to wait for connection exceeded " 
			       + tcpTimeouts.get_max_time_to_wait());
		    }
		    
		    // NOTE: Need to indicate the timeout.
		    // And it needs to break the loop in the delegate.
		    failureException = wrapper.communicationsRetryTimeout( failureException,
			    Long.toString(tcpTimeouts.get_max_time_to_wait()));
		    return false;
		}

		waiter.sleepTime() ;
		waiter.advance() ;
		return true;
	    }

	    if (isAddrDispositionRetry) {
		return true;
	    }

	    if (primaryToContactInfo != null) {
		result = primaryToContactInfo.hasNext( primaryContactInfo, 
		    previousContactInfo, listOfContactInfos);
	    } else {
		result = effectiveTargetIORIterator.hasNext();
	    }

	    if (!result && !waiter.isExpired()) {
		if (debug) {
		    dprint("Reached end of ContactInfoList list. Starting at beginning");
		}

		previousContactInfo = null;
		if (primaryToContactInfo != null) {
		    primaryToContactInfo.reset(primaryContactInfo);
		} else {
                    // Argela:
		    effectiveTargetIORIterator = listOfContactInfos.iterator() ;
		}

		result = hasNext();
		return result;
	    }

	    return result;
	} finally {
	    tp.exit_contactInfoListIteratorHasNext() ;

	    if (debug) {
		dprint(".hasNext<-: " + result);
	    }
	}
    }

    public CorbaContactInfo next()
    {
	try {
	    tp.enter_contactInfoListIteratorNext() ;

	    if (retryWithPreviousContactInfo) {
		retryWithPreviousContactInfo = false;
		return previousContactInfo;
	    }

	    if (isAddrDispositionRetry) {
		isAddrDispositionRetry = false;
		return previousContactInfo;
	    }

	    // We hold onto the last in case we get an addressing
	    // disposition retry.  Then we use it again.

	    // We also hold onto it for the sticky manager.

	    if (primaryToContactInfo != null) {
		previousContactInfo = (CorbaContactInfo)
		    primaryToContactInfo.next(primaryContactInfo,
					      previousContactInfo,
					      listOfContactInfos);
	    } else {
		previousContactInfo = effectiveTargetIORIterator.next();
	    }

	    // We must use waiter here regardless of whether or not
	    // there is a IIOPPrimaryToContactInfo or not.
	    // Failure to do this resulted in bug 6568174.
	    if (failedEndpoints.contains(previousContactInfo)) {
		failedEndpoints.clear() ;
		waiter.sleepTime() ;
		waiter.advance() ;
	    }

	    return previousContactInfo;
	} finally {
	    tp.exit_contactInfoListIteratorNext() ;
	}
    }

    public void remove()
    {
	throw new UnsupportedOperationException();
    }

    public CorbaContactInfoList getContactInfoList()
    {
	return contactInfoList;
    }

    public void reportSuccess(CorbaContactInfo contactInfo)
    {
	if (debug) {
	    dprint(".reportSuccess: " + contactInfo);
	}
	failedEndpoints.clear() ;
	waiter.reset() ; // not strictly necessary
    }

    public boolean reportException(CorbaContactInfo contactInfo,
				   RuntimeException ex) {
	boolean result = false;
	try {
	    tp.enter_contactInfoListIteratorReportException() ;
	    if (debug) {
		dprint(".reportException->: " + contactInfo + " " + ex);
	    }

	    failedEndpoints.add( contactInfo ) ;
	    this.failureException = ex;
	    if (ex instanceof COMM_FAILURE) {
		SystemException se = (SystemException) ex;
		if (se.minor == ORBUtilSystemException.CONNECTION_REBIND) {
		    if (debug) {
			dprint(".reportException: " + contactInfo + " " + ex
			    + ": COMM_FAILURE/CONNECTION_REBIND - retryWithPreviousContactInfo");
		    }

		    retryWithPreviousContactInfo = true;
		    result = true;
		    return result;
		} else {
		    if (se.completed == CompletionStatus.COMPLETED_NO) {
			if (hasNext()) {
			    if (debug) {
				dprint(".reportException: " + contactInfo + " " + ex
				    + ": COMM_FAILURE/COMPLETED_NO and hasNext/true - try next ContactInfo");
			    }
			    result = true;
			    return result;
			}
			if (contactInfoList.getEffectiveTargetIOR() !=
			    contactInfoList.getTargetIOR()) {
			    if (debug) {
				dprint(".reportException: " + contactInfo + " " + ex
				    + ": COMM_FAILURE/COMPLETED_NO and hasNext/false and effect != target - retry target");
			    }

			    // retry from root ior
			    updateEffectiveTargetIOR(contactInfoList.getTargetIOR());
			    result = true;
			    return result;
			}
		    }
		}
	    } else if (ex instanceof TRANSIENT) {
		if (debug) {
		    dprint(".reportException: " + contactInfo + " " + ex
			+ ": TRANSIENT - retryWithPreviousContactInfo");
		}
		retryWithPreviousContactInfo = true;
		result = true;
		return result;
	    }
	    result = false;
	    waiter.reset() ; // not strictly necessary.
	    return result;
	} finally {
	    tp.exit_contactInfoListIteratorReportException() ;
	    if (debug) {
		dprint(".reportException<-: " + contactInfo + " " + ex
		    + ": " + result);
	    }
	}
    }

    public RuntimeException getFailureException()
    {
	if (failureException == null) {
	    return
		orb.getLogWrapperTable().get_RPC_TRANSPORT_ORBUtil()
		    .invalidContactInfoListIteratorFailureException();
	} else {
	    return failureException;
	}
    }

    ////////////////////////////////////////////////////
    //
    // spi.CorbaContactInfoListIterator
    //

    public void reportAddrDispositionRetry(CorbaContactInfo contactInfo, 
					   short disposition)
    {
	if (debug) {
	    dprint(".reportAddrDispositionRetry: " 
		   + contactInfo + " " + disposition);
	}
	previousContactInfo.setAddressingDisposition(disposition);
	isAddrDispositionRetry = true;
	waiter.reset() ; // necessary
    }

    public void reportRedirect(CorbaContactInfo contactInfo,
			       IOR forwardedIOR)
    {
	if (debug) {
	    dprint(".reportRedirect: " + contactInfo + " " + forwardedIOR);
	}
	updateEffectiveTargetIOR(forwardedIOR);
	waiter.reset() ; // Necessary
    }

    ////////////////////////////////////////////////////
    //
    // Implementation.
    //

    // 
    // REVISIT:
    // 
    // The normal operation for a standard iterator is to throw
    // ConcurrentModificationException whenever the underlying collection
    // changes.  This is implemented by keeping a modification counter (the
    // timestamp may fail because the granularity is too coarse).
    // Essentially what you need to do is whenever the iterator fails this
    // way, go back to ContactInfoList and get a new iterator.
    //
    // Need to update CorbaClientRequestDispatchImpl to catch and use 
    // that exception.
    //

    public void updateEffectiveTargetIOR(IOR newIOR)
    {
	contactInfoList.setEffectiveTargetIOR(newIOR);
	// If we report the exception in _request (i.e., beginRequest
	// we cannot throw RemarshalException to the stub because _request
	// does not declare that exception.
	// To keep the two-level dispatching (first level chooses ContactInfo,
	// second level is specific to that ContactInfo/EPT) we need to
	// ensure that the request dispatchers get their iterator from the 
	// InvocationStack (i.e., ThreadLocal). That way if the list iterator
	// needs a complete update it happens right here.

        // Ugly hack for Argela: avoid rotating the iterator in this case,
        // or we rotate the iterator twice on every request.
        // XXX remove this hack once we figure out why we get all of the
        // membership changes when the cluster shape does not change.
        CorbaContactInfoListImpl.setSkipRotate() ;

	((CorbaInvocationInfo)orb.getInvocationInfo())
	    .setContactInfoListIterator(contactInfoList.iterator());
    }

    protected void dprint(String msg)
    {
	ORBUtility.dprint("CorbaContactInfoListIteratorImpl", msg);
    }
}

// End of file.
