/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.orbutil.newtimer ;

import java.io.IOException ;
import java.io.File ;
import java.io.PrintStream ;

import org.xml.sax.SAXException ;
import org.xml.sax.Attributes ;

import org.xml.sax.helpers.DefaultHandler ;

import javax.xml.parsers.SAXParserFactory ;
import javax.xml.parsers.ParserConfigurationException ;
import javax.xml.parsers.SAXParser ;

import java.util.List ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.LinkedHashMap ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.Collections ;
import java.util.Properties ;

import java.io.IOException ;

import com.sun.corba.se.spi.orbutil.newtimer.Controllable ;
import com.sun.corba.se.spi.orbutil.newtimer.Timer ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerGroup ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerFactory ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerFactoryBuilder ;

import com.sun.corba.se.spi.orbutil.generic.Pair ;

import com.sun.corba.se.impl.codegen.Identifier ;
import com.sun.corba.se.impl.codegen.ASMUtil ;

import com.sun.corba.se.spi.codegen.Type ;
import com.sun.corba.se.spi.codegen.Expression ;
import com.sun.corba.se.spi.codegen.Signature ;
import com.sun.corba.se.spi.codegen.ImportList ;

import static java.lang.reflect.Modifier.* ;

import static com.sun.corba.se.spi.codegen.Wrapper.* ;

/** Class used to compile an XML description of timer and timergroup
 * information into a Java source file.  Uses the codegen library
 * to generate the source file.
 *
 * Note on bootstrapping: the time point file needs to be generated
 * before the bulk of the ORB is built.  This requires compiling
 * the newtimer and codegen classes into a library file that is 
 * checked into SCCS.
 */
public class TimerPointSourceGenerator {
    private static class TimingInfoProcessor {
	private boolean done = false ;
	private String pkg ;
	private TimerFactory tf ;
	private Map<String,List<String>> contents ;
	private TimerGroup currentTimerGroup ;

	private void checkForValidIdentifier( String name ) {
	    if (!Identifier.isValidIdentifier( name )) 
		throw new IllegalArgumentException(
		    "name " + name + " is not a valid Java identifier" ) ;
	}

	private void checkDone() {
	    if (done)
		throw new IllegalStateException(
		    "past getResult: no other methods may be called" ) ;
	}

	public TimingInfoProcessor( String name, String pkg ) {
	    this.done = false ;
	    this.pkg = pkg ;
	    checkForValidIdentifier( name ) ;
	    if (!Identifier.isValidFullIdentifier( pkg ))
		throw new IllegalArgumentException(
		    pkg + " is not a valid package name" ) ;
	    this.tf = TimerFactoryBuilder.make( name, name ) ;
	    this.contents = new LinkedHashMap<String,List<String>>() ;
	    this.currentTimerGroup = null ;
	}

	public void addTimer( String name, String desc ) {
	    checkDone() ;
	    checkForValidIdentifier( name ) ;
	    tf.makeTimer( name, desc ) ;
	    currentTimerGroup = null ;
	}

	public void addTimerGroup( String name, String desc ) {
	    checkDone() ;
	    checkForValidIdentifier( name ) ;
	    currentTimerGroup = tf.makeTimerGroup( name, desc ) ;
	}

	public void contains( String name ) {
	    checkDone() ;
	    if (currentTimerGroup == null) {
		throw new IllegalStateException(
		    "contains must be called after an addTimerGroup call" ) ;
	    } else {
		String cname = currentTimerGroup.name() ;
		List<String> list = contents.get( cname ) ;
		if (list == null) {
		    list = new ArrayList<String>() ;
		    contents.put( cname, list ) ;
		}

		list.add( name ) ;
	    }
	}

	private static Controllable getControllable( TimerFactory tf, 
	    String name ) {

	    Controllable result = tf.timers().get( name ) ;
	    if (result == null)
		result = tf.timerGroups().get( name ) ;
	    if (result == null)
		throw new IllegalArgumentException( 
		    name + " is not a valid Timer or TimerGroup name" ) ;
	    return result ;
	}

	private void updateTimerFactoryContents() {
	    //  Use the Map<String,List<String>> to fill in the TimerGroup
	    //  containment relation
	    for (String str : contents.keySet()) {
		List<String> list = contents.get(str) ;
		TimerGroup tg = tf.timerGroups().get( str ) ;
		for (String content : list) {
		    tg.add( getControllable( tf, content ) ) ;
		}
	    }
	}

	public Pair<String,TimerFactory> getResult() {
	    checkDone() ;
	    done = true ;
	    updateTimerFactoryContents() ;
	    Pair<String,TimerFactory> result = 
		new Pair<String,TimerFactory>( pkg, tf ) ;
	    return result ;
	}
    }

