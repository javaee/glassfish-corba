package org.glassfish.corba.annotation.processing;

import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Message;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * This class creates properties files for annotated exception interfaces. Applicable interfaces are annotated with the
 * {@link ExceptionWrapper} annotation. An entry will be made for each method with a {@link Message} annotation.
 */
@SupportedAnnotationTypes( {"org.glassfish.pfl.basic.logex.ExceptionWrapper","org.glassfish.pfl.basic.logex.Message"} )
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ExceptionWrapperProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnvironment) {
        System.out.println( "REG-> typeElements " + typeElements );
        return false;
    }
}
