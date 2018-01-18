package org.glassfish.rmic.tools.javac;

import com.meterware.simplestub.Memento;
import com.meterware.simplestub.SystemPropertySupport;
import org.glassfish.rmic.asm.AsmClassFactory;
import org.glassfish.rmic.tools.binaryclass.BinaryClassFactory;
import org.glassfish.rmic.tools.java.ClassDefinitionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

@SuppressWarnings("deprecation")
public class BatchEnvironmentTest {
    private static final String USE_LEGACY_PARSING_PROPERTY = "org.glassfish.rmic.UseLegacyClassParsing";
    private static final String JAVA_VERSION_PROPERTY = "java.version";
    private List<Memento> mementos = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        mementos.add(SystemPropertySupport.preserve(USE_LEGACY_PARSING_PROPERTY));
        mementos.add(SystemPropertySupport.preserve(JAVA_VERSION_PROPERTY));
    }

    @After
    public void tearDown() throws Exception {
        for (Memento memento : mementos) memento.revert();
    }

    @Test
    public void whenPropertyNotSet_chooseAsmParser() throws Exception {
        System.clearProperty(USE_LEGACY_PARSING_PROPERTY);

        ClassDefinitionFactory factory = BatchEnvironment.createClassDefinitionFactory();

        assertThat(factory, instanceOf(AsmClassFactory.class));
    }

    @Test
    public void whenPropertySetAndJdk8_chooseBinaryParser() throws Exception {
        System.setProperty(USE_LEGACY_PARSING_PROPERTY, "true");
        System.setProperty(JAVA_VERSION_PROPERTY, "1.8");

        ClassDefinitionFactory factory = BatchEnvironment.createClassDefinitionFactory();

        assertThat(factory, instanceOf(BinaryClassFactory.class));
    }

    @Test
    public void whenPropertySetAndJdk9_chooseBinaryParser() throws Exception {
        System.setProperty(USE_LEGACY_PARSING_PROPERTY, "true");
        System.setProperty(JAVA_VERSION_PROPERTY, "9");

        ClassDefinitionFactory factory = BatchEnvironment.createClassDefinitionFactory();

        assertThat(factory, instanceOf(BinaryClassFactory.class));
    }

    @Test
    public void whenPropertySetAndJdk10_chooseAsmParser() throws Exception {
        System.setProperty(USE_LEGACY_PARSING_PROPERTY, "true");
        System.setProperty(JAVA_VERSION_PROPERTY, "10");

        ClassDefinitionFactory factory = BatchEnvironment.createClassDefinitionFactory();

        assertThat(factory, instanceOf(AsmClassFactory.class));
    }

    @Test
    public void whenPropertySetAndJdk10EarlyAccess_chooseAsmParser() throws Exception {
        System.setProperty(USE_LEGACY_PARSING_PROPERTY, "true");
        System.setProperty(JAVA_VERSION_PROPERTY, "10-ea");

        ClassDefinitionFactory factory = BatchEnvironment.createClassDefinitionFactory();

        assertThat(factory, instanceOf(AsmClassFactory.class));
    }

    @Test
    public void whenPropertySetAndJdk11_chooseAsmParser() throws Exception {
        System.setProperty(USE_LEGACY_PARSING_PROPERTY, "true");
        System.setProperty(JAVA_VERSION_PROPERTY, "11");

        ClassDefinitionFactory factory = BatchEnvironment.createClassDefinitionFactory();

        assertThat(factory, instanceOf(AsmClassFactory.class));
    }

}