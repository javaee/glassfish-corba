package org.codehaus.mojo.idlj;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests access using the Sun IDL compiler
 */
public class IDLJTestCase {

    private Properties savedProperties;
    private static String[] args;
    private TestClassloaderFacade loaderFacade = new TestClassloaderFacade();
    private TestScanner testScanner = new TestScanner();
    private TestDependenciesFacade testDependenciesFacade = new TestDependenciesFacade();
    private TestLog log = new TestLog();
    private IDLJMojo mojo;

    @Before
    public void setUp() throws Exception {
        args = null;
        savedProperties = (Properties) System.getProperties().clone();
        AbstractTranslator.setClassLoaderFacade(loaderFacade);

        mojo = new IDLJMojo(testDependenciesFacade);
        defineMavenProject(null);
        defineSourceDirectory("src/main/idl");
        defineOutputDirectory("target/main/generatedSources/idl");
        defineTimestampDirectory("target/main/timeStamps");
        mojo.setLog(log);
        testScanner.includedSources.add(new File("src/main/idl/dummy.idl"));
    }

    @After
    public void tearDown() {
        System.setProperties(savedProperties);
    }

    @Test
    public void whenCompilerNotSpecified_chooseSunCompiler() throws Exception {
        mojo.execute();
        assertEquals("com.sun.tools.corba.se.idl.toJavaPortable.Compile", loaderFacade.idlCompilerClass);
    }

    @Test
    public void whenVMNameContainsIBM_chooseIBMIDLCompiler() throws Exception {
        System.setProperty("java.vm.vendor", "pretend it is IBM");
        mojo.execute();
        assertEquals("com.ibm.idl.toJavaPortable.Compile", loaderFacade.idlCompilerClass);
    }

    @Test
    public void whenSpecified_chooseJacorbCompiler() throws Exception {
        defineCompiler("jacorb");
        mojo.execute();
        assertEquals("org.jacorb.idl.parser", loaderFacade.idlCompilerClass);
    }

    @Test
    public void whenNoOptionsAreSpecified_useCurrentDirectoryAsIncludePath() throws Exception {
        mojo.execute();
        assertArgumentsContains("-i", getCurrentDir() + "/src/main/idl");
    }

    @Test
    public void whenIncludePathIsSpecified_createIncludeArguments() throws Exception {
        defineIncludePaths( "/src/main/idl-include" );
        mojo.execute();
        assertArgumentsContains("-i", "/src/main/idl-include");
    }

    private String getCurrentDir() {
        return System.getProperty("user.dir");
    }

    private void assertArgumentsContains(String... expectedArgs) {
        if (!contains(args, expectedArgs))
            fail( toArgumentString( expectedArgs ) + " not found in " + toArgumentString(args));
    }

    private boolean contains(String[] container, String[] candidate) {
        for (int i = 0; i < container.length - candidate.length + 1; i++)
            if (isSubArrayAt(container, i, candidate)) return true;
        return false;
    }

    private boolean isSubArrayAt( String[] container, int start, String[] candidate ) {
        for (int j = 0; j < candidate.length; j++)
            if (!container[start+j].equals( candidate[j])) return false;
        return true;
    }

