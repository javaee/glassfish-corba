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

package com.sun.corba.se.impl.presentation.rmi ;

import java.io.PrintStream ;

import java.util.Map ;
import java.util.HashMap ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.List ;
import java.util.ArrayList ;

import java.lang.reflect.Method ;

import java.rmi.Remote ;

import javax.rmi.CORBA.Tie ;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.omg.PortableServer.POA ;
import org.omg.PortableServer.Servant ;

import com.sun.corba.se.spi.presentation.rmi.IDLNameTranslator ;
import com.sun.corba.se.spi.presentation.rmi.DynamicMethodMarshaller ;
import com.sun.corba.se.spi.presentation.rmi.PresentationManager ;

import com.sun.corba.se.spi.logging.ORBUtilSystemException ;

import com.sun.corba.se.impl.util.RepositoryId ;

import com.sun.corba.se.impl.misc.ClassInfoCache ;
import org.glassfish.pfl.basic.concurrent.WeakCache;
import org.glassfish.pfl.basic.graph.Graph ;
import org.glassfish.pfl.basic.graph.GraphImpl ;
import org.glassfish.pfl.basic.graph.Node ;
import org.glassfish.pfl.basic.graph.NodeData ;
import org.glassfish.pfl.basic.proxy.InvocationHandlerFactory;

public final class PresentationManagerImpl implements PresentationManager
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private WeakCache<Class<?>,ClassData> classToClassData ;
    private WeakCache<Method,DynamicMethodMarshaller> methodToDMM ;
    private PresentationManager.StubFactoryFactory staticStubFactoryFactory ;
    private PresentationManager.StubFactoryFactory dynamicStubFactoryFactory ;
    private boolean useDynamicStubs ;
    private boolean debug ;
    private PrintStream ps ;

    public PresentationManagerImpl( boolean useDynamicStubs )
    {
	this.useDynamicStubs = useDynamicStubs ;

	classToClassData = new WeakCache<Class<?>,ClassData>() {
            @Override
            protected ClassData lookup(Class<?> key) {
                return new ClassDataImpl( key ) ;
            }
        } ;

	methodToDMM = new WeakCache<Method,DynamicMethodMarshaller>() {
            @Override
            protected DynamicMethodMarshaller lookup(Method key) {
                return new DynamicMethodMarshallerImpl( key ) ;
            }
        } ;
    }

