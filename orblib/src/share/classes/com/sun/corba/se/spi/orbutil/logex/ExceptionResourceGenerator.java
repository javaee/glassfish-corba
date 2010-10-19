/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.corba.se.spi.orbutil.logex;

import com.sun.corba.se.spi.orbutil.argparser.ArgParser;
import com.sun.corba.se.spi.orbutil.argparser.DefaultValue;
import com.sun.corba.se.spi.orbutil.argparser.Help;
import com.sun.corba.se.spi.orbutil.file.FileWrapper;
import com.sun.corba.se.spi.orbutil.file.Scanner;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** Scans a directory looking for class files.  For each class file,
 * if the class file is annotated with ExceptionWrapper, extract the
 * messages and write out into a resource file.
 *
 * @author ken
 */
public class ExceptionResourceGenerator {
    private final Arguments args ;

    private ExceptionResourceGenerator( String[] strs ) throws IOException {
        ArgParser<Arguments> ap = new ArgParser<Arguments>( Arguments.class ) ;
        args = ap.parse( strs ) ;
        args.destination().delete() ; // ignore return: just want to start
                                      // with an empty file.
    }

    private interface Arguments {
	@Help( "Set to >0 to get information about actions taken for every "
            + "file.  Larger values give more detail." )
	@DefaultValue( "0" )
	int verbose() ;

	@Help( "Source directory for classes to scan" )
	@DefaultValue( "" )
	File source() ;

	@Help( "Destination file for resources" )
	@DefaultValue( "" )
	File destination() ;
    }

    private void msg(String string) {
        System.out.println( string ) ;
    }

    private static String getLoggerName( Class<?> cls ) {
        ExceptionWrapper ew = cls.getAnnotation( ExceptionWrapper.class ) ;
        String str = ew.loggerName() ;
        if (str.length() == 0) {
            str = cls.getPackage().getName() ;
        }
        return str ;
    }

    /** Generate a list of Strings for a resource file for the given
     * exception and log handling class.
     * @param cls The class that describes messages for logging.
     * @return A list of strings suitable for inclusion in a resource bundle.
     */
    public static List<String> getResources( final Class<?> cls ) {
        if (cls.getAnnotation( ExceptionWrapper.class ) == null) {
            throw new RuntimeException( cls
                + " does not have an @ExceptionWrapper annotation") ;
        }

        Field self ;
        try {
            self = cls.getDeclaredField("self");
        } catch (Exception ex) {
            throw new RuntimeException("Could not get field named self in "
                + cls, ex) ;
        }

        int mod = self.getModifiers() ;
        if (!Modifier.isStatic(mod) || !Modifier.isPublic(mod)) {
            throw new RuntimeException( cls + " is not public static") ;
        }

        if (!self.getType().equals(cls)) {
            throw new RuntimeException( cls.getName()
                + ".self does not have the correct type" ) ;
        }

        WrapperGenerator.MessageInfo minfo ;
        try {
            minfo = (WrapperGenerator.MessageInfo)self.get(null) ;
        } catch (Exception exc) {
            throw new RuntimeException( "Could not access field self in " + cls,
                exc ) ;
        }

        Map<String,String> mmap = minfo.getMessageInfo() ;

        // Check that cls is annotated with @ExceptionWrapper
        // For each method of cls that is annotated with @Message
        //     add a string of the form
        //     <logger name>.<method name> = "<idPrefix><id> : <message text>"
        //     to the output.
        final List<String> result = new ArrayList<String>() ;
        for (Map.Entry<String,String> entry : mmap.entrySet()) {
            final StringBuilder sb = new StringBuilder() ;
            sb.append( entry.getKey() ) ;
            sb.append( "=\"" ) ;
            sb.append( entry.getValue() ) ;
            sb.append( "\"" ) ;
            result.add( sb.toString() ) ;
        }

        return result ;
    }

    Scanner.Action action = new Scanner.Action() {
        @SuppressWarnings("unchecked")
        public boolean evaluate(FileWrapper arg) {
            String fileName = arg.getAbsoluteName() ;
            if (fileName.endsWith(".class")) {
                final String absSourceName = args.source().getAbsolutePath() ;
                final String relArgName = fileName.substring(
                    absSourceName.length() + 1,
                    fileName.length() - ".class".length() ) ;
                final File output = new File( args.destination(),
                    relArgName + ".properties" ) ;
                final FileWrapper dest = new FileWrapper( output ) ;
                final String className =
                    relArgName.replace( File.separatorChar, '.' );

                ExceptionWrapper ew = null ;

                try {
                    Class cls = Class.forName( className ) ;
                    ew = (ExceptionWrapper)cls.getAnnotation(
                        ExceptionWrapper.class) ;

                    if (ew != null) {
                        if (args.verbose() > 0) {
                            msg( "Writing resource file for class "
                                + cls.getName() ) ;
                        }
                        List<String> resStrings = getResources(cls) ;

                        dest.open(FileWrapper.OpenMode.WRITE_EMPTY);
                        dest.writeLine( "### Resource file generated on "
                            + new Date() ) ;
                        dest.writeLine( "#" ) ;
                        dest.writeLine( "# Resources for class " + className );
                        dest.writeLine( "#" ) ;

                        if (resStrings != null) {
                            for (String str : resStrings) {
                                dest.writeLine(str);
                            }
                        }
                    }
                } catch (Exception exc ) {
                    if (args.verbose() > 0) {
                        msg( "Error in processing class " + className ) ;
                        exc.printStackTrace();
                    }
                } finally {
                    if (ew != null) {
                        dest.close();
                    }
                }
            }
            
            return true ;
        }

    } ;

    private void run() throws IOException {
        // set up scanner for args.source, which must be a directory
        Scanner scanner = new Scanner( args.verbose(), args.source() ) ;
        scanner.scan(action);
    }

    public static void main( String[] strs ) throws IOException {
        (new ExceptionResourceGenerator(strs)).run() ;
    }
}
