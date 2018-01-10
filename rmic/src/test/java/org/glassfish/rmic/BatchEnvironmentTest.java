package org.glassfish.rmic;

import org.junit.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;

public class BatchEnvironmentTest {

    @Test
    public void createdClassPathString_usesPathSeparator() throws Exception {
        String systemPath = "./jdk/jre/lib/rt.jar";
        String classPath = "./user.jar" + File.pathSeparator + "./user2.jar" + File.pathSeparator + "./user3.jar";

        assertThat(BatchEnvironment.createClassPath(classPath, systemPath).toString().split(File.pathSeparator), arrayWithSize(4));
    }
 }