package cuenen.raymond.java.ppawegkant.application;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Dit is een class die de juiste 
 * afsluitprocedure afhandelt.
 * 
 * @author R. Cuenen
 */
public class ShutdownHandler extends Thread
        implements UncaughtExceptionHandler {

    
       /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            MainApplication.shutdown();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            MainApplication.onError("Er is een onherstelbare fout opgetreden", e);
        }
}
