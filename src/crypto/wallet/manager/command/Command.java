package crypto.wallet.manager.command;

import java.util.Arrays;
import java.util.List;

public record Command(String command, String[] arguments) {
    private static final int ONE_ARGUMENT_COMMAND = 1;

    public static Command newCommand(String clientInput) {
        if (clientInput == null) {
            throw new IllegalArgumentException("clientInput cannot be null");
        }

        List<String> tokens = Arrays.stream(clientInput.split(" ")).toList();

        if (tokens.size() == ONE_ARGUMENT_COMMAND) {
            return new Command(clientInput, null);
        }

        String[] args = tokens.subList(1, tokens.size()).toArray(new String[0]);
        return new Command(tokens.get(0), args);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Command command1 = (Command) o;

        if (!command.equals(command1.command)) return false;
        return Arrays.equals(arguments, command1.arguments);
    }

    @Override
    public int hashCode() {
        int result = command.hashCode();
        result = 31 * result + Arrays.hashCode(arguments);
        return result;
    }
}
