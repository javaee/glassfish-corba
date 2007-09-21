/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.presentation.rmi ;

import java.io.Serializable ;
import java.io.Externalizable ;

import javax.rmi.PortableRemoteObject ;

import java.rmi.RemoteException ;
import java.rmi.UnexpectedException ;

import org.omg.CORBA.UserException ;

import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.CORBA.portable.ApplicationException ;

import java.lang.reflect.Method ;

import com.sun.corba.se.impl.logging.ORBUtilSystemException ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.impl.orbutil.ClassInfoCache ;

public class ExceptionHandlerImpl implements ExceptionHandler 
{
    private ExceptionRW[] rws ;

    private final ORBUtilSystemException wrapper ;

///////////////////////////////////////////////////////////////////////////////
// ExceptionRW interface and implementations.  
// Used to read and write exceptions.
///////////////////////////////////////////////////////////////////////////////

    public interface ExceptionRW
    {
	Class getExceptionClass() ;

	String getId() ;

	void write( OutputStream os, Exception ex ) ;

	Exception read( InputStream is ) ;
    }

    public abstract class ExceptionRWBase implements ExceptionRW
    {
	private Class cls ;
	private String id ;

	public ExceptionRWBase( Class cls ) 
	{
	    this.cls = cls ;
	}

	public Class getExceptionClass() 
	{
	    return cls ;
	}

	public String getId()
	{
	    return id ;
	}

	void setId( String id )
	{
	    this.id = id ;
	}
    }

    public class ExceptionRWIDLImpl extends ExceptionRWBase
    {
	private Method readMethod ;
	private Method writeMethod ;

	public ExceptionRWIDLImpl( Class cls ) 
	{
	    super( cls ) ;

	    String helperName = cls.getName() + "Helper" ;
	    ClassLoader loader = cls.getClassLoader() ;
	    Class helperClass ;

	    try {
		helperClass = Class.forName( helperName, true, loader ) ;
		Method idMethod = helperClass.getDeclaredMethod( "id" ) ;
		setId( (String)idMethod.invoke( null ) ) ;
	    } catch (Exception ex) {
		throw wrapper.badHelperIdMethod( ex, helperName ) ;
	    }

	    try {
		writeMethod = helperClass.getDeclaredMethod( "write", 
		    org.omg.CORBA.portable.OutputStream.class, cls ) ;
	    } catch (Exception ex) {
		throw wrapper.badHelperWriteMethod( ex, helperName ) ;
	    }

	    try {
		readMethod = helperClass.getDeclaredMethod( "read", 
		    org.omg.CORBA.portable.InputStream.class ) ;
	    } catch (Exception ex) {
		throw wrapper.badHelperReadMethod( ex, helperName ) ;
	    }
	}

	public void write( OutputStream os, Exception ex ) 
	{
	    try {
		writeMethod.invoke( null, os, ex ) ;
	    } catch (Exception exc) {
		throw wrapper.badHelperWriteMethod( exc, 
		    writeMethod.getDeclaringClass().getName() ) ;
	    }
	}

	public Exception read( InputStream is ) 
	{
	    try {
		return (Exception)readMethod.invoke( null, is ) ;
	    } catch (Exception ex) {
		throw wrapper.badHelperReadMethod( ex, 
		    readMethod.getDeclaringClass().getName() ) ;
	    }
	}
    }

    public class ExceptionRWRMIImpl extends ExceptionRWBase
    {
	public ExceptionRWRMIImpl( Class cls ) 
	{
	    super( cls ) ;
	    setId( IDLNameTranslatorImpl.getExceptionId( cls ) ) ;
	}

	public void write( OutputStream os, Exception ex ) 
	{
	    os.write_string( getId() ) ;
	    os.write_value( ex, getExceptionClass() ) ;
	}

	public Exception read( InputStream is ) 
	{
	    is.read_string() ; // read and ignore!
	    return (Exception)is.read_value( getExceptionClass() ) ;
	}
    }

///////////////////////////////////////////////////////////////////////////////

    public ExceptionHandlerImpl( Class[] exceptions )
    {
	wrapper = ORB.getStaticLogWrapperTable().get_RPC_PRESENTATION_ORBUtil() ;

	int count = 0 ;
	for (int ctr=0; ctr<exceptions.length; ctr++) {
	    Class cls = exceptions[ctr] ;
	    if (!ClassInfoCache.get(cls).isARemoteException())
		count++ ;
	}

	rws = new ExceptionRW[count] ;

	int index = 0 ;
	for (int ctr=0; ctr<exceptions.length; ctr++) {
	    Class cls = exceptions[ctr] ;
	    ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( cls ) ;
	    if (!cinfo.isARemoteException()) {
		ExceptionRW erw = null ;
		if (cinfo.isAUserException())
		    erw = new ExceptionRWIDLImpl( cls ) ;
		else
		    erw = new ExceptionRWRMIImpl( cls ) ;

		/* The following check is not performed
		 * in order to maintain compatibility with 
		 * rmic.  See bug 4989312.
		 
		// Check for duplicate repository ID
		String repositoryId = erw.getId() ;
		int duplicateIndex = findDeclaredException( repositoryId ) ;
		if (duplicateIndex > 0) {
		    ExceptionRW duprw = rws[duplicateIndex] ;
		    String firstClassName = 
			erw.getExceptionClass().getName() ;
		    String secondClassName = 
			duprw.getExceptionClass().getName() ;
		    throw wrapper.duplicateExceptionRepositoryId(
			firstClassName, secondClassName, repositoryId ) ;
		}

		*/

		rws[index++] = erw ;
	    }
	}
    }

    private int findDeclaredException( Class cls ) 
    {
        for (int ctr = 0; ctr < rws.length; ctr++) {
            Class next = rws[ctr].getExceptionClass() ;
            if (next.isAssignableFrom(cls))
		return ctr ;
        }

        return -1 ;
    }

    private int findDeclaredException( String repositoryId )
    {
	for (int ctr=0; ctr<rws.length; ctr++) {
	    // This may occur when rws has not been fully 
	    // populated, in which case the search should just fail.
	    if (rws[ctr]==null)
		return -1 ;

	    String rid = rws[ctr].getId() ;
	    if (repositoryId.equals( rid )) 
		return ctr ;
	}

	return -1 ;
    }

    public boolean isDeclaredException( Class cls ) 
    {
	return findDeclaredException( cls ) >= 0 ;
    }

    public void writeException( OutputStream os, Exception ex ) 
    {
	int index = findDeclaredException( ex.getClass() ) ;
	if (index < 0)
	    throw wrapper.writeUndeclaredException( ex,
		ex.getClass().getName() ) ;

	rws[index].write( os, ex ) ;
    }

    public Exception readException( ApplicationException ae ) 
    {
	// Note that the exception ID is present in both ae 
	// and in the input stream from ae.  The exception 
	// reader must actually read the exception ID from
	// the stream.
	InputStream is = (InputStream)ae.getInputStream() ;
	String excName = ae.getId() ;
	int index = findDeclaredException( excName ) ;
	if (index < 0) {
	    excName = is.read_string() ;
	    Exception res = new UnexpectedException( excName ) ;
	    res.initCause( ae ) ;
	    return res ;
	}

	return rws[index].read( is ) ;
    }

    // This is here just for the dynamicrmiiiop test
    public ExceptionRW getRMIExceptionRW( Class cls )
    {
	return new ExceptionRWRMIImpl( cls ) ;
    }
}

