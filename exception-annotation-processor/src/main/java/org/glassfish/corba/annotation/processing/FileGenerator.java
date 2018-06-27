/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.glassfish.corba.annotation.processing;

import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Message;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
* This class generates properties files based on annotations.
*/
class FileGenerator {
    private Element classElement;
    private Date creationDate;
    private List<Element> methodElements = new ArrayList<Element>();

    FileGenerator(Element classElement, Date creationDate) {
        this.classElement = classElement;
        this.creationDate = creationDate;
    }

    String getPrefix() {
        ExceptionWrapper wrapper = classElement.getAnnotation(ExceptionWrapper.class);
        return wrapper.idPrefix();
    }

    FileObject createResource(Filer filer) throws IOException {
        return filer.createResource(StandardLocation.CLASS_OUTPUT, getPackage(), getName() + ".properties");
    }

    void addMethod(Element methodElement) {
        methodElements.add(methodElement);
    }

    String getPackage() {
        return classElement.getEnclosingElement().toString();
    }

    private String getName() {
        return classElement.getSimpleName().toString();
    }

    boolean shouldWriteFile() {
        return !methodElements.isEmpty();
    }

    void writePropertyFileHeader(Writer writer) throws IOException {
        writer.append("### Resource file generated on ").append(creationDate.toString()).append('\n');
        writer.append("#\n");
        writer.append("# Resources for class ").append(classElement.toString()).append('\n');
        writer.append("#\n");
    }

    void writePropertyLines(Writer writer) throws IOException {
        for (Element methodElement : methodElements)
            writePropertyLine(writer, methodElement);
    }

    private void writePropertyLine(Writer writer, Element methodElement) throws IOException {
        writer.append('.').append(methodElement.getSimpleName()).append("=\"").append(getPrefix())
              .append(": ").append(getMessage(methodElement)).append("\"\n");
    }

    private String getMessage(Element methodElement) {
        return methodElement.getAnnotation(Message.class).value();
    }

    void writeContents(Writer writer) throws IOException {
        writePropertyFileHeader(writer);
        writePropertyLines(writer);
        writer.close();
    }

    void writeFile(Filer filer) throws IOException {
        FileObject file = createResource(filer);
        writeContents(file.openWriter());
    }
}
