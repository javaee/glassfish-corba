#  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#  
#  Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
#  
#  The contents of this file are subject to the terms of either the GNU
#  General Public License Version 2 only ("GPL") or the Common Development
#  and Distribution License("CDDL") (collectively, the "License").  You
#  may not use this file except in compliance with the License.  You can
#  obtain a copy of the License at
#  https://oss.oracle.com/licenses/CDDL+GPL-1.1
#  or LICENSE.txt.  See the License for the specific
#  language governing permissions and limitations under the License.
#  
#  When distributing the software, include this License Header Notice in each
#  file and include the License file at LICENSE.txt.
#  
#  GPL Classpath Exception:
#  Oracle designates this particular file as subject to the "Classpath"
#  exception as provided by Oracle in the GPL Version 2 section of the License
#  file that accompanied this code.
#  
#  Modifications:
#  If applicable, add the following below the License Header, with the fields
#  enclosed by brackets [] replaced by your own identifying information:
#  "Portions Copyright [year] [name of copyright owner]"
#  
#  Contributor(s):
#  If you wish your version of this file to be governed by only the CDDL or
#  only the GPL Version 2, indicate your decision by adding "[Contributor]
#  elects to include this software in this distribution under the [CDDL or GPL
#  Version 2] license."  If you don't indicate a single choice of license, a
#  recipient has the option to distribute your version of this file under
#  either the CDDL, the GPL Version 2 or to extend the choice of license to
#  its licensees as provided above.  However, if you add GPL Version 2 code
#  and therefore, elected the GPL Version 2 license, then the option applies
#  only if the new code is made subject to such option by the copyright
#  holder.

This is an example of how to use interceptors.

It contains two scenarios:

1. A logging service which logs invocations.  The client nor server
   does not explicitly use the logging service.

2. An "empty" service which passes information from the client to the
   server.  The client and server explicitly use this service but are
   not aware that it is implemented using interceptors.

The purpose of the logging service example is to show how to avoid
infinite recursion when making outcalls (i.e., invocations on CORBA
references) from within interception points.  This turns out to be quite
involved when all corner cases are covered.

The purpose of the "empty" service example is to show how to implement
services which flow context information from client to server and back.

Note: these examples explicitly register ORBInitializers to make the
code easier to experiment with and setup.  Typically this would not be
done.  Instead, this information would be passed as -D properties to
java when the applications are started.  That way the applications are
not coupled to the fact that either the service exists (e.g., the
logging service) or that a service that they explicitly use (e.g., the
"AService" interface) is implemented as an interceptor.

Files:

serviceexample.idl 

        Contains definitions of an arbitrary object on which to make
        invocations and two services which will service calls to the
        arbitrary object.

LoggingServiceClientORBInitializer.java

        Creates and registers the logging service interceptor used by
        object clients.

LoggingServiceClientInterceptor.java

        This interceptor logs client side interception points.  It
        illustrates how to make invocations on other objects from
        within an interceptor and how to avoid infinite recursion
        during those "outcall" invocations.

LoggingServiceServerORBInitializer.java

        Creates and registers the logging service interceptor used by
        object servers.  Even though this interceptor is meant to only
        log server side interception points it is both a client and
        server interceptor.  This is to illustrate how to avoid
        infinite recursion in the case where the object being called
        is colocated in the same ORB as which the interceptors are
        registered.

LoggingServiceServerInterceptor.java

        This interceptor logs server side interception points.

        This interceptor is implemented as both a
        ClientRequestInterceptor and a ServerRequestInterceptor to
        illustrate the need to set some "out call" service context
        data (in addition to setting an out call slot) to avoid
        infinite recursion for the case noted in the description of
        LoggingServiceServerORBInitializer.java.
        
LoggingServiceImpl.java

        The logging object to which the logging service interceptors
        send their data.

AServiceORBInitializer.java

        Creates and registers the AServiceInterceptor as both a client
        and server interceptor.  It also creates a local object to be
        used by applications to explicitly control the services
        offered by AService.  This local object is registered as an
        initial service with the ORB such that it can be obtained by
        clients via org.omg.CORBA.ORB.resolve_initial_references.

        Creates and registers teh AServiceIORInterceptor which
        puts a TaggedComponent into IORs.

AServiceImpl.java

        This object is explicitly used by client and servant code to
        communicate with the service.  When the client "begin"s the
        service that causes an "service id" specific to the service to
        be put in the TSC PICurrent slot reserved by this service.
        When the client call "end" on the service it sets the TSC slot
        to a non-valid value to indicate that the service is not in
        effect.  Servant's use the "verify" method which simply
        indicates whether the service id was passed from the
        client side to the server side.

AServiceInterceptor.java

        This interceptor is responsible for arranging to pass the
        client side AService information to the service side.

        On the client side, if AService.begin() has been called then
        the send_request point will see the service id in the RSC
        slot.  In this case it then inserts the value of that service
        id into a org.omg.CORBA.ServiceContext and adds that service
        context to the data to be passed along with the invocation.

        On the server side, receive_request_service_context looks for
        the presence of that service context.  When present, it
        extracts the service id value from the service context and
        sets the RSC slot to that value.  When the servant is
        executing the value of that RSC slot is available in the TSC
        slot.

AServiceIORInterceptor.java

        Adds a TaggedComponent to IORs.

ArbitaryObjectImpl.java

        An implementation and server for the ArbitraryObject IDL interface.
        The implementations of the IDL interface operations explicitly
        call the AServiceImpl.verify method to illustrate the
        end-to-end passing of data from the client (via
        AService.begin) to the servant.

Client.java

        A client which calls methods on ArbitraryObject.  It makes
        some of those calls within the context of AService and some
        outside of its context.  It is unaware of the existence of the
        logging interceptor (except that it explicitly registers the
        LoggingServerClientORBInitializer as noted above).
        
ColocatedServers.java

        This is a server which runs both ArbitraryObject and
        LoggingService in the same ORB.  This means that these objects
        are "colocated".  

        This is done to exercise the code in
        LoggingServiceServerInterceptor that illustrates when
        interceptors make out calls to objects which are colocated in
        the same ORB extra steps must be taken to avoid infinite
        recursion.

ServerExampleTest.java

        This class exists to run the code in the rip unit test
        framework.  It is NOT part of the example.

// End of file.
