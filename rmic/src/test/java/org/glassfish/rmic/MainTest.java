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

package org.glassfish.rmic;

import org.glassfish.rmic.classes.hcks.RmiIIServant;
import org.glassfish.rmic.tools.java.ClassPath;
import org.glassfish.rmic.tools.java.Identifier;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.glassfish.rmic.tools.java.Constants.F_WARNINGS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MainTest {

    private ClassPath classPath = new ClassPath();
    private ByteArrayOutputStream out = new ByteArrayOutputStream();
    private BatchEnvironment environment;
    private Main main = new Main(out, "rmic");
    private File destDir;

    @Before
    public void setUp() throws Exception {
        destDir = Files.createTempDirectory("rmic").toFile();
        environment = new BatchEnvironment(out, classPath, destDir);
        environment.flags = F_WARNINGS;
    }

    @Test
    public void whenNoErrorsOrWarnings_displayErrorsReturnsTrue() throws Exception {
        assertThat(main.displayErrors(environment), is(true));
    }

    @Test
    public void afterOneError_displayErrorsReturnsFalse() throws Exception {
        environment.error(0, "error");

        assertThat(main.displayErrors(environment), is(false));
    }

    @Test
    public void whenNoErrorsOrWarnings_outputIsEmpty() throws Exception {
        main.displayErrors(environment);

        assertThat(getOutput(), isEmptyString());
    }

    private String getOutput() throws IOException {
        out.close();
        return out.toString();
    }

    @Test
    public void afterOneError_outputReportsOneError() throws Exception {
        reportError();

        main.displayErrors(environment);

        assertThat(getOutput(), containsString("1 error"));
    }

    private void reportError() {
        environment.error(0, "rmic.wrote", "something bad");
    }

    @Test
    public void afterThreeErrors_outputReportsNumberOfErrors() throws Exception {
        reportError();
        reportError();
        reportError();

        main.displayErrors(environment);

        assertThat(getOutput(), containsString("3 errors"));
    }

    @Test
    public void afterOneWarning_outputReportsOneWarning() throws Exception {
        reportWarning();

        main.displayErrors(environment);

        assertThat(getOutput(), containsString("1 warning"));
    }

    private void reportWarning() {
        environment.error(0, "warn.rmic.member.not.mapped", "foo", "bar");
    }

    @Test
    public void afterThreeWarningss_outputReportsNumberOfErrors() throws Exception {
        reportWarning();
        reportWarning();
        reportWarning();

        main.displayErrors(environment);

        assertThat(getOutput(), containsString("3 warnings"));
    }

    @Test
    public void afterOneErrorAndTwoWarnings_outputReportsNumbersOfBoth() throws Exception {
        reportError();
        reportWarning();
        reportWarning();

        main.displayErrors(environment);

        assertThat(getOutput(), containsString("1 error, 2 warnings"));
    }

    @Test
    public void afterTwoErrorsAndOneWarnings_outputReportsNumbersOfBoth() throws Exception {
        reportError();
        reportError();
        reportWarning();

        main.displayErrors(environment);

        assertThat(getOutput(), containsString("2 errors, 1 warning"));
    }

    @Test
    public void afterGenerateStubs_getGeneratedClassNames() throws Exception {
        main.parseArgs("-iiop", "-classpath", TestUtils.getClassPathString(), "-d", destDir.getAbsolutePath());
        environment = main.getEnv();
        Identifier identifier = Main.getClassIdentifier(environment, RmiIIServant.class.getName());
        main.generateClass(environment, identifier);

        assertThat(main.getGeneratedClassNames(environment),
                arrayContainingInAnyOrder("org.glassfish.rmic.classes.hcks._RmiII_Stub", "org.glassfish.rmic.classes.hcks._RmiIIServant_Tie"));
    }

}
