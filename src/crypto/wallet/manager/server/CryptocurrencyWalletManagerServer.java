package crypto.wallet.manager.server;

import crypto.wallet.manager.api.ApiCall;
import crypto.wallet.manager.api.ApiCallExecutor;
import crypto.wallet.manager.command.Command;
import crypto.wallet.manager.command.CommandExecutor;
import crypto.wallet.manager.database.Logs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CryptocurrencyWalletManagerServer {

    private static final int SERVER_PORT = 7777;

    private static final String SERVER_HOST = "localhost";

    private static final String DISCONNECT = "disconnect";

    private static final int BUFFER_SIZE = 32768;

    private static final int TIME_BETWEEN_API_REQUESTS = 30;

    private boolean isServerWorking;

    private ByteBuffer buffer;

    private final CommandExecutor commandExecutor;

    public CryptocurrencyWalletManagerServer(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
        this.isServerWorking = true;
    }

    public void startServer() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            Selector selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);

            buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

            isServerWorking = true;
            while (isServerWorking) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        try {
                            String clientInput = readClientInput(sc, key);

                            if (clientInput == null || !key.isValid()) {
                                continue;
                            }
                            String response = null;

                            try {
                                response = commandExecutor
                                        .execute(Command.newCommand(clientInput), key);
                            } catch (Exception e) {
                                Logs.logErrorWithStackTrace(e.getStackTrace()
                                        , "Couldn't parse the clientInput correctly.", Logs.DEFAULT_LOG_PATH);
                            } finally {
                                sendResponseToClient(sc, response);
                            }
                        } catch (IOException e) {
                            if (e.getMessage().contains("Connection reset")) {
                                // client exited forcefully, get the key associated with the channel
                                handleDisconnect(sc, key);
                            }
                        }
                    } else if (key.isAcceptable()) {
                        accept(selector, key);
                    }

                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the server socket", e);
        } catch (Exception e) {
            throw new RuntimeException("AutoCloseable object threw exception");
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String readClientInput(SocketChannel sc, SelectionKey key) throws IOException {
        buffer.clear();
        int r = sc.read(buffer);
        if (r < 0) {
            handleDisconnect(sc, key);
            return null;
        }

        buffer.flip();
        byte[] byteArray = new byte[buffer.remaining()];
        buffer.get(byteArray);
        return new String(byteArray, StandardCharsets.UTF_8);
    }

    private void handleDisconnect(SocketChannel sc, SelectionKey key) throws IOException {
        commandExecutor.execute(Command.newCommand(DISCONNECT), key);
        sc.close();
        key.cancel();
    }

    private void sendResponseToClient(SocketChannel sc, String response) throws IOException {
        if (response == null) {
            response = "There was a problem with reading your input. Try again.";
        }

        buffer.clear();
        buffer.put(response.getBytes());
        buffer.flip();
        sc.write(buffer);
        if (response.equals("disconnect")) {
            sc.close();
        } else if (response.equals("shutdown")) {
            sc.close();
            isServerWorking = false;
        }
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    public void start(ApiCall apiCall) {
        try (ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1)) {
            Runnable apiCallExecutor = new ApiCallExecutor(apiCall, commandExecutor.getCryptoCoinsInformation());
            Thread thread = new Thread(apiCallExecutor);
            scheduledExecutorService.scheduleAtFixedRate(thread,
                    0, TIME_BETWEEN_API_REQUESTS, TimeUnit.MINUTES);
            startServer();
        } catch (RuntimeException e) {
            Logs.logErrorWithStackTrace(e.getStackTrace()
                    , "Problem occurred with client communication.", Logs.DEFAULT_LOG_PATH);
        } catch (Exception e) {
            Logs.logErrorWithStackTrace(e.getStackTrace()
                    , "Problem occurred while trying to send request to API.", Logs.DEFAULT_LOG_PATH);
        }
    }
}
