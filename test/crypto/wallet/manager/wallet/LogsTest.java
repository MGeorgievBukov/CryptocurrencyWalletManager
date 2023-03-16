package crypto.wallet.manager.wallet;

import crypto.wallet.manager.database.Logs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogsTest {

    public static String logsPath = "logsTest.log";

    private static String readFromLogs() {
        Path pathOfLogs = Path.of(logsPath);
        StringBuilder logsContent = new StringBuilder();
        try (var bufferedReader = Files.newBufferedReader(pathOfLogs)) {
            String line;
            line = bufferedReader.readLine();
            while ((line = bufferedReader.readLine()) != null) {
                logsContent.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new IllegalStateException("A problem occurred while reading from a file", e);
        }

        return logsContent.toString();
    }

    @Test
    public void testLogErrorWithStackTrace() {
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();

        Logs.logErrorWithStackTrace(ste, "Test message", logsPath);

        StringBuilder expectedResult = new StringBuilder("INFO: Test message" + System.lineSeparator());
        for (var el : ste) {
            expectedResult.append(el.toString()).append(System.lineSeparator());
        }

        String logsResult = readFromLogs();
        assertEquals(expectedResult.toString(), logsResult, "Logging errors with stack trace is not working properly.");
    }

    @Test
    public void testLogErrorWithNoStackTrace() {
        Logs.logErrorMessage("Test message", logsPath);

        String logsResult = readFromLogs();
        assertEquals("INFO: Test message" + System.lineSeparator(), logsResult, "Logging errors with no stack trace is not working properly.");
    }

    @AfterEach
    public void teardown() throws IOException {
        Files.delete(Path.of(logsPath));
    }
}