    private String toArgumentString(String... args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args)
            sb.append( arg ).append( ' ' );
        return sb.toString().trim();
    }

    private void defineCompiler(String compiler) throws NoSuchFieldException, IllegalAccessException {
        setPrivateFieldValue(mojo, "compiler", compiler);
    }

    private void defineMavenProject(Model model) throws NoSuchFieldException, IllegalAccessException {
        setPrivateFieldValue(mojo, "project", new MavenProject(model));
    }

    private void defineTimestampDirectory(String path) throws NoSuchFieldException, IllegalAccessException {
        setPrivateFieldValue(mojo, "timestampDirectory", new File(path));
    }

    private void defineOutputDirectory(String path) throws NoSuchFieldException, IllegalAccessException {
        setPrivateFieldValue(mojo, "outputDirectory", new File(path));
    }

    private void defineIncludePaths(String... paths) throws NoSuchFieldException, IllegalAccessException {
        File[] dirs = new File[ paths.length ];
        for (int i = 0; i < dirs.length; i++)
            dirs[i] = new File( paths[i] );
        setPrivateFieldValue(mojo, "includeDirs", dirs);
    }

    private void defineSourceDirectory(String path) throws NoSuchFieldException, IllegalAccessException {
        setPrivateFieldValue(mojo, "sourceDirectory", new File(path));
        testDependenciesFacade.readOnlyDirectories.add(new File(path));
    }

    private void setPrivateFieldValue(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Class theClass = obj.getClass();
        setPrivateFieldValue(obj, theClass, fieldName, value);
    }

    private void setPrivateFieldValue(Object obj, Class theClass, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        try {
            Field field = theClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (NoSuchFieldException e) {
            if (theClass.equals(Object.class))
                throw e;
            else
                setPrivateFieldValue(obj, theClass.getSuperclass(), fieldName, value);
        }
    }

    static class TestClassloaderFacade implements AbstractTranslator.ClassLoaderFacade {

        private List<URL> prependedURLs = new ArrayList<URL>();
        private List<String> loadedClasses = new ArrayList<String>();
        private String idlCompilerClass;
        private Map<String,URL> definedResources = new HashMap<String, URL>();

        public void prependUrls(URL... urls) {
            prependedURLs.addAll(Arrays.asList(urls));
        }

        public Class loadClass(String className) throws ClassNotFoundException {
            loadedClasses.add(className);
            idlCompilerClass = className;
            return TestIdlCompiler.class;
        }

        public URL getResource(String resourceName) {
            return definedResources.get(resourceName);
        }
    }

    static class TestIdlCompiler {
        public static void main(String... args) {
            IDLJTestCase.args = args.clone();
        }
    }


    static class TestScanner implements SourceInclusionScanner {

        private List<SourceMapping> mappings = new ArrayList<SourceMapping>();
        private Set<File> includedSources = new HashSet<File>();

        public void addSourceMapping(SourceMapping sourceMapping) {
            mappings.add(sourceMapping);
        }

        public Set getIncludedSources(File sourceDir, File targetDir) throws InclusionScanException {
            return includedSources;
        }
    }


    class TestDependenciesFacade implements AbstractIDLJMojo.DependenciesFacade {
        List<File> sourceFiles = new ArrayList<File>();
        List<File> targetFiles = new ArrayList<File>();
        List<File> writeableDirectories = new ArrayList<File>();
        List<File> readOnlyDirectories = new ArrayList<File>();

        public SourceInclusionScanner createSourceInclusionScanner(int updatedWithinMsecs, Set includes, Set excludes) {
            return testScanner;
        }

        public void copyFile(File sourceFile, File targetFile) throws IOException {
            sourceFiles.add(sourceFile);
            targetFiles.add(targetFile);
        }

        public boolean exists(File directory) {
            return isDirectory(directory);
        }

        public void createDirectory(File directory) {
            writeableDirectories.add(directory);
        }

        public boolean isWriteable(File directory) {
            return writeableDirectories.contains(directory);
        }

        public boolean isDirectory(File file) {
            return writeableDirectories.contains(file) || readOnlyDirectories.contains(file);
        }
    }


    static class TestLog implements org.apache.maven.plugin.logging.Log {
        public boolean isDebugEnabled() {
            return false;
        }

        public void debug(CharSequence charSequence) {
        }

        public void debug(CharSequence charSequence, Throwable throwable) {
        }

        public void debug(Throwable throwable) {
        }

        public boolean isInfoEnabled() {
            return false;
        }

        public void info(CharSequence charSequence) {
        }

        public void info(CharSequence charSequence, Throwable throwable) {
        }

        public void info(Throwable throwable) {
        }

        public boolean isWarnEnabled() {
            return false;
        }

        public void warn(CharSequence charSequence) {
        }

        public void warn(CharSequence charSequence, Throwable throwable) {
        }

        public void warn(Throwable throwable) {
        }

        public boolean isErrorEnabled() {
            return false;
        }

        public void error(CharSequence charSequence) {
        }

        public void error(CharSequence charSequence, Throwable throwable) {
        }

        public void error(Throwable throwable) {
        }
    }
}
