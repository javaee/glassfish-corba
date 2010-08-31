#  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#  
#  Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
#  
#  The contents of this file are subject to the terms of either the GNU
#  General Public License Version 2 only ("GPL") or the Common Development
#  and Distribution License("CDDL") (collectively, the "License").  You
#  may not use this file except in compliance with the License.  You can
#  obtain a copy of the License at
#  https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
#  or packager/legal/LICENSE.txt.  See the License for the specific
#  language governing permissions and limitations under the License.
#  
#  When distributing the software, include this License Header Notice in each
#  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

include Rules.jmk

all: build test1

build: idl src

idl:
	$(IDLJ) -td $(TOOLS_OUTPUTDIR) -fall -pkgPrefix IIOP org.omg -pkgPrefix IOP org.omg IIOP.idl
	$(IDLJ) -td $(TOOLS_OUTPUTDIR) -fall -pkgPrefix IIOP org.omg -pkgPrefix IOP org.omg IOP.idl
	$(IDLJ) -td $(TOOLS_OUTPUTDIR) -fall -pkgPrefix CONV_FRAME org.omg CONV_FRAME.idl
	$(JAVAC) -classpath $(CLASSPATH) -d $(TOOLS_OUTPUTDIR) $(TOOLS_OUTPUTDIR)/org/omg/IOP/*.java $(TOOLS_OUTPUTDIR)/org/omg/IIOP/*.java
	$(JAVAC) -classpath $(CLASSPATH) -d $(TOOLS_OUTPUTDIR) $(TOOLS_OUTPUTDIR)/org/omg/CONV_FRAME/*.java

src:
	$(JAVAC) -d $(TOOLS_OUTPUTDIR) -classpath $(CLASSPATH) IORDecoder.java TextOutputHandler.java IIOPProfileHandler.java Utility.java EncapsHandler.java TagHelperHandler.java CodeBaseHandler.java DecodingException.java InitializationException.java TaggedMapFileReader.java

test1:
	$(JAVA) $(ORB_PROPS) -classpath $(CLASSPATH) tools.ior.IORDecoder IOR:0000000000000026524d493a5044617461312e4461746131486f6d653a3030303030303030303030303030303000000000000001000000000000018c000102000000000f3133302e3231342e36302e3133320000041a00000000003eafabcb0000000022000000640000000100000000000000010000000e50657273697374656e74504f410000000000000d0000415514cc000000000001ff0300000000000500000001000000200000000000010001000000020501000100010020000101090000000100010100000000210000006800000000000000010040000000000024000000100000000000000424000000660000000000400040000000080606678102010101000000170401000806066781020101010000000764656661756c74000400000000000000000000010000000806066781020101010000001900000070000000000000006866696c653a2f453a2f6a3273646b6565312e332f7265706f7369746f72792f616a6f7368696e742f6170706c69636174696f6e732f41707031436c69656e742e6a617220687474703a2f2f616a6f7368696e743a393139312f41707031436c69656e742e6a6172000000001f0000000400000003000000200000000400000001

clean:
	$(RM) -r $(TOOLS_OUTPUTDIR)
