;  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
; 
;  Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
; 
;  The contents of this file are subject to the terms of either the GNU
;  General Public License Version 2 only ("GPL") or the Common Development
;  and Distribution License("CDDL") (collectively, the "License").  You
;  may not use this file except in compliance with the License. You can obtain
;  a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
;  or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
;  language governing permissions and limitations under the License.
; 
;  When distributing the software, include this License Header Notice in each
;  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
;  Sun designates this particular file as subject to the "Classpath" exception
;  as provided by Sun in the GPL Version 2 section of the License file that
;  accompanied this code.  If applicable, add the following below the License
;  Header, with the fields enclosed by brackets [] replaced by your own
;  identifying information: "Portions Copyrighted [year]
;  [name of copyright owner]"
; 
;  Contributor(s):
; 
;  If you wish your version of this file to be governed by only the CDDL or
;  only the GPL Version 2, indicate your decision by adding "[Contributor]
;  elects to include this software in this distribution under the [CDDL or GPL
;  Version 2] license."  If you don't indicate a single choice of license, a
;  recipient has the option to distribute your version of this file under
;  either the CDDL, the GPL Version 2 or to extend the choice of license to
;  its licensees as provided above.  However, if you add GPL Version 2 code
;  and therefore, elected the GPL Version 2 license, then the option applies
;  only if the new code is made subject to such option by the copyright
;  holder.

