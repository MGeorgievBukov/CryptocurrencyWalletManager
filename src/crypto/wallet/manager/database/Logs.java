package crypto.wallet.manager.database;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Logs {

    public static final String DEFAULT_LOG_PATH = "logs.log";

    public static void logErrorWithStackTrace(StackTraceElement[] ste, String message, String logPath) {
        Logger logger = Logger.getLogger("MyLog");
        logger.setUseParentHandlers(false);
        FileHandler fh;

        try {
            fh = new FileHandler(logPath, true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            StringBuilder stackTrace = new StringBuilder();
            for (var el : ste) {
                stackTrace.append(System.lineSeparator()).append(el.toString());
            }

            logger.info(message + stackTrace);
            fh.close();
        } catch (SecurityException | IOException e) {
            System.out.println("Could not log error");
        }
    }

    public static void logErrorMessage(String message, String logPath) {
        Logger logger = Logger.getLogger("MyLog");
        logger.setUseParentHandlers(false);
        FileHandler fh;

        try {
            fh = new FileHandler(logPath, true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            logger.info(message);
            fh.close();
        } catch (SecurityException | IOException e) {
            System.out.println("Could not log error");
        }
    }
}