    private static class Handler extends DefaultHandler {
	private static final int WIDTH = 4 ;

	// Names of XML elements and attributes
	private static final String TIMER_ELEMENT = "timer" ;
	private static final String TIMING_ELEMENT = "timing" ;
	private static final String TIMER_GROUP_ELEMENT = "timerGroup" ;
	private static final String CONTAINS_ELEMENT = "contains" ;

	private static final String NAME_ATTR = "name" ;
	private static final String DESCRIPTION_ATTR = "description" ;
	private static final String PACKAGE_ATTR = "package" ;

	private boolean debug ;
	private int level ;
	private char[] pad ;
	private TimingInfoProcessor tip ;
	private Pair<String,TimerFactory> result ;

	public Handler( boolean debug ) {
	    this.debug = debug ;
	    this.level = 0 ;
	    setPad() ;
	    this.tip = null ;
	    this.result = null ;
	}

	private void indent() {
	    level++ ;
	    setPad() ;
	}

	private void undent() {
	    level-- ;
	    setPad() ;
	}

	private void setPad() {
	    int length = WIDTH * level ;
	    pad = new char[length] ;
	    for (int ctr=0; ctr<length; ctr++)
		pad[ctr] = ' ' ;
	}

	private void dprint( String msg ) {
	    if (debug) {
		System.out.print( pad ) ;
		System.out.println( msg ) ;
	    }
	}

	public void startDocument() throws SAXException {
	    dprint( "startDocument called" ) ;
	}

	public void endDocument() throws SAXException {
	    dprint( "endDocument called" ) ;
	    result = tip.getResult() ;
	}

	public void startElement( String namespaceURI,
	    String lName, String qName, Attributes attrs ) 
	    throws SAXException {

	    indent() ;

	    // only qName is useful
	    dprint( "namespaceURI=" + namespaceURI ) ;
	    dprint( "lName=" + lName ) ;
	    dprint( "qName=" + qName ) ;
	    dprint( "Attributes:" ) ;

	    // only local name, value are useful
	    for (int ctr=0; ctr<attrs.getLength(); ctr++) {
		dprint( "\tlocal name    =" + attrs.getLocalName(ctr) ) ;
		dprint( "\tqualified name=" + attrs.getQName(ctr) ) ;
		dprint( "\tvalue         =" + attrs.getValue(ctr) ) ;
	    }

	    if (qName.equals( TIMING_ELEMENT )) {
		String name = attrs.getValue( NAME_ATTR ) ;
		String pkg = attrs.getValue( PACKAGE_ATTR ) ;
		tip = new TimingInfoProcessor( name, pkg ) ;
	    } else if (qName.equals( TIMER_ELEMENT )) {
		String name = attrs.getValue( NAME_ATTR ) ;
		String desc = attrs.getValue( DESCRIPTION_ATTR ) ;
		tip.addTimer( name, desc ) ;
	    } else if (qName.equals( TIMER_GROUP_ELEMENT )) {
		String name = attrs.getValue( NAME_ATTR ) ;
		String desc = attrs.getValue( DESCRIPTION_ATTR ) ;
		tip.addTimerGroup( name, desc ) ;
	    } else if (qName.equals( CONTAINS_ELEMENT )) {
		String name = attrs.getValue( NAME_ATTR ) ;
		tip.contains( name ) ;
	    } else {
		throw new IllegalStateException(
		    "Unknown XML element: " + qName ) ;
	    }
	}

	public void endElement( String namespaceURI,
	    String lName, String qName ) throws SAXException {

	    undent() ;
	}

	public Pair<String,TimerFactory> getResult() {
	    return result ;
	}
    }

    public static Pair<String,TimerFactory> parseDescription( 
	String fileName ) throws IOException {
	return parseDescription( fileName, false ) ;
    }

    /** Return the package for the file to generate and a TimerFactory
     * that contains all of the information from the XML source file
     * given by fileName.
     */
    public static Pair<String,TimerFactory> parseDescription( 
	String fileName, boolean debug ) throws IOException {

	Handler handler = new Handler( debug ) ;
	SAXParserFactory factory = SAXParserFactory.newInstance() ;
	factory.setValidating( true ) ;
	File file = null ;

	try {
	    SAXParser saxParser = factory.newSAXParser() ;
	    file = new File( fileName ) ;
	    saxParser.parse( file, handler ) ;
	} catch (Exception exc) {
	    System.out.println( "Exception in processing " 
		+ file + ": " + exc ) ;
	    exc.printStackTrace() ;
	    return null ;
	}

	return handler.getResult() ;
    }