("com.sun.corba.se.impl.logging" "ORBUtilSystemException" ORBUTIL
    (
	(BAD_OPERATION 
	    (ADAPTER_ID_NOT_AVAILABLE 
	     1 WARNING "Adapter ID not available")
	    (SERVER_ID_NOT_AVAILABLE
	     2 WARNING "Server ID not available")
	    (ORB_ID_NOT_AVAILABLE
	     3 WARNING "ORB ID not available")
	    (OBJECT_ADAPTER_ID_NOT_AVAILABLE
	     4 WARNING "Object adapter ID not available")
	    (CONNECTING_SERVANT
	     5 WARNING "Error connecting servant")
	    (EXTRACT_WRONG_TYPE
	     6 FINE "Expected typecode kind {0} but got typecode kind {1}")
	    (EXTRACT_WRONG_TYPE_LIST
	     7 WARNING "Expected typecode kind to be one of {0} but got typecode kind {1}")
	    (BAD_STRING_BOUNDS
	     8 WARNING "String length of {0} exceeds bounded string length of {1}")
	    (INSERT_OBJECT_INCOMPATIBLE
	     10 WARNING "Tried to insert an object of an incompatible type into an Any for an object reference")
	    (INSERT_OBJECT_FAILED
	     11 WARNING "insert_Object call failed on an Any")
	    (EXTRACT_OBJECT_INCOMPATIBLE
	     12 WARNING "extract_Object call failed on an Any")
	    (FIXED_NOT_MATCH
	     13 WARNING "Fixed type does not match typecode")
	    (FIXED_BAD_TYPECODE 
	     14 WARNING "Tried to insert Fixed type for non-Fixed typecode")
	    (SET_EXCEPTION_CALLED_NULL_ARGS
	     23 WARNING "set_exception(Any) called with null args for DSI ServerRequest")
	    (SET_EXCEPTION_CALLED_BAD_TYPE
	     24 WARNING "set_exception(Any) called with a bad (non-exception) type")
	    (CONTEXT_CALLED_OUT_OF_ORDER
	     25 WARNING "ctx() called out of order for DSI ServerRequest")
	    (BAD_ORB_CONFIGURATOR
	     26 WARNING "ORB configurator class {0} could not be instantiated")
	    (ORB_CONFIGURATOR_ERROR
	     27 WARNING "Error in running ORB configurator")
	    (ORB_DESTROYED
	     28 WARNING "This ORB instance has been destroyed, so no operations can be performed on it")
	    (NEGATIVE_BOUNDS
	     29 WARNING "Negative bound for string TypeCode is illegal")
	    (EXTRACT_NOT_INITIALIZED
	     30 WARNING "Called typecode extract on an uninitialized typecode")
	    (EXTRACT_OBJECT_FAILED
	     31 WARNING "extract_Object failed on an uninitialized Any")
	    (METHOD_NOT_FOUND_IN_TIE
	     32 FINE "Could not find method named {0} in class {1} in reflective Tie")
	    (CLASS_NOT_FOUND1
	     33 FINE "ClassNotFoundException while attempting to load preferred stub named {0}")
	    (CLASS_NOT_FOUND2
	     34 FINE "ClassNotFoundException while attempting to load alternate stub named {0}")
	    (CLASS_NOT_FOUND3
	     35 FINE "ClassNotFoundException while attempting to load interface {0}")
	    (GET_DELEGATE_SERVANT_NOT_ACTIVE
	     36 WARNING "POA ServantNotActive exception while trying get an org.omg.CORBA.Portable.Delegate for an org.omg.PortableServer.Servant")
	    (GET_DELEGATE_WRONG_POLICY
	     37 WARNING "POA WrongPolicy exception while trying get an org.omg.CORBA.Portable.Delegate for an org.omg.PortableServer.Servant")
	    (SET_DELEGATE_REQUIRES_STUB
	     38 FINE "Call to StubAdapter.setDelegate did not pass a stub")
	    (GET_DELEGATE_REQUIRES_STUB
	     39 WARNING "Call to StubAdapter.getDelegate did not pass a stub")
	    (GET_TYPE_IDS_REQUIRES_STUB
	     40 WARNING "Call to StubAdapter.getTypeIds did not pass a stub")
	    (GET_ORB_REQUIRES_STUB
	     41 WARNING "Call to StubAdapter.getORB did not pass a stub")
	    (CONNECT_REQUIRES_STUB
	     42 WARNING "Call to StubAdapter.connect did not pass a stub")
	    (IS_LOCAL_REQUIRES_STUB
	     43 WARNING "Call to StubAdapter.isLocal did not pass a stub")
	    (REQUEST_REQUIRES_STUB
	     44 WARNING "Call to StubAdapter.request did not pass a stub")
	    (BAD_ACTIVATE_TIE_CALL
	     45 WARNING "Call to StubAdapter.activateTie did not pass a valid Tie")
	    (BAD_OPERATION_FROM_INVOKE
	     46 WARNING "Bad operation from _invoke: {0}")
	    (COULD_NOT_ACCESS_STUB_DELEGATE
	     47 WARNING "Could not access StubDelegateImpl")
	    (COULD_NOT_LOAD_INTERFACE
	     48 WARNING "Could not load interface {0} for creating stub")
	    (ADAPTER_INACTIVE_IN_ACTIVATE_SERVANT
	     49 WARNING "Could not activate POA from foreign ORB due to AdapterInactive exception in StubAdapter")
	    (COULD_NOT_INSTANTIATE_STUB_CLASS 
	     50 WARNING "Could not instantiate stub class {0} for dynamic RMI-IIOP")
	    (STRING_EXPECTED_IN_OPERATION
	     51 WARNING "String expected in OperationFactory.getString()")
	    (OBJECT_ARRAY_EXPECTED
	     52 WARNING "Object[] expected")
	    (PAIR_STRING_STRING_EXPECTED
	     53 WARNING "Pair<String,String> expected")
	    (CLASS_ACTION_EXCEPTION
	     54 WARNING "Error while attempting to load class {0}")
	    (BAD_URL_IN_ACTION
	     55 WARNING "Bad URL {0} in URLAction")
	    (VALUE_NOT_IN_RANGE
	     56 WARNING "Property value {0} is not in the range {1} to {2}")
	    (NUM_TOKENS_ACTIONS_DONT_MATCH
	     57 WARNING "Number of token ({0}) and number of actions ({1}) don't match")
	    (EXCEPTION_IN_CONVERT_ACTION_CONSTRUCTOR
	     58 WARNING "Could not find constructor <init>(String) in class {0}")
	    (EXCEPTION_IN_CONVERT_ACTION
	     59 WARNING "Exception in ConvertAction operation")
            (IO_EXCEPTION_ON_CLOSE
             60 FINE "Useless exception on call to Closeable.close()")
            (CLASS_NOT_FOUND_IN_BUNDLE
             61 FINE "Bundle not found for class {0}")
            (FOUND_CLASS_IN_BUNDLE
             62 FINE "Class {0} found in bundle {1}")
            (BUNDLE_COULD_NOT_LOAD_CLASS
             63 FINE "Class {0} could not be loaded by bundle {1}")
            (FOUND_CLASS_IN_BUNDLE_VERSION
             64 FINE "Class {0} found in bundle {1} with version {2}")
            (CLASS_NOT_FOUND_IN_BUNDLE_VERSION
             65 FINE "Class {0} not found in bundle {1} with version {2}")
            (INSERT_ORB_PROVIDER
             66 FINE "Inserting ORB provider class {0} from bundle {1}")
            (REMOVE_ORB_PROVIDER
             67 FINE "Inserting ORB provider class {0} from bundle {1}")
            (RECEIVED_BUNDLE_EVENT
             68 FINE "Received bundle event of type {0} on bundle {1}")
            (PROBE_BUNDLES_FOR_PROVIDERS
             69 FINE "Probing all bundles for ORB class providers")
            (CLASS_NOT_FOUND_IN_CLASS_NAME_MAP
             70 FINE "Could not find class {0} in classNameMap")
            (CLASS_NOT_FOUND_IN_PACKAGE_NAME_MAP
             71 FINE "Could not find class {0} in packageNameMap")
            (CLASS_FOUND_IN_PACKAGE_NAME_MAP
             72 FINE "Found class {0} in bundle {1} using packageNameMap")
            (CLASS_FOUND_IN_CLASS_NAME_MAP
             73 FINE "Found class {0} in bundle {1} using classNameMap")
            (COULD_NOT_LOAD_CLASS_IN_BUNDLE
             74 WARNING "Could not load class {0} in bundle {1}")
            (EXCEPTION_IN_SELECTOR
             75 WARNING "Exception while handling event on {0}")
            (CANCELED_SELECTION_KEY 
             76 FINE "Ignoring cancelled SelectionKey {0}: key will be removed from Selector")
            (PACKAGE_ADMIN_SERVICE_NOT_AVAILABLE
             77 FINE "The OSGi PackageAdmin service is not available")
            (SET_PARAMETER_CALLED_AGAIN
             78 WARNING "The ORBImpl.set_parameters method was called more than once")
            (INSERT_BUNDLE_PACKAGE
             79 FINE "Inserting exported package {0} from bundle {1} into packageNameMap")
            (REMOVE_BUNDLE_PACKAGE
             80 FINE "Removing exported package {0} from bundle {1} from packageNameMap") 
	    )
	(BAD_PARAM
	    (NULL_PARAM 
	     1 WARNING "Null parameter")
	    (UNABLE_FIND_VALUE_FACTORY
	     2 FINE "Unable to find value factory")
	    (ABSTRACT_FROM_NON_ABSTRACT
	     3 WARNING "Abstract interface derived from non-abstract interface")
	    (INVALID_TAGGED_PROFILE 
	     4 WARNING "Error in reading IIOP TaggedProfile")
	    (OBJREF_FROM_FOREIGN_ORB 
	     5 FINE "Object reference came from foreign ORB")
	    (LOCAL_OBJECT_NOT_ALLOWED 
	     6 FINE "Local object not allowed")
	    (NULL_OBJECT_REFERENCE 
	     7 WARNING "null object reference")
	    (COULD_NOT_LOAD_CLASS 
	     8 WARNING "Could not load class {0}")
	    (BAD_URL 
	     9 WARNING "Malformed URL {0}")
	    (FIELD_NOT_FOUND 
	     10 WARNING "Field {0} not found in parser data object")
	    (ERROR_SETTING_FIELD 
	     11 WARNING "Error in setting field {0} to value {1} in parser data object")
	    (BOUNDS_ERROR_IN_DII_REQUEST 
	     12 WARNING "Bounds error occurred in DII request")
	    (PERSISTENT_SERVER_INIT_ERROR 
	     13 WARNING "Initialization error for persistent server")
	    (COULD_NOT_CREATE_ARRAY 
	     14 WARNING "Could not create array for field {0} with component type {1} and size {2}")
	    (COULD_NOT_SET_ARRAY 
	     15 WARNING "Could not set array for field {0} at index {1} with component type {2} and size {3} to value {4}")
	    (ILLEGAL_BOOTSTRAP_OPERATION 
	     16 WARNING "Illegal bootstrap operation {0}")
	    (BOOTSTRAP_RUNTIME_EXCEPTION 
	     17 WARNING "Runtime Exception during bootstrap operation")
	    (BOOTSTRAP_EXCEPTION 
	     18 WARNING "Exception during bootstrap operation")
	    (STRING_EXPECTED 
	     19 WARNING "Expected a string, but argument was not of String type")
	    (INVALID_TYPECODE_KIND 
	     20 WARNING "{0} does not represent a valid kind of typecode")
	    (SOCKET_FACTORY_AND_CONTACT_INFO_LIST_AT_SAME_TIME
	     21 WARNING "cannot have a SocketFactory and a ContactInfoList at the same time")
	    (ACCEPTORS_AND_LEGACY_SOCKET_FACTORY_AT_SAME_TIME
	     22 WARNING "cannot have Acceptors and a legacy SocketFactory at the same time")
	    (BAD_ORB_FOR_SERVANT 
	     23 WARNING "Reflective POA Servant requires an instance of org.omg.CORBA_2_3.ORB")
	    (INVALID_REQUEST_PARTITIONING_POLICY_VALUE 
	     24 WARNING "Request partitioning value specified, {0}, is outside supported range, {1} - {2}")
	    (INVALID_REQUEST_PARTITIONING_COMPONENT_VALUE 
	     25 WARNING "Could not set request partitioning component value to {0}, valid values are {1} - {2}")
	    (INVALID_REQUEST_PARTITIONING_ID
	     26 WARNING "Invalid request partitioning id {0}, valid values are {1} - {2}")
	    (ERROR_IN_SETTING_DYNAMIC_STUB_FACTORY_FACTORY 
	     27 FINE "ORBDynamicStubFactoryFactoryClass property had value {0}, which could not be loaded by the ORB ClassLoader" )
	    (REGISTER_DUPLICATE_SERVICE_CONTEXT 
	     28 WARNING "An attempt was made to register a ServiceContext.Factory with an ID that is already registered")
	    (NOT_AN_OBJECT_IMPL
	     29 WARNING "CORBA object is not an ObjectImpl in ORB.getIOR")
	    (BAD_TIMEOUT_STRING_DATA
	     30 WARNING "{0} is not a valid positive decimal integer for {1}")
	    (BAD_TIMEOUT_DATA_LENGTH
	     31 WARNING "Timeout data must be 3 or 4 positive decimal integers separated by :")
	    (INVALID_LOAD_BALANCING_POLICY_VALUE 
	     32 WARNING "Load balancing value specified, {0}, is outside supported range, {1} - {2}")
	    (INVALID_LOAD_BALANCING_COMPONENT_VALUE 
	     33 WARNING "Could not set load balancing component value to {0}, valid values are {1} - {2}")
	    (INVALID_LOAD_BALANCING_ID
	     34 WARNING "Invalid request partitioning id {0}, valid values are {1} - {2}")
            (CODE_BASE_UNAVAILABLE 
             35 FINE "CodeBase unavailable on connection {0}")
	    )
	(BAD_INV_ORDER 
	    (DSIMETHOD_NOTCALLED 
	     1 WARNING "DSI method not called")
	    (ARGUMENTS_CALLED_MULTIPLE 
	     2 WARNING "arguments(NVList) called more than once for DSI ServerRequest")
	    (ARGUMENTS_CALLED_AFTER_EXCEPTION 
	     3 WARNING "arguments(NVList) called after exceptions set for DSI ServerRequest")
	    (ARGUMENTS_CALLED_NULL_ARGS 
	     4 WARNING "arguments(NVList) called with null args for DSI ServerRequest")
	    (ARGUMENTS_NOT_CALLED 
	     5 FINE "arguments(NVList) not called for DSI ServerRequest")
	    (SET_RESULT_CALLED_MULTIPLE 
	     6 WARNING "set_result(Any) called more than once for DSI ServerRequest")
	    (SET_RESULT_AFTER_EXCEPTION 
	     7 FINE "set_result(Any) called exception was set for DSI ServerRequest")
	    (SET_RESULT_CALLED_NULL_ARGS 
	     8 WARNING "set_result(Any) called with null args for DSI ServerRequest"))
	(BAD_TYPECODE
	    (BAD_REMOTE_TYPECODE 
	     1 WARNING "Foreign to native typecode conversion constructor should not be called with native typecode")
	    (UNRESOLVED_RECURSIVE_TYPECODE 
	     2 WARNING "Invoked operation on unresolved recursive TypeCode"))
	(COMM_FAILURE 
	    (CONNECT_FAILURE
	     1  FINE "Connection failure: socketType: {0}; hostname: {1}; port: {2}")
	    (WRITE_ERROR_SEND
	     3  FINE "Write error sent")
	    (GET_PROPERTIES_ERROR
	     4  WARNING "Get properties error")
	    (BOOTSTRAP_SERVER_NOT_AVAIL
	     5  WARNING "Bootstrap server is not available")
	    (INVOKE_ERROR
	     6  WARNING "Invocation error")
	    (DEFAULT_CREATE_SERVER_SOCKET_GIVEN_NON_IIOP_CLEAR_TEXT
	     7 WARNING "DefaultSocketFactory.createServerSocket only handles IIOP_CLEAR_TEXT, given {0}")
	    (CONNECTION_ABORT
	     8 FINE "Connection abort")
	    (CONNECTION_REBIND
	     9 FINE "Connection rebind")
	    (RECV_MSG_ERROR
	     10 WARNING "Received a GIOP MessageError, indicating header corruption or version mismatch")
	    (IOEXCEPTION_WHEN_READING_CONNECTION
	     11 FINE "IOException received when reading from connection {0}")
	    (SELECTION_KEY_INVALID
	     12 FINE "SelectionKey invalid on channel, {0}")
    	    (EXCEPTION_IN_ACCEPT
	     13 FINE "Unexpected {0} in accept")
   	    (SECURITY_EXCEPTION_IN_ACCEPT
	     14 FINE "Unexpected exception, has permissions {0}")
	    (TRANSPORT_READ_TIMEOUT_EXCEEDED
	     15 WARNING "Read of full message failed : bytes requested = {0} bytes read = {1} max wait time = {2} total time spent waiting = {3}")
	    (CREATE_LISTENER_FAILED 
	     16 SEVERE "Unable to create IIOP listener on the specified host/port: {0}/{1}")
	    (THROWABLE_IN_READ_BITS 
	     17 FINE "Throwable received in ReadBits")
	    (IOEXCEPTION_IN_ACCEPT
	     18 WARNING "IOException in accept")
	    (COMMUNICATIONS_TIMEOUT_WAITING_FOR_RESPONSE
	     19 WARNING "Communications timeout waiting for response.  Exceeded {0} milliseconds")
	    (COMMUNICATIONS_RETRY_TIMEOUT
	     20 WARNING "Communications retry timeout.  Exceeded {0} milliseconds")
	    (IGNORING_EXCEPTION_WHILE_WAITING_FOR_RETRY
	     21 FINE "Ignoring exception while waiting for retry")
            (TEMPORARY_WRITE_SELECTOR_WITH_BLOCKING_CONNECTION
             22 SEVERE "Invalid request for a temporary write selector object for use on a blocking connection: {0}.")
            (TEMPORARY_READ_SELECTOR_WITH_BLOCKING_CONNECTION
             23 SEVERE "Invalid request for a temporary read selector object for use on a blocking connection: {0}.")
            (TEMPORARY_SELECTOR_SELECT_TIMEOUT_LESS_THAN_ONE
             24 SEVERE "TemporarySelector's Selector, {0} .select(timeout) must called with timeout value greater than 0, called with a timeout value of, {1}.")
	    (TRANSPORT_WRITE_TIMEOUT_EXCEEDED
	     25 WARNING "Write of message exceeded TCP timeout : max wait time = {0} ms, total time spent blocked, waiting to write = {1} ms.")
            (EXCEPTION_WHEN_READING_WITH_TEMPORARY_SELECTOR
	     26 SEVERE "Unexpected exception when reading with a temporary selector: bytes read = {0}, bytes requested = {1}, time spent waiting = {2} ms, max time to wait = {3}.")
            (EXCEPTION_WHEN_WRITING_WITH_TEMPORARY_SELECTOR
	     27 SEVERE "Unexpected exception when writing with a temporary selector:  bytes written = {0}, total bytes requested to write = {1}, time spent waiting = {2} ms, max time to wait = {3}.")
            (THROWABLE_IN_DO_OPTIMIZED_READ_STRATEGY
	     28 FINE "Throwable received in doOptimizedReadStrategy")
	    (BLOCKING_READ_TIMEOUT
	     29 WARNING "Blocking read failed, expected to read additional bytes:  max wait time = {0}ms total time spent waiting = {1}ms")
            (EXCEPTION_BLOCKING_READ_WITH_TEMPORARY_SELECTOR
	     30 FINE "Exception in a blocking read on connection {0} with a temporary selector")
            (NON_BLOCKING_READ_ON_BLOCKING_SOCKET_CHANNEL
	     31 SEVERE "Invalid operation, attempting a non-blocking read on blocking connection, {0}")
            (UNEXPECTED_EXCEPTION_CANCEL_AND_FLUSH_TEMP_SELECTOR
	     32 FINE "Unexpected exception when canceling SelectionKey and flushing temporary Selector")
            (MAXIMUM_READ_BYTE_BUFFER_SIZE_EXCEEDED
	     33 WARNING "Ignoring request to read a message which exceeds read size threshold of {0} bytes, requested size was {1}. Use ORB property -D{2}=<# of bytes> to set threshold higher.")
            (BLOCKING_READ_END_OF_STREAM
	     34 FINE "Received {0}, in a blocking read on connection, {1}, because an 'end of stream' was detected")
            (NONBLOCKING_READ_END_OF_STREAM
	     35 FINE "Received {0}, in a non-blocking read on connection, {1}, because an 'end of stream' was detected")
	    (IOEXCEPTION_IN_ACCEPT_FINE
	     36 FINE "IOException in accept")
	    (BUFFER_READ_MANAGER_TIMEOUT
	     37 WARNING "Timeout while reading data in buffer manager")
	    )
	(DATA_CONVERSION
	    (BAD_STRINGIFIED_IOR_LEN 
	     1  WARNING "A character did not map to the transmission code set")
	    (BAD_STRINGIFIED_IOR 
	     2  WARNING "Bad stringified IOR")
	    (BAD_MODIFIER 
	     3 WARNING "Unable to perform resolve_initial_references due to bad host or port configuration")
	    (CODESET_INCOMPATIBLE 
	     4 WARNING "Codesets incompatible")
	    (BAD_HEX_DIGIT 
	     5  WARNING "Illegal hexadecimal digit")
	    (BAD_UNICODE_PAIR 
	     6 WARNING "Invalid unicode pair detected during code set conversion")
	    (BTC_RESULT_MORE_THAN_ONE_CHAR 
	     7 WARNING "Tried to convert bytes to a single java char, but conversion yielded more than one Java char (Surrogate pair?)")
	    (BAD_CODESETS_FROM_CLIENT 
	     8 WARNING "Client sent code set service context that we do not support")
	    (INVALID_SINGLE_CHAR_CTB 
	     9 WARNING "Char to byte conversion for a CORBA char resulted in more than one byte")
	    (BAD_GIOP_1_1_CTB 
	     10 WARNING "Character to byte conversion did not exactly double number of chars (GIOP 1.1 only)")
	    (BAD_SEQUENCE_BOUNDS 
	     12 WARNING "Tried to insert a sequence of length {0} into a bounded sequence of maximum length {1} in an Any")
	    (ILLEGAL_SOCKET_FACTORY_TYPE 
	     13 WARNING "Class {0} is not a subtype of ORBSocketFactory")
	    (BAD_CUSTOM_SOCKET_FACTORY 
	     14 WARNING "{0} is not a valid custom socket factory")
	    (FRAGMENT_SIZE_MINIMUM 
	     15 WARNING "Fragment size {0} is too small: it must be at least {1}")
	    (FRAGMENT_SIZE_DIV 
	     16 WARNING "Illegal valiue for fragment size ({0}): must be divisible by {1}")
	    (ORB_INITIALIZER_FAILURE
	     17 WARNING "Could not instantiate ORBInitializer {0}")
	    (ORB_INITIALIZER_TYPE
	     18 WARNING "orb initializer class {0} is not a subtype of ORBInitializer")
	    (ORB_INITIALREFERENCE_SYNTAX
	     19 WARNING "Bad syntax for ORBInitialReference")
	    (ACCEPTOR_INSTANTIATION_FAILURE 
	     20 WARNING "Could not instantiate Acceptor {0}")
	    (ACCEPTOR_INSTANTIATION_TYPE_FAILURE 
	     21 WARNING "Acceptor class {0} is not a subtype of Acceptor")
	    (ILLEGAL_CONTACT_INFO_LIST_FACTORY_TYPE
	     22 WARNING "Class {0} is not a subtype of CorbaContactInfoListFactory")
	    (BAD_CONTACT_INFO_LIST_FACTORY
	     23 WARNING "{0} is not a valid CorbaContactInfoListFactory")
	    (ILLEGAL_IOR_TO_SOCKET_INFO_TYPE
	     24 WARNING "Class {0} is not a subtype of IORToSocketInfo")
	    (BAD_CUSTOM_IOR_TO_SOCKET_INFO
	     25 WARNING "{0} is not a valid custom IORToSocketInfo")
	    (ILLEGAL_IIOP_PRIMARY_TO_CONTACT_INFO_TYPE
	     26 WARNING "Class {0} is not a subtype of IIOPPrimaryToContactInfo")
	    (BAD_CUSTOM_IIOP_PRIMARY_TO_CONTACT_INFO
	     27 WARNING "{0} is not a valid custom IIOPPrimaryToContactInfo")
	    )
	(INV_OBJREF 
	    (BAD_CORBALOC_STRING 
	     1  WARNING "Bad corbaloc: URL")
	    (NO_PROFILE_PRESENT 
	     2  WARNING "No profile in IOR"))
	(INITIALIZE 
	    (CANNOT_CREATE_ORBID_DB  
	     1 WARNING "Cannot create ORB ID datastore")
	    (CANNOT_READ_ORBID_DB    
	     2 WARNING "Cannot read ORB ID datastore")
	    (CANNOT_WRITE_ORBID_DB   
	     3 WARNING "Cannot write ORB ID datastore")
	    (GET_SERVER_PORT_CALLED_BEFORE_ENDPOINTS_INITIALIZED 
	     4 WARNING "legacyGetServerPort called before endpoints initialized")
	    (PERSISTENT_SERVERPORT_NOT_SET  
	     5 FINE "Persistent server port is not set")
	    (PERSISTENT_SERVERID_NOT_SET  
	     6 FINE "Persistent server ID is not set")
	    (USER_CONFIGURATOR_EXCEPTION
	     7 WARNING "Exception occured while running a user configurator")
	    )
	(INTERNAL 
	    (NON_EXISTENT_ORBID 
	     1 WARNING "Non-existent ORB ID")
	    (NO_SERVER_SUBCONTRACT 
	     2  WARNING "No server request dispatcher")
	    (SERVER_SC_TEMP_SIZE 
	     3  WARNING "server request dispatcher template size error")
	    (NO_CLIENT_SC_CLASS 
	     4  WARNING "No client request dispatcher class")	
	    (SERVER_SC_NO_IIOP_PROFILE 
	     5  WARNING "No IIOP profile in server request dispatcher")	
	    (GET_SYSTEM_EX_RETURNED_NULL 
	     6 WARNING "getSystemException returned null")
	    (PEEKSTRING_FAILED 
	     7  WARNING "The repository ID of a user exception had a bad length")
	    (GET_LOCAL_HOST_FAILED 
	     8  WARNING "Unable to determine local hostname from InetAddress.getLocalHost().getHostName()")
	    ;; 9 is not used at this time - it is available for reuse.
	    (BAD_LOCATE_REQUEST_STATUS 
	     10  WARNING "Bad locate request status in IIOP locate reply")
	    (STRINGIFY_WRITE_ERROR
	     11  WARNING "Error while stringifying an object reference")
	    (BAD_GIOP_REQUEST_TYPE
	     12  WARNING "IIOP message with bad GIOP 1.0 message type")
	    (ERROR_UNMARSHALING_USEREXC
	     13  WARNING "Error in unmarshalling user exception")
	    (RequestDispatcherRegistry_ERROR
	     14  WARNING "Overflow in RequestDispatcherRegistry")
	    (LOCATIONFORWARD_ERROR
	     15  WARNING "Error in processing a LocationForward")
	    (WRONG_CLIENTSC
	     16  WARNING "Wrong client request dispatcher")
	    (BAD_SERVANT_READ_OBJECT
	     17  WARNING "Bad servant in read_Object")
	    (MULT_IIOP_PROF_NOT_SUPPORTED
	     18  WARNING "multiple IIOP profiles not supported")
	    (GIOP_MAGIC_ERROR
	     20 WARNING "Error in GIOP magic")
	    (GIOP_VERSION_ERROR
	     21 WARNING "Error in GIOP version")
	    (ILLEGAL_REPLY_STATUS 
	     22 WARNING "Illegal reply status in GIOP reply message")
	    (ILLEGAL_GIOP_MSG_TYPE 
	     23 WARNING "Illegal GIOP message type")
	    (FRAGMENTATION_DISALLOWED 
	     24 WARNING "Fragmentation not allowed for this message type")
	    (BAD_REPLYSTATUS 
	     25  WARNING "Bad status in the IIOP reply message")
	    (CTB_CONVERTER_FAILURE 
	     26 WARNING "character to byte converter failure")
	    (BTC_CONVERTER_FAILURE 
	     27 WARNING "byte to character converter failure")
	    (WCHAR_ARRAY_UNSUPPORTED_ENCODING 
	     28 WARNING "Unsupported wchar encoding: ORB only supports fixed width UTF-16 encoding")
	    (ILLEGAL_TARGET_ADDRESS_DISPOSITION 
	     29 WARNING "Illegal target address disposition value")    
	    (NULL_REPLY_IN_GET_ADDR_DISPOSITION 
	     30 WARNING "No reply while attempting to get addressing disposition")
	    (ORB_TARGET_ADDR_PREFERENCE_IN_EXTRACT_OBJECTKEY_INVALID 
	     31 WARNING "Invalid GIOP target addressing preference")
	    (INVALID_ISSTREAMED_TCKIND 
	     32 WARNING "Invalid isStreamed TCKind {0}")
	    (INVALID_JDK1_3_1_PATCH_LEVEL 
	     33 WARNING "Found a JDK 1.3.1 patch level indicator with value less than JDK 1.3.1_01 value of 1")
	    (SVCCTX_UNMARSHAL_ERROR 
	     34 WARNING "Error unmarshalling service context data")
	    (NULL_IOR 
	     35  WARNING "null IOR")
	    (UNSUPPORTED_GIOP_VERSION 
	     36 WARNING "Unsupported GIOP version {0}")
	    (APPLICATION_EXCEPTION_IN_SPECIAL_METHOD 
	     37 WARNING "Application exception in special method: should not happen")
	    (STATEMENT_NOT_REACHABLE1 
	     38 WARNING "Assertion failed: statement not reachable (1)")
	    (STATEMENT_NOT_REACHABLE2 
	     39 WARNING "Assertion failed: statement not reachable (2)")
	    (STATEMENT_NOT_REACHABLE3 
	     40 WARNING "Assertion failed: statement not reachable (3)")
	    (STATEMENT_NOT_REACHABLE4 
	     41 FINE "Assertion failed: statement not reachable (4)")
	    (STATEMENT_NOT_REACHABLE5 
	     42 WARNING "Assertion failed: statement not reachable (5)")
	    (STATEMENT_NOT_REACHABLE6 
	     43 WARNING "Assertion failed: statement not reachable (6)")
	    (UNEXPECTED_DII_EXCEPTION 
	     44 WARNING "Unexpected exception while unmarshalling DII user exception")
	    (METHOD_SHOULD_NOT_BE_CALLED 
	     45 WARNING "This method should never be called")
	    (CANCEL_NOT_SUPPORTED 
	     46 WARNING "We do not support cancel request for GIOP 1.1")
	    (EMPTY_STACK_RUN_SERVANT_POST_INVOKE 
	     47 WARNING "Empty stack exception while calling runServantPostInvoke")
	    (PROBLEM_WITH_EXCEPTION_TYPECODE 
	     48 WARNING "Bad exception typecode")
	    (ILLEGAL_SUBCONTRACT_ID 
	     49 WARNING "Illegal Subcontract id {0}")
	    (BAD_SYSTEM_EXCEPTION_IN_LOCATE_REPLY 
	     50 WARNING "Bad system exception in locate reply")
	    (BAD_SYSTEM_EXCEPTION_IN_REPLY 
	     51 WARNING "Bad system exception in reply")
	    (BAD_COMPLETION_STATUS_IN_LOCATE_REPLY 
	     52 WARNING "Bad CompletionStatus {0} in locate reply")
	    (BAD_COMPLETION_STATUS_IN_REPLY 
	     53 WARNING "Bad CompletionStatus {0} in reply")
	    (BADKIND_CANNOT_OCCUR 
	     54 WARNING "The BadKind exception should never occur here")
	    (ERROR_RESOLVING_ALIAS 
	     55 WARNING "Could not resolve alias typecode")
	    (TK_LONG_DOUBLE_NOT_SUPPORTED 
	     56 WARNING "The long double type is not supported in Java")
	    (TYPECODE_NOT_SUPPORTED 
	     57 WARNING "Illegal typecode kind")
	    (BOUNDS_CANNOT_OCCUR 
	     59 WARNING "Bounds exception cannot occur in this context")
	    (NUM_INVOCATIONS_ALREADY_ZERO 
	     61 WARNING "Number of invocations is already zero, but another invocation has completed")
	    (ERROR_INIT_BADSERVERIDHANDLER 
	     62 WARNING "Error in constructing instance of bad server ID handler")
	    (NO_TOA 
	     63 WARNING "No TOAFactory is availble")
	    (NO_POA 
	     64 WARNING "No POAFactory is availble")
	    (INVOCATION_INFO_STACK_EMPTY 
	     65 WARNING "Invocation info stack is unexpectedly empty")
	    (BAD_CODE_SET_STRING 
	     66 WARNING "Empty or null code set string")
	    (UNKNOWN_NATIVE_CODESET 
	     67 WARNING "Unknown native codeset: {0}")
	    (UNKNOWN_CONVERSION_CODE_SET 
	     68 WARNING "Unknown conversion codset: {0}")
	    (INVALID_CODE_SET_NUMBER 
	     69 WARNING "Invalid codeset number")
	    (INVALID_CODE_SET_STRING 
	     70 WARNING "Invalid codeset string {0}")
	    (INVALID_CTB_CONVERTER_NAME 
	     71 WARNING "Invalid CTB converter {0}")
	    (INVALID_BTC_CONVERTER_NAME 
	     72 WARNING "Invalid BTC converter {0}")
	    (COULD_NOT_DUPLICATE_CDR_INPUT_STREAM 
	     73 WARNING "Could not duplicate CDRInputStream")
	    (BOOTSTRAP_APPLICATION_EXCEPTION 
	     74 WARNING "BootstrapResolver caught an unexpected ApplicationException")
	    (DUPLICATE_INDIRECTION_OFFSET 
	     75 FINE "Old entry in serialization indirection table has a different value than the value being added with the same key")
	    (BAD_MESSAGE_TYPE_FOR_CANCEL 
	     76 WARNING "GIOP Cancel request contained a bad request ID: the request ID did not match the request that was to be cancelled")
	    (DUPLICATE_EXCEPTION_DETAIL_MESSAGE 
	     77 WARNING "Duplicate ExceptionDetailMessage")
	    (BAD_EXCEPTION_DETAIL_MESSAGE_SERVICE_CONTEXT_TYPE 
	     78 WARNING "Bad ExceptionDetailMessage ServiceContext type")
	    (UNEXPECTED_DIRECT_BYTE_BUFFER_WITH_NON_CHANNEL_SOCKET 
	     79 WARNING "unexpected direct ByteBuffer with non-channel socket")
	    (UNEXPECTED_NON_DIRECT_BYTE_BUFFER_WITH_CHANNEL_SOCKET 
	     80 WARNING "unexpected non-direct ByteBuffer with channel socket")
	    (INVALID_CONTACT_INFO_LIST_ITERATOR_FAILURE_EXCEPTION 
	     82 WARNING "There should be at least one CorbaContactInfo to try (and fail) so this error should not be seen.")
	    (REMARSHAL_WITH_NOWHERE_TO_GO 
	     83 WARNING "Remarshal with nowhere to go")
	    (EXCEPTION_WHEN_SENDING_CLOSE_CONNECTION 
	     84 WARNING "Exception when sending close connection")
	    (INVOCATION_ERROR_IN_REFLECTIVE_TIE 
	     85 WARNING "A reflective tie got an error while invoking method {0} on class {1}")
	    (BAD_HELPER_WRITE_METHOD 
	     86 WARNING "Could not find or invoke write method on exception Helper class {0}")
	    (BAD_HELPER_READ_METHOD 
	     87 WARNING "Could not find or invoke read method on exception Helper class {0}")
	    (BAD_HELPER_ID_METHOD 
	     88 WARNING "Could not find or invoke id method on exception Helper class {0}")
	    (WRITE_UNDECLARED_EXCEPTION 
	     89 WARNING "Tried to write exception of type {0} that was not declared on method")
	    (READ_UNDECLARED_EXCEPTION 
	     90 WARNING "Tried to read undeclared exception with ID {0}")
	    (UNABLE_TO_SET_SOCKET_FACTORY_ORB 
	     91 WARNING "Unable to setSocketFactoryORB")
	    (UNEXPECTED_EXCEPTION 
	     92 WARNING "Unexpected exception occurred where no exception should occur")
	    (NO_INVOCATION_HANDLER 
	     93 WARNING "No invocation handler available for {0}")
	    (INVALID_BUFF_MGR_STRATEGY 
	     94 WARNING "{0}: invalid buffer manager strategy for Java serialization")
	    (JAVA_STREAM_INIT_FAILED 
	     95 WARNING "Java stream initialization failed")
	    (DUPLICATE_ORB_VERSION_SERVICE_CONTEXT 
	     96 WARNING "An ORBVersionServiceContext was already in the service context list")
	    (DUPLICATE_SENDING_CONTEXT_SERVICE_CONTEXT 
	     97 WARNING "A SendingContextServiceContext was already in the service context list")
	    (NO_SUCH_THREADPOOL_OR_QUEUE 
	     98 WARNING "No such threadpool or queue {0}")
	    (INFO_CREATE_LISTENER_SUCCEEDED
	     99 FINE "Successfully created IIOP listener on the specified host/port: {0}/{1}")
	    (IOEXCEPTION_DURING_STREAM_CLOSE
	     100 WARNING "Exception occurred while closing an IO stream object")
	    (INVALID_JAVA_SERIALIZATION_VERSION
	     101 SEVERE "Invalid Java serialization version {0}")
	    (ERROR_IN_SERVICE_CONTEXT_MAP
	     102 WARNING "Object in ServiceContext map was not of the correct type")
	    (BAD_TYPE_IN_DELEGATE 
	     103 WARNING "The ContactInfoList in a CorbaClientDelegate is NOT a CorbaContactInfoList")
            (NO_FRAGMENT_QUEUE_FOR_REQUEST_ID
             117 WARNING "Ignoring parsed fragment message because there is no fragment queue found for request id {0}.")
            (RESUME_OPTIMIZED_READ_THREAD_INTERRUPTED
             118 WARNING "Ignoring unexpected InterruptedException while waiting for next fragment in CorbaMessageMediatorImpl.resumeOptimizedReadProcessing.")
            (UNDEFINED_CORBA_REQUEST_ID_NOT_ALLOWED
             119 SEVERE "Not allowed to get the integer value for an undefined CorbaRequestId.")
	    (GET_KEY_INVALID_IN_CACHE_TABLE
	     120 WARNING "Illegal call to getKey in CacheTable: this instance has no reverse map")
	    (TIMER_MANAGER_NOT_INITIALIZED
	     121 WARNING "TimerManager not initialized: error in constructoring TypeCodeImpl")
	    (TIMING_POINTS_ARE_NULL 
	     122 WARNING "TimingPoints instance is null in TypeCodeImpl constructor")
	    (LOST_CONNECTION_EVENT
	     123 SEVERE "Error in connection event handler caused event loss: may result in client-side deadlock")
	    (UNDEFINED_SOCKETINFO_OPERATION
	     124 WARNING "SharedCDRContactInfoImpl does not support SocketInfo calls")
	    (DUPLICATE_REQUEST_IDS_IN_RESPONSE_WAITING_ROOM
	     125 WARNING "Duplicate request ids in response waiting room: over wrote old one: {0},  with new one: {1}")
            (EXCEPTION_IN_READER_THREAD
             132 FINE "Exception occurred in reader thread")
            (EXCEPTION_IN_LISTENER_THREAD
             133 FINE "Exception occurred in listener thread")
	    )
	(MARSHAL 
	    (CHUNK_OVERFLOW 
	     1 WARNING "Data read past end of chunk without closing the chunk")
	    (UNEXPECTED_EOF 
	     2 WARNING "Grow buffer strategy called underflow handler")
	    (READ_OBJECT_EXCEPTION 
	     3  WARNING "Error in reading marshalled object")
	    (CHARACTER_OUTOFRANGE 
	     4  WARNING "Character not IOS Latin-1 compliant in marshalling")
	    (DSI_RESULT_EXCEPTION 
	     5  WARNING "Exception thrown during result() on ServerRequest")
	    (IIOPINPUTSTREAM_GROW 
	     6  WARNING "grow() called on IIOPInputStream")
	    (END_OF_STREAM 
	     7 FINE "Underflow in BufferManagerReadStream after last fragment in message")
	    (INVALID_OBJECT_KEY 
	     8 WARNING "Invalid ObjectKey in request header")
	    (MALFORMED_URL 
	     9 WARNING "Unable to locate value class for repository ID {0} because codebase URL {1} is malformed")
	    (VALUEHANDLER_READ_ERROR 
	     10 WARNING "Error from readValue on ValueHandler in CDRInputStream")
	    (VALUEHANDLER_READ_EXCEPTION 
	     11 WARNING "Exception from readValue on ValueHandler in CDRInputStream")
	    (BAD_KIND 
	     12 WARNING "Bad kind in isCustomType in CDRInputStream")
	    (CNFE_READ_CLASS 
	     13 WARNING "Could not find class {0} in CDRInputStream.readClass")
	    (BAD_REP_ID_INDIRECTION 
	     14 WARNING "Bad repository ID indirection at index {0}")
	    (BAD_CODEBASE_INDIRECTION 
	     15 WARNING "Bad codebase string indirection at index {0}")
	    (UNKNOWN_CODESET 
	     16 WARNING "Unknown code set {0} specified by client ORB as a negotiated code set")
	    (WCHAR_DATA_IN_GIOP_1_0 
	     17 WARNING "Attempt to marshal wide character or string data in GIOP 1.0")
	    (NEGATIVE_STRING_LENGTH 
	     18 WARNING "String or wstring with a negative length {0}")
	    (EXPECTED_TYPE_NULL_AND_NO_REP_ID 
	     19 WARNING "CDRInputStream.read_value(null) called, but no repository ID information on the wire")
	    (READ_VALUE_AND_NO_REP_ID 
	     20 WARNING "CDRInputStream.read_value() called, but no repository ID information on the wire")
	    (UNEXPECTED_ENCLOSING_VALUETYPE 
	     22 WARNING "Received end tag {0}, which is less than the expected value {1}")
	    (POSITIVE_END_TAG 
	     23 WARNING "Read non-negative end tag {0} at offset {1} (end tags should always be negative)")
	    (NULL_OUT_CALL 
	     24 WARNING "Out call descriptor is missing")
	    (WRITE_LOCAL_OBJECT 
	     25 WARNING "write_Object called with a local object")
	    (BAD_INSERTOBJ_PARAM 
	     26 WARNING "Tried to insert non-ObjectImpl {0} into an Any via insert_Object")
	    (CUSTOM_WRAPPER_WITH_CODEBASE 
	     27 WARNING "Codebase present in RMI-IIOP stream format version 1 optional data valuetype header")
	    (CUSTOM_WRAPPER_INDIRECTION 
	     28 WARNING "Indirection preseint in RMI-IIOP stream format version 2 optional data valuetype header")
	    (CUSTOM_WRAPPER_NOT_SINGLE_REPID 
	     29 WARNING "0 or more than one repository ID found reading the optional data valuetype header")
	    (BAD_VALUE_TAG 
	     30 WARNING "Bad valuetag {0} found while reading repository IDs")
	    (BAD_TYPECODE_FOR_CUSTOM_VALUE 
	     31 WARNING "Bad typecode found for custom valuetype")
	    (ERROR_INVOKING_HELPER_WRITE
	     32 WARNING "An error occurred using reflection to invoke IDL Helper write method")
	    (BAD_DIGIT_IN_FIXED 
	     33 WARNING "A bad digit was found while marshalling an IDL fixed type")
	    (REF_TYPE_INDIR_TYPE 
	     34 WARNING "Referenced type of indirect type not marshaled")
	    (BAD_RESERVED_LENGTH
	     35 WARNING "Request message reserved bytes has invalid length")
	    (NULL_NOT_ALLOWED 
	     36 WARNING "A null object is not allowed here")
	    (UNION_DISCRIMINATOR_ERROR 
	     38 WARNING "Error in typecode union discriminator")
	    (CANNOT_MARSHAL_NATIVE 
	     39 WARNING "Cannot marshal a native TypeCode")
	    (CANNOT_MARSHAL_BAD_TCKIND
	     40 WARNING "Cannot marshal an invalid TypeCode kind")
	    (INVALID_INDIRECTION 
	     41 WARNING "Invalid indirection value {0} (>-4): probable stream corruption")
	    (INDIRECTION_NOT_FOUND 
	     42 FINE "No type found at indirection {0}: probably stream corruption")
	    (RECURSIVE_TYPECODE_ERROR 
	     43 WARNING "Recursive TypeCode not supported by InputStream subtype")
	    (INVALID_SIMPLE_TYPECODE 
	     44 WARNING "TypeCode is of wrong kind to be simple")
	    (INVALID_COMPLEX_TYPECODE
	     45 WARNING "TypeCode is of wrong kind to be complex")
	    (INVALID_TYPECODE_KIND_MARSHAL 
	     46 WARNING "Cannot marshal typecode of invalid kind")
	    (UNEXPECTED_UNION_DEFAULT 
	     47 WARNING "Default union branch not expected") 
	    (ILLEGAL_UNION_DISCRIMINATOR_TYPE 
	     48 WARNING "Illegal discriminator type in union")
	    (COULD_NOT_SKIP_BYTES 
	     49 WARNING "Could not skip over {0} bytes at offset {1}")
	    (BAD_CHUNK_LENGTH 
	     50 WARNING "Incorrect chunk length {0} at offset {1}")
	    (UNABLE_TO_LOCATE_REP_ID_ARRAY
	     51 WARNING "Unable to locate array of repository IDs from indirection {0}")
	    (BAD_FIXED
	     52 WARNING "Fixed of length {0} in buffer of length {1}")
	    (READ_OBJECT_LOAD_CLASS_FAILURE
	     53 WARNING "Failed to load stub for {0} with class {1}")
	    (COULD_NOT_INSTANTIATE_HELPER
	     54 WARNING "Could not instantiate Helper class {0}")
	    (BAD_TOA_OAID 
	     55 WARNING "Bad ObjectAdapterId for TOA")
	    (COULD_NOT_INVOKE_HELPER_READ_METHOD 
	     56 WARNING "Could not invoke helper read method for helper {0}")
	    (COULD_NOT_FIND_CLASS
	     57 WARNING "Could not load class {0}")
	    (BAD_ARGUMENTS_NVLIST
	     58 FINE "Error in arguments(NVList) for DSI ServerRequest")
	    (STUB_CREATE_ERROR 
	     59 FINE "Could not create stub")
	    (JAVA_SERIALIZATION_EXCEPTION 
	     60 WARNING "Java serialization exception during {0} operation")
	    (COULD_NOT_READ_INFO
	     61 WARNING "Could not read exception from UEInfoServiceContext")
	    (ENUM_CLASS_NOT_FOUND
	     62 WARNING "Could not find enum class {0} while reading an enum")
	    (PROXY_CLASS_NOT_FOUND
             63 WARNING "Could not find Proxy class for interfaces {0} while reading a proxy")
            (MALFORMED_PROXY_URL
             64 WARNING "Unable to load proxy class for interfaces {0} because codebase URL {1} is malformed")
            (EMPTY_PROXY_INTERFACE_LIST
             65 WARNING "Unable to create proxy instance because the interface list specified is empty")
            (PROXY_WITH_ILLEGAL_ARGS
             66 WARNING "Unable to create proxy instance because Proxy.getProxyClass(..) called with violated restrictions.")
	    (OBJECT_NOT_SERIALIZABLE
	     67 WARNING "An instance of class {0} could not be marshalled: the class is not an instance of java.io.Serializable")
	    )
	(NO_IMPLEMENT
	    (GENERIC_NO_IMPL 
	     1  FINE "feature not implemented")
	    (CONTEXT_NOT_IMPLEMENTED 
	     2  FINE "IDL request context is not implemented") 
	    (GETINTERFACE_NOT_IMPLEMENTED
	     3  FINE "getInterface() is not implemented")
	    (SEND_DEFERRED_NOTIMPLEMENTED
	     4  FINE "send deferred is not implemented")
	    (LONG_DOUBLE_NOT_IMPLEMENTED
	     5 FINE "IDL type long double is not supported in Java")
            (NOT_SUPPORTED_ON_LAZY_ACCEPTOR
             6 WARNING "getAcceptedSocket is not supported for a CorbaAcceptorLazyImpl")
            )
	(OBJ_ADAPTER 
	    (NO_SERVER_SC_IN_DISPATCH
	     1  WARNING "No server request dispatcher found when dispatching request to object adapter")
	    (ORB_CONNECT_ERROR
	     2  WARNING "Error in connecting servant to ORB")
	    (ADAPTER_INACTIVE_IN_ACTIVATION
	     3 FINE "StubAdapter.getDelegate failed to activate a Servant"))
	(OBJECT_NOT_EXIST
	    (LOCATE_UNKNOWN_OBJECT
	     1  WARNING "Locate response indicated that the object was unknown") 
	    (BAD_SERVER_ID
	     2  FINE "The server ID in the target object key does not match the server key expected by the server") 
	    (BAD_SKELETON
	     3  WARNING "No skeleton found in the server that matches the target object key") 
	    (SERVANT_NOT_FOUND
	     4  WARNING "Servant not found") 
	    (NO_OBJECT_ADAPTER_FACTORY
	     5 WARNING "No object adapter factory")
	    (BAD_ADAPTER_ID
	     6  WARNING "Bad adapter ID")
	    (DYN_ANY_DESTROYED
	     7 WARNING "Dynamic Any was destroyed: all operations are invalid"))
	(TIMEOUT
	    (INTERRUPTED_EXCEPTION_IN_TIMEOUT
	     1 FINE "Sleep was interrupted in TCP timeouts")
	)
	(TRANSIENT
	    (REQUEST_CANCELED
	     1 FINE "Request cancelled by exception"))
	(UNKNOWN 
	    (UNKNOWN_CORBA_EXC
	     1  WARNING "Unknown user exception while unmarshalling")
	    (RUNTIMEEXCEPTION
	     2  WARNING "Unknown user exception thrown by the server - exception: {0}; message: {1}")
	    (UNKNOWN_DSI_SYSEX
	     3 WARNING "Error while marshalling SystemException after DSI-based invocation")
	    (UNKNOWN_SYSEX
	     4 WARNING "Error while unmarshalling SystemException")
	    (WRONG_INTERFACE_DEF
	     5 WARNING "InterfaceDef object of wrong type returned by server")
	    (NO_INTERFACE_DEF_STUB
	     6 WARNING "org.omg.CORBA._InterfaceDefStub class not available")
	    (UNKNOWN_EXCEPTION_IN_DISPATCH
	     7 FINE "UnknownException in dispatch"))))

;;; End of file.
