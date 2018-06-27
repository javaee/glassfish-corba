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

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.*;

/**
 * This class creates properties files for annotated exception interfaces. Applicable interfaces are annotated with the
 * {@link ExceptionWrapper} annotation. An entry will be made for each method with a {@link Message} annotation.
 */
@SupportedAnnotationTypes({"org.glassfish.pfl.basic.logex.ExceptionWrapper", "org.glassfish.pfl.basic.logex.Message"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ExceptionWrapperProcessor extends AbstractProcessor {

    Map<Element,FileGenerator> annotatedClasses = new HashMap<Element, FileGenerator>();
    Date creationDate = new Date();

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnvironment) {
        if (roundEnvironment.processingOver()) return false;
        if (typeElements.isEmpty()) return false;

        processClassElements(roundEnvironment.getElementsAnnotatedWith(ExceptionWrapper.class));
        processMethodElements(roundEnvironment.getElementsAnnotatedWith(Message.class));

        for (FileGenerator generator : annotatedClasses.values())
            writeFile(generator);
        return true;
    }

    private void writeFile(FileGenerator generator) {
        try {
            if (generator.shouldWriteFile())
                generator.writeFile(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processClassElements(Set<? extends Element> classElements) {
        for (Element classElement : classElements)
            annotatedClasses.put(classElement,new FileGenerator(classElement, creationDate));
    }

    private void processMethodElements(Set<? extends Element> methodElements) {
        for (Element methodElement : methodElements)
            if (annotatedClasses.containsKey(methodElement.getEnclosingElement()))
                annotatedClasses.get(methodElement.getEnclosingElement()).addMethod(methodElement);
    }

}
