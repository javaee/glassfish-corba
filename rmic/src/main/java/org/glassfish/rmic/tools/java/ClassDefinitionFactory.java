package org.glassfish.rmic.tools.java;

import java.io.DataInputStream;
import java.io.IOException;

public interface ClassDefinitionFactory {
    ClassDefinition loadDefinition(DataInputStream is, Environment env, int fileFlags) throws IOException;
}