////////////////////////////////////////////////////////////////////////////////
// PresentationManager interface
////////////////////////////////////////////////////////////////////////////////

    public synchronized DynamicMethodMarshaller getDynamicMethodMarshaller( 
	Method method ) 
    {
	if (method == null) {
            return null;
        }

	return methodToDMM.get(method) ;
    }

    public synchronized ClassData getClassData( Class<?> cls )
    {
	return classToClassData.get(cls) ;
    }

    private class ClassDataImpl implements PresentationManager.ClassData 
    {
	private Class<?> cls ;
	private IDLNameTranslator nameTranslator ;
	private String[] typeIds ;
	private InvocationHandlerFactory ihfactory ;
	private Map<String,Object> dictionary ;

	ClassDataImpl( Class<?> cls ) {
	    this.cls = cls ;
	    Graph<NodeImpl> gr = new GraphImpl<NodeImpl>() ;
	    NodeImpl root = new NodeImpl( cls ) ;
	    Set<NodeImpl> rootSet = getRootSet( cls, root, gr ) ;

	    // At this point, rootSet contains those remote interfaces
	    // that are not related by inheritance, and gr contains
	    // all reachable remote interfaces.

	    Class<?>[] interfaces = getInterfaces( rootSet ) ;
	    nameTranslator = IDLNameTranslatorImpl.get( interfaces ) ;
	    typeIds = makeTypeIds( root, gr, rootSet ) ;
	    ihfactory = new InvocationHandlerFactoryImpl( 
		PresentationManagerImpl.this, this ) ;
	    dictionary = new HashMap<String,Object>() ;
	}

	public Class<?> getMyClass()
	{
	    return cls ;
	}

	public IDLNameTranslator getIDLNameTranslator()
	{
	    return nameTranslator ;
	}

	public String[] getTypeIds()
	{
	    return typeIds.clone() ;
	}

	public InvocationHandlerFactory getInvocationHandlerFactory() 
	{
	    return ihfactory ;
	}

	public Map<String,Object> getDictionary()
	{
	    return dictionary ;
	}
    }

    public PresentationManager.StubFactoryFactory getStubFactoryFactory( 
	boolean isDynamic ) 
    {
	if (isDynamic) {
            return dynamicStubFactoryFactory;
        } else {
            return staticStubFactoryFactory;
        }
    }

    public void setStubFactoryFactory( boolean isDynamic, 
	PresentationManager.StubFactoryFactory sff ) 
    {
	if (isDynamic) {
            dynamicStubFactoryFactory = sff;
        } else {
            staticStubFactoryFactory = sff;
        }
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

    public void flushClass( final Class<?> cls )
    {
	classToClassData.remove( cls ) ;

	Method[] methods = (Method[])AccessController.doPrivileged(
	    new PrivilegedAction<Object>() {
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

    private Set<NodeImpl> getRootSet( Class<?> target, NodeImpl root,
        Graph<NodeImpl> gr )
    {
	Set<NodeImpl> rootSet = null ;

	if (ClassInfoCache.get(target).isInterface()) {
	    gr.add( root ) ;
	    rootSet = gr.getRoots() ; // rootSet just contains root here
	} else {
	    // Use this class and its superclasses (not Object) as initial roots
	    Class<?> superclass = target ;
	    Set<NodeImpl> initialRootSet = new HashSet<NodeImpl>() ;
	    while ((superclass != null) && !superclass.equals( Object.class )) {
		NodeImpl node = new NodeImpl( superclass ) ;
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

    private Class<?>[] getInterfaces( Set<NodeImpl> roots )
    {
	int ctr = 0 ;
	Class<?>[] classes = new Class<?>[ roots.size() ] ;
        for (NodeImpl node : roots) {
	    classes[ctr] = node.getInterface() ;
            ctr++ ;
        }

	return classes ;
    }

    private String[] makeTypeIds( NodeImpl root, Graph<NodeImpl> gr,
        Set<NodeImpl> rootSet )
    {
	Set<NodeImpl> nonRootSet = new HashSet<NodeImpl>( gr ) ;
	nonRootSet.removeAll( rootSet ) ;

	// Handle the case of a remote reference that only implements
	// java.rmi.Remote.
	if (rootSet.isEmpty()) {
            return new String[]{""};
        }

	// List<String> for the typeids
	List<String> result = new ArrayList<String>() ;

	if (rootSet.size() > 1) {
	    // If the rootSet has more than one element, we must
	    // put the type id of the implementation class first.
	    // Root represents the implementation class here.
	    result.add( root.getTypeId() ) ;
	}

	addNodes( result, rootSet ) ;
	addNodes( result, nonRootSet ) ;

	return result.toArray(new String[result.size()]) ;
    }

    private void addNodes( List<String> resultList, Set<NodeImpl> nodeSet )
    {
        for (NodeImpl node : nodeSet) {
	    String typeId = node.getTypeId() ;
	    resultList.add( typeId ) ;
        }
    }

    private static class NodeImpl implements Node<NodeImpl>
    {
	private Class<?> interf ;

	public Class<?> getInterface()
	{
	    return interf ;
	}

	NodeImpl( Class<?> interf )
	{
	    this.interf = interf ;
	}

	public String getTypeId()
	{
	    return RepositoryId.createForJavaType( interf ) ;
	    // return "RMI:" + interf.getName() + ":0000000000000000" ;
	}

	public Set<NodeImpl> getChildren()
	{
	    Set<NodeImpl> result = new HashSet<NodeImpl>() ;
	    Class<?>[] interfaces = interf.getInterfaces() ;
	    for (int ctr=0; ctr<interfaces.length; ctr++) {
		Class<?> cls = interfaces[ctr] ;
		ClassInfoCache.ClassInfo cinfo = 
		    ClassInfoCache.get( cls ) ;
		if (cinfo.isARemote(cls) &&
		    !Remote.class.equals(cls)) {
                    result.add(new NodeImpl(cls));
                }
	    }

	    return result ;
	}

        @Override
	public String toString() 
	{
	    return "NodeImpl[" + interf + "]" ;
	}

        @Override
	public int hashCode()
	{
	    return interf.hashCode() ;
	}

        @Override
	public boolean equals( Object obj )
	{
	    if (this == obj) {
                return true;
            }

	    if (!(obj instanceof NodeImpl)) {
                return false;
            }

	    NodeImpl other = (NodeImpl)obj ;

	    return other.getInterface().equals( interf ) ;
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