    private static Type Controllable ;
    private static Type TimerManager ;
    private static Type TimerFactory ; 
    private static Type Timer ; 
    private static Type TimerEventController ;
    private static Type TimerGroup ; 
    private static ImportList standardImports ;

    private static Type generatedInterfaceType ;
    private static Type generatedBaseClassType ;

    static {
	_clear() ;
	_package() ;

	Controllable = _import( 
	    "com.sun.corba.se.spi.orbutil.newtimer.Controllable" ) ;
	TimerManager = _import( 
	    "com.sun.corba.se.spi.orbutil.newtimer.TimerManager" ) ;
	TimerFactory = _import( 
	    "com.sun.corba.se.spi.orbutil.newtimer.TimerFactory" ) ;
	Timer = _import( 
	    "com.sun.corba.se.spi.orbutil.newtimer.Timer" ) ;
	TimerEventController = _import( 
	    "com.sun.corba.se.spi.orbutil.newtimer.TimerEventController" ) ;
	TimerGroup = _import( 
	    "com.sun.corba.se.spi.orbutil.newtimer.TimerGroup" ) ;

	standardImports = _import() ;
    }

    /** Generate the source files for the Timers in the TimerFactory from the 
     * description.  Three files are generated: 
     * <ul>
     * <li>[name], which is an interface that defines all of the Timer 
     * and TimerGroup accessor methods, and the Timer enter/exit methods
     * <li>[name]Base, which is an abstract base class that implements
     * [name] and contains the accessor method implementations
     * <li>[name]DisabledImpl, which is an implementation of [name] 
     * that returns null for all Timer and TimerGroup accessor methods, 
     * and the enter/exit methods do nothing
     * <li>[name]EnabledImpl, which is the full implementation of [name] that 
     * is used * for timing.
     * </ul>
     * The file is generated in the directory given by the package from 
     * the description starting at the dirName.  The name of the file is 
     * NAME.java, where NAME is the TimerFactory name from the description.
     */
    public static void generateSourceFiles( String dirName, 
	Pair<String,TimerFactory> description ) throws IOException {
	
	String packageName = description.first() ;
	TimerFactory tf = description.second() ;

	generateInterface( dirName, packageName, tf ) ;
	generateBaseClass( dirName, packageName, tf ) ;
	generateImpl( false, dirName, packageName, tf ) ;
	generateImpl( true, dirName, packageName, tf ) ;
    }

    private static void generateInterface( String dirName,
	String packageName, TimerFactory tf ) throws IOException {

	startFile( packageName ) ;

	_interface( PUBLIC, tf.name() ) ;

	generateAccessorMethods( tf, false ) ;
	generateEnterExitMethods( tf, GenerationType.METHOD_HEADER ) ;

	_end() ;

	generatedInterfaceType = Type._classGenerator( _classGenerator() ) ;

	writeFile( dirName, generatedInterfaceType ) ;
    }

    private static void generateBaseClass( String dirName, 
	String packageName, TimerFactory tf ) throws IOException {

	startFile( packageName ) ;
	_import( generatedInterfaceType.name() ) ;

	String baseClassName = tf.name() + "Base" ;

	_class( PUBLIC|ABSTRACT, baseClassName, _Object(), generatedInterfaceType ) ; 

	generateFields( tf ) ;
	generateConstructor( tf ) ;
	generateAccessorMethods( tf, true ) ;

	_end() ;

	generatedBaseClassType = Type._classGenerator( _classGenerator() ) ;

	writeFile( dirName, generatedBaseClassType ) ;
    }

    private static void generateImpl( boolean isNoop, String dirName, 
	String packageName, TimerFactory tf ) throws IOException {

	startFile( packageName ) ;
	_import( generatedBaseClassType.name() ) ;

	String className ;
	if (isNoop) {
	    className = tf.name() + "DisabledImpl" ;
	} else {
	    className = tf.name() + "EnabledImpl" ;
	}

	_class( PUBLIC, className, generatedBaseClassType ) ; 

	// Constructor just calls base class constructor
	_constructor( PUBLIC ) ;
	    Expression tfe = _arg( TimerFactory, "tf" ) ;
	    Expression tec = _arg( TimerEventController, "tec" ) ;
	_body() ;
	    _expr(_super( tfe, tec )) ;
	_end() ;
	
	generateEnterExitMethods( tf, 
	    isNoop ? GenerationType.DISABLED_METHOD 
		   : GenerationType.ENABLED_METHOD ) ;

	_end() ; // of class generation

	Type thisClass = Type._classGenerator( _classGenerator() ) ;

	writeFile( dirName, thisClass ) ;
    }
    
