# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
# 
# Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
# 
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://oss.oracle.com/licenses/CDDL+GPL-1.1
# or LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
# 
# When distributing the software, include this License Header Notice in each
# file and include the License file at legal/LICENSE.txt.
# 
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
# 
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
# 
# Contributor(s):
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

Requirements
------------

This requires an ORB that supports the CodecFactory
through resolve_initial_references.  Also, if you want
to see CSIv2 related information, adjust the two
tag files appropriately.  Currently, they look for
com.sun.corba.ee.org.omg.CSIIOP files (renamed to
ee).

Of course, you'll want to use the generated IDL files
that correspond to the same classes that were used to
create the IOR.

Overview
--------

This is a simple utility which uses standard APIs to
print the contents of a stringified IOR.  It uses
reflection and a mapping of tag to instance of
EncapsHandler to recursively decode.

If a Helper class is available for a certain tagged
component or profile, one can easily associate the
tag with the Helper's type by adding a line in the
appropriate text file.

There are two files, one that maps tags to profiles,
and one that maps tags to components.

If you want to add more semantic interpretation,
create your own EncapsHandler and associate it
with a certain tag in one of the files.

Building
--------

Make sure your rip-int workspace is built, as the makefiles
rely on its classes.  Simply run 

gnumake build 

from this directory.

Generated code and classes are places in a build subdirectory
under this directory.

Possibilities
-------------

One could have another map from

[containing class name][field name]

to EncapsHandler.  That way, someone could provide
an EncapsHandler which could break apart a Sun
object key to give more specific details about what
it contains.

Sample
------

IOR:0000000000000026524d493a5044617461312e4461746131486f6d653a3030303030303030303030303030303000000000000001000000000000018c000102000000000f3133302e3231342e36302e3133320000041a00000000003eafabcb0000000022000000640000000100000000000000010000000e50657273697374656e74504f410000000000000d0000415514cc000000000001ff0300000000000500000001000000200000000000010001000000020501000100010020000101090000000100010100000000210000006800000000000000010040000000000024000000100000000000000424000000660000000000400040000000080606678102010101000000170401000806066781020101010000000764656661756c74000400000000000000000000010000000806066781020101010000001900000070000000000000006866696c653a2f453a2f6a3273646b6565312e332f7265706f7369746f72792f616a6f7368696e742f6170706c69636174696f6e732f41707031436c69656e742e6a617220687474703a2f2f616a6f7368696e743a393139312f41707031436c69656e742e6a6172000000001f0000000400000003000000200000000400000001

Output:

Number of known TaggedProfiles: 2
Number of known TaggedComponents: 5
IOR:
  type_id: RMI:PData1.Data1Home:0000000000000000
  profiles array [length 1]
    profiles[0]
      tag: 0
      ProfileBody_1_1:
        iiop_version:
          major: 1
          minor: 2
        host: 130.214.60.132
        port: 1050
        object_key:
          af ab cb 00 00 00 00 22 00 00 00 64 00 00 00 01 ???........d....
          00 00 00 00 00 00 00 01 00 00 00 0e 50 65 72 73 ............Pers
          69 73 74 65 6e 74 50 4f 41 00 00 00 00 00 00 0d istentPOA.......
          00 00 41 55 14 cc 00 00 00 00 00 01 ff 03       ..AU.?........
        components array [length 5]
          components[0]
            tag: 1
            type: IDL:CONV_FRAME/CodeSetComponentInfo:1.0
            data:
              ForCharData:
                native_code_set: 65537
                conversion_code_sets array [length 2]
                  conversion_code_sets[0]: 83951617
                  conversion_code_sets[1]: 65568
              ForWcharData:
                native_code_set: 65801
                conversion_code_sets array [length 1]
                  conversion_code_sets[0]: 65792
          components[1]
            tag: 33
            type: IDL:omg.org/CSIIOP/CompoundSecMechList:1.0
            data:
              stateful: false
              mechanism_list array [length 1]
                mechanism_list[0]:
                  target_requires: 64
                  transport_mech
                    tag: 36
                    type: IDL:omg.org/CSIIOP/TLS_SEC_TRANS:1.0
                    data:
                      target_supports: 0
                      target_requires: 0
                      port: 1060
                  as_context_mech:
                    target_supports: 64
                    target_requires: 64
                    client_authentication_mech:
                      06 06 67 81 02 01 01 01                         ..g?....
                    target_name:
                      04 01 00 08 06 06 67 81 02 01 01 01 00 00 00 07 ......g?........
                      64 65 66 61 75 6c 74                            default
                  sas_context_mech:
                    target_supports: 1024
                    target_requires: 0
                    privilege_authorities array [length 0]
                    supported_naming_mechanisms array [length 1]
                      supported_naming_mechanisms[0]:
                        06 06 67 81 02 01 01 01                         ..g?....
          components[2]
            tag: 25
            type: Java Codebase Component
            codebase: file:/E:/j2sdkee1.3/repository/ajoshint/applications/App1Client.jar http://ajoshint:9191/App1Client.jar
          components[3]
            tag: 31
            data:
              00 00 00 03                                     ....
          components[4]
            tag: 32
            data:
              00 00 00 01                                     ....
