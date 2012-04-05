package org.glassfish.corba.annotation.processing;

import org.glassfish.pfl.basic.logex.ExceptionWrapper;
import org.glassfish.pfl.basic.logex.Message;
import org.junit.Test;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A test for the ExceptionWrapper annotation Processor.
 *
 * @author Russell Gold
 */
public class ExceptionWrapperProcessorTestCase {

    @Test
    public void processor_supportsExceptionWrapperAnnotation() {
        SupportedAnnotationTypes annotation = ExceptionWrapperProcessor.class.getAnnotation(SupportedAnnotationTypes.class);
        assertTrue(Arrays.asList(annotation.value()).contains( ExceptionWrapper.class.getName() ) );
    }

    @Test
    public void processor_supportsMessageAnnotation() {
        SupportedAnnotationTypes annotation = ExceptionWrapperProcessor.class.getAnnotation(SupportedAnnotationTypes.class);
        assertTrue(Arrays.asList(annotation.value()).contains( Message.class.getName() ) );
    }

    @Test
    public void process_supportsSourceVersion6() {
        SupportedSourceVersion annotation = ExceptionWrapperProcessor.class.getAnnotation(SupportedSourceVersion.class);
        assertEquals(SourceVersion.RELEASE_6,annotation.value());
    }

    @Test
    public void processor_extendsAbstractProcessor() {
        Class superclass = ExceptionWrapperProcessor.class.getSuperclass();
        assertEquals(AbstractProcessor.class, superclass);
    }

    @Test
    public void whenNoExceptionWrapperAnnotations_doNothing() {
        ExceptionWrapperProcessor processor = new ExceptionWrapperProcessor();
        assertFalse(processor.process(new HashSet<TypeElement>(), null));
    }


    static class TestExceptionWrapper implements ExceptionWrapper {
        @Override
        public String idPrefix() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String loggerName() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String resourceBundle() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
