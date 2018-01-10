package org.glassfish.rmic.tools.java;

import java.io.IOException;
import java.io.InputStream;

public interface ClassDefinitionFactory {
    ClassDefinition loadDefinition(InputStream is, Environment env) throws IOException;
}
