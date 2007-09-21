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

import java.io.PrintStream ;

import java.util.Map ;
import java.util.HashMap ;
import java.util.WeakHashMap ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Iterator ;

import java.lang.reflect.Method ;

import java.rmi.Remote ;

import javax.rmi.CORBA.Tie ;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.omg.PortableServer.POA ;
import org.omg.PortableServer.Servant ;

import com.sun.corba.se.spi.orbutil.proxy.InvocationHandlerFactory ;

import com.sun.corba.se.spi.presentation.rmi.IDLNameTranslator ;
import com.sun.corba.se.spi.presentation.rmi.DynamicMethodMarshaller ;
import com.sun.corba.se.spi.presentation.rmi.PresentationManager ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.impl.logging.ORBUtilSystemException ;

import com.sun.corba.se.impl.presentation.rmi.IDLNameTranslatorImpl ;
import com.sun.corba.se.impl.presentation.rmi.StubFactoryProxyImpl ;

import com.sun.corba.se.impl.util.RepositoryId ;

import com.sun.corba.se.impl.orbutil.graph.Node ;
import com.sun.corba.se.impl.orbutil.graph.Graph ;
import com.sun.corba.se.impl.orbutil.graph.GraphImpl ;

import com.sun.corba.se.impl.orbutil.ClassInfoCache ;

public final class PresentationManagerImpl implements PresentationManager
{
    private Map classToClassData ;
    private Map methodToDMM ;
    private PresentationManager.StubFactoryFactory staticStubFactoryFactory ;
    private PresentationManager.StubFactoryFactory dynamicStubFactoryFactory ;
    private ORBUtilSystemException wrapper = null ;
    private boolean useDynamicStubs ;
    private boolean debug ;
    private PrintStream ps ;

    public PresentationManagerImpl( boolean useDynamicStubs )
    {
	this.useDynamicStubs = useDynamicStubs ;
	wrapper = ORB.getStaticLogWrapperTable().get_RPC_PRESENTATION_ORBUtil() ;

	classToClassData = new WeakHashMap() ;
	methodToDMM = new WeakHashMap() ;
    }

////////////////////////////////////////////////////////////////////////////////
// PresentationManager interface
////////////////////////////////////////////////////////////////////////////////

    public synchronized DynamicMethodMarshaller getDynamicMethodMarshaller( 
	Method method ) 
    {
	if (method == null)
	    return null ;

	DynamicMethodMarshaller result = 
	    (DynamicMethodMarshaller)methodToDMM.get( method ) ;
	if (result == null) {
	    result = new DynamicMethodMarshallerImpl( method ) ;
	    methodToDMM.put( method, result ) ;
	}

	return result ;
    }

    public synchronized ClassData getClassData( Class cls ) 
    {
	ClassData result = (ClassData)classToClassData.get( cls ) ;
	if (result == null) {
	    result = new ClassDataImpl( cls ) ;
	    classToClassData.put( cls, result ) ;
	}

	return result ;
    }

    private class ClassDataImpl implements PresentationManager.ClassData 
    {
	private Class cls ;
	private IDLNameTranslator nameTranslator ;
	private String[] typeIds ;
	private PresentationManager.StubFactory sfactory ;
	private InvocationHandlerFactory ihfactory ;
	private Map dictionary ;

	public ClassDataImpl( Class cls ) 
	{
	    this.cls = cls ;
	    Graph gr = new GraphImpl() ;
	    NodeImpl root = new NodeImpl( cls ) ;
	    Set rootSet = getRootSet( cls, root, gr ) ;

	    // At this point, rootSet contains those remote interfaces
	    // that are not related by inheritance, and gr contains
	    // all reachable remote interfaces.

	    Class[] interfaces = getInterfaces( rootSet ) ;
	    nameTranslator = IDLNameTranslatorImpl.get( interfaces ) ;
	    typeIds = makeTypeIds( root, gr, rootSet ) ;
	    ihfactory = new InvocationHandlerFactoryImpl( 
		PresentationManagerImpl.this, this ) ;
	    dictionary = new HashMap() ;
	}

	public Class getMyClass()
	{
	    return cls ;
	}

	public IDLNameTranslator getIDLNameTranslator()
	{
	    return nameTranslator ;
	}

	public String[] getTypeIds()
	{
	    return typeIds ;
	}

	public InvocationHandlerFactory getInvocationHandlerFactory() 
	{
	    return ihfactory ;
	}

	public Map getDictionary()
	{
	    return dictionary ;
	}
    }

    public PresentationManager.StubFactoryFactory getStubFactoryFactory( 
	boolean isDynamic ) 
    {
	if (isDynamic)
	    return dynamicStubFactoryFactory ;
	else
	    return staticStubFactoryFactory ;
    }

    public void setStubFactoryFactory( boolean isDynamic, 
	PresentationManager.StubFactoryFactory sff ) 
    {
	if (isDynamic)
	    dynamicStubFactoryFactory = sff ;
	else
	    staticStubFactoryFactory = sff ;
    }

    public Tie getTie()
    {
	return dynamicStubFactoryFactory.getTie( null ) ;
    }

    public String getRepositoryId( java.rmi.Remote impl ) 
    {
	// Get an empty reflective Tie.
	Tie tie = getTie() ;
	
	// Setting the target causes the ReflectiveTieImpl to
	// compute all of the required repo ID information.
	tie.setTarget( impl ) ;

	return Servant.class.cast( tie )._all_interfaces( 
	    (POA)null, (byte[])null)[0] ;
    }

    public boolean useDynamicStubs()
    {
	return useDynamicStubs ;
    }

    public void flushClass( final Class cls ) 
    {
	classToClassData.remove( cls ) ;

	Method[] methods = (Method[])AccessController.doPrivileged(
	    new PrivilegedAction() {
		public Object run() {
		    return cls.getMethods() ;
		}
	    } 
	) ;

	for( int ctr=0; ctr<methods.length; ctr++) {
	    methodToDMM.remove( methods[ctr] ) ;
	}
    }

////////////////////////////////////////////////////////////////////////////////
// Graph computations
////////////////////////////////////////////////////////////////////////////////

    private Set getRootSet( Class target, NodeImpl root, Graph gr ) 
    {
	Set rootSet = null ;

	if (ClassInfoCache.get(target).isInterface()) {
	    gr.add( root ) ;
	    rootSet = gr.getRoots() ; // rootSet just contains root here
	} else {
	    // Use this class and its superclasses (not Object) as initial roots
	    Class superclass = target ;
	    Set initialRootSet = new HashSet() ;
	    while ((superclass != null) && !superclass.equals( Object.class )) {
		Node node = new NodeImpl( superclass ) ;
		gr.add( node ) ;
		initialRootSet.add( node ) ;
		superclass = superclass.getSuperclass() ;
	    }

	    // Expand all nodes into the graph
	    gr.getRoots() ; 

	    // remove the roots and find roots again
	    gr.removeAll( initialRootSet ) ;
	    rootSet = gr.getRoots() ;    
	}

	return rootSet ;
    }

    private Class[] getInterfaces( Set roots )
    {
	Class[] classes = new Class[ roots.size() ] ;
	Iterator iter = roots.iterator() ;
	int ctr = 0 ;
	while (iter.hasNext()) {
	    NodeImpl node = (NodeImpl)iter.next() ;
	    classes[ctr++] = node.getInterface() ;
	}

	return classes ;
    }

    private String[] makeTypeIds( NodeImpl root, Graph gr, Set rootSet ) 
    {
	Set nonRootSet = new HashSet( gr ) ;
	nonRootSet.removeAll( rootSet ) ;

	// Handle the case of a remote reference that only implements
	// java.rmi.Remote.
	if (rootSet.size() == 0)
	    return new String[] { "" } ;

	// List<String> for the typeids
	List result = new ArrayList() ;

	if (rootSet.size() > 1) {
	    // If the rootSet has more than one element, we must
	    // put the type id of the implementation class first.
	    // Root represents the implementation class here.
	    result.add( root.getTypeId() ) ;
	}

	addNodes( result, rootSet ) ;
	addNodes( result, nonRootSet ) ;

	return (String[])result.toArray( new String[result.size()] ) ;
    }

    private void addNodes( List resultList, Set nodeSet )
    {
	Iterator iter = nodeSet.iterator() ;
	while (iter.hasNext()) {
	    NodeImpl node = (NodeImpl)iter.next() ;
	    String typeId = node.getTypeId() ;
	    resultList.add( typeId ) ;
	}
    }

    private static class NodeImpl implements Node
    {
	private Class interf ;

	public Class getInterface()
	{
	    return interf ;
	}

	public NodeImpl( Class interf )
	{
	    this.interf = interf ;
	}

	public String getTypeId()
	{
	    return RepositoryId.createForJavaType( interf ) ;
	    // return "RMI:" + interf.getName() + ":0000000000000000" ;
	}

	public Set getChildren()
	{
	    Set result = new HashSet() ;
	    Class[] interfaces = interf.getInterfaces() ;
	    for (int ctr=0; ctr<interfaces.length; ctr++) {
		Class cls = interfaces[ctr] ;
		ClassInfoCache.ClassInfo cinfo = 
		    ClassInfoCache.get( cls ) ;
		if (cinfo.isARemote(cls) &&
		    !Remote.class.equals(cls))
		    result.add( new NodeImpl( cls ) ) ;
	    }

	    return result ;
	}

	public String toString() 
	{
	    return "NodeImpl[" + interf + "]" ;
	}

	public int hashCode()
	{
	    return interf.hashCode() ;
	}

	public boolean equals( Object obj )
	{
	    if (this == obj)
		return true ;

	    if (!(obj instanceof NodeImpl))
		return false ;

	    NodeImpl other = (NodeImpl)obj ;

	    return other.interf.equals( interf ) ;
	}
    }

    public void enableDebug( PrintStream ps ) {
	this.debug = true ;
	this.ps = ps ;
    }

    public void disableDebug() {
	this.debug = false ;
	this.ps = null ;
    }

    public boolean getDebug() {
	return debug ;
    }

    public PrintStream getPrintStream() {
	return ps ;
    }
}
