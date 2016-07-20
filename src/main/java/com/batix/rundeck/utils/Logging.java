/**
 * Logging utility class for reading and parsing ansible logs
 *
 * @author Yassine Azzouz <a href="mailto:yassine.azzouz@gmail.com">yassine.azzouz@gmail.com</a>
 */
package com.batix.rundeck.utils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Logging {

   /**
    * A simple Thread subclass that perform realtime ansible logging.
    */ 
    public static class AnsibleLoggerThread extends Thread { 
        final InputStream in; 
        final Listener out; 
        private IOException exception; 
 
        public AnsibleLoggerThread(final InputStream in, final Listener out) { 
            this.in = in; 
            this.out = out; 
        } 
 
        @Override 
        public void run() { 
            try { 
                Logging.ansibleStreamLogger(in, out); 
            } catch (IOException e) { 
                exception = e; 
            } 
        } 
 
        public IOException getException() { 
            return exception; 
        }
    }
    
   /**
    * Read the ansible logs from the input stream then parse and try to return it to the output listener. 
    * 
    * @param in  inputstream 
    * @param out listener 
    * 
    * @throws java.io.IOException if thrown by underlying io operations 
    */
    public static void ansibleStreamLogger(final InputStream in, final Listener out) throws IOException { 
       InputStreamReader isr = new InputStreamReader(in);
       LineNumberReader lines = new LineNumberReader(isr);
       String line;
       while ((line = lines.readLine()) != null) {
         out.output(line);
       }
    }

    /**
     * Return a new thread that will copy an inputstream to an output stream.  You must start the thread. 
     * 
     * @param in  inputstream 
     * @param out listener 
     * 
     * @return an unstarted {@link AnsibleLoggerThread} 
     */ 
    public static AnsibleLoggerThread copyStreamThread(final InputStream in, final Listener out) { 
        return new AnsibleLoggerThread(in, out); 
    }
}