    private static void generateAccessorMethods( TimerFactory tf, boolean isImpl ) {
	int modifiers = isImpl ? (PUBLIC|FINAL) : (PUBLIC|ABSTRACT) ;

	for (Timer t : tf.timers().values()) {
	    _method( modifiers, Timer, t.name()) ;
	    if (isImpl) {
		_body() ;
		_return(_field(_this(), t.name())) ; 
	    }
	    _end() ;
	}

	for (TimerGroup tg : tf.timerGroups().values()) {
	    _method( modifiers, TimerGroup, tg.name()) ;
	    if (isImpl) {
		_body() ;
		_return(_field(_this(), tg.name())) ; 
	    }
	    _end() ;
	}

    }

    private static void startFile( String packageName ) {
	_clear() ;
	_package( packageName ) ;
	_import( standardImports ) ;
    }

    private static void writeFile( String dirName, 
	Type type ) throws IOException {

	File file = ASMUtil.getFile( dirName, type.name(), 
	    ".java" ) ;

	PrintStream ps = new PrintStream( file ) ; 
	try {
	    _sourceCode( ps, new Properties() ) ;
	} finally {
	    ps.close() ;
	}
    }

    private static void generateFields( TimerFactory tf ) {
	_data( PROTECTED|FINAL, TimerEventController, "controller" ) ;

	for (Timer t : tf.timers().values()) {
	    _data( PROTECTED|FINAL, Timer, t.name() ) ;
	}

	for (TimerGroup tg : tf.timerGroups().values()) {
	    _data( PRIVATE|FINAL, TimerGroup, tg.name() ) ;
	}
    }

    private static void generateConstructor( TimerFactory tf ) {
	_constructor( PUBLIC ) ;
	    Expression tfe = _arg( TimerFactory, "tf" ) ;
	    Expression controller = _arg( TimerEventController, 
		"controller" ) ;
	_body() ;

	// set up orb and tm
	_assign( _field( _this(), "controller" ), controller ) ;

	// create all timers
	for (Timer t : tf.timers().values() ) {
	    _assign( _v( t.name() ), 
		_call( tfe, "makeTimer", 
		    _const(t.name()), _const(t.description()))) ;
	}
    
	// create all timer groups
	for (TimerGroup tg : tf.timerGroups().values() ) {
	    _assign( _v( tg.name() ), 
		_call( tfe, "makeTimerGroup", 
		    _const(tg.name()), _const(tg.description()))) ;
	}
	
	// fill in timer group containment
	// Signature addSig = _s( _boolean(), _t("Controllable")) ;
	for (TimerGroup tg : tf.timerGroups().values() ) {
	    for (Controllable c : tg.contents() ) {
		_expr( 
		    _call( _v(tg.name()), "add", // addSig,
			_v(c.name()))) ;
	    }
	}

	_end() ;
    }

    enum GenerationType { METHOD_HEADER, ENABLED_METHOD, DISABLED_METHOD } ;

    private static void generateEnterExitMethods( TimerFactory tf, 
	GenerationType gtype ) {
	
	// generate enter and exit method for each Timer
	for (Timer t : tf.timers().values() ) {
	    generateMethod( "enter", t.name(), gtype ) ;
	    generateMethod( "exit", t.name(), gtype ) ;
	}
    }

    // XXX we should add JavaDoc support to codegen, and use it
    // for the enter/exit methods.
    private static void generateMethod( String op, String timer, 
	GenerationType gtype ) {

	int modifiers = (gtype == GenerationType.METHOD_HEADER) ?
	    (PUBLIC|ABSTRACT) :
	    PUBLIC ;

	_method( modifiers, _void(), op + "_" + timer ) ;

	if (gtype != GenerationType.METHOD_HEADER) {
	    _body() ;
	    if (gtype == GenerationType.ENABLED_METHOD) {
		_expr( _call( _v("controller"), op, 
		    _v(timer))) ;
	    }
	}

	_end() ;
    }

    public static void main( String[] args ) {
	// arguments: infile outdir debug
	if (args.length != 3) {
	    System.out.println( 
		"Required arguments: input-file output-directory" ) ;
	    System.exit( 1 ) ;
	} else {
	    try {
		String infile = args[0] ;
		String outdir = args[1] ;
		boolean debug = Boolean.parseBoolean( args[2] ) ;
		Pair<String,TimerFactory> result = parseDescription( infile,
		    debug ) ;
		generateSourceFiles( outdir, result ) ;
	    } catch (Exception exc) {
		System.out.println( "Failed with exception: " + exc ) ;
		exc.printStackTrace() ;
		System.exit( 1 ) ;
	    }
	}
    }
}
