package crypto.wallet.manager.wallet;

import crypto.wallet.manager.command.Command;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandTest {

    public Command command;

    @Test
    public void testNewCommandTwoArguments() {
        String[] args = new String[]{"arg1", "arg2"};
        command = new Command("test", args);
        assertEquals(command, Command.newCommand("test arg1 arg2"), "Command parsing is not correct");
    }

    @Test
    public void testNewCommandZeroArguments() {
        command = new Command("test", null);
        assertEquals(command, Command.newCommand("test"), "Command parsing is not correct");
    }

    @Test
    public void testNewCommandClientInputIsNull() {
        command = new Command("test", null);
        assertThrows(IllegalArgumentException.class, () -> Command.newCommand(null), "Expected exception but was not thrown");
    }
}
