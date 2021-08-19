package org.crimson;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.crimson.command.CrimsonCommandDispatcher;
import org.crimson.command.ServerCommand;
import org.crimson.command.commands.CommandServerStop;
import org.crimson.console.CrimsonConsoleHandler;
import org.crimson.network.CrimsonNetwork;
import org.crimson.network.Network;
import org.crimson.network.event.CrimsonServerEventHandler;
import org.crimson.properties.CrimsonServerProperties;
import org.crimson.properties.ServerPropertiesConfiguration;
import org.crimson.punishment.BanList;
import org.crimson.punishment.BanListByIP;
import org.crimson.punishment.BanListByName;
import org.crimson.punishment.BanListType;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The base server
 */
@Log4j2
final class CrimsonServer implements Server {

    /**
     * Version
     */
    private static final String VERSION = "0.1.1-81221";

    /**
     * Current path of this server
     */
    private static final String PATH = System.getProperty("user.dir") + "/";

    /**
     * The server.properties file configuration.
     */
    private final ServerPropertiesConfiguration serverProperties = new CrimsonServerProperties();

    /**
     * Constant array of ban-lists.
     */
    private final BanList[] banLists = new BanList[2];

    /**
     * Running status
     */
    private final AtomicBoolean running = new AtomicBoolean(true);

    /**
     * List of pending server tasks.
     */
    private final ConcurrentLinkedQueue<Runnable> pendingServerTasks = new ConcurrentLinkedQueue<>();

    /**
     * The RakNet server
     */
    private CrimsonNetwork network;

    /**
     * Console reader.
     */
    private CrimsonConsoleHandler console;
    private CrimsonCommandDispatcher dispatcher;

    /**
     * Main server thread.
     */
    private Thread currentThread;

    /**
     * If the server is currently stopping.
     */
    private volatile boolean isStopping;

    public CrimsonServer() {
        log.info("Starting Crimson server version " + VERSION);
        Crimson.setServer(this);

        this.currentThread = Thread.currentThread();

        if (!loadServerProperties()) {
            shutdown();
            return;
        }

        // track how long server startup took.
        final long now = System.currentTimeMillis();
        loadPlayerBans();

        network = new CrimsonNetwork(new InetSocketAddress("127.0.0.1", 19132), new CrimsonServerEventHandler(this));
        if (!network.bind()) {
            shutdown();
            return;
        }

        createShutdownHook();

        log.info("Loading server commands...");

        this.dispatcher = new CrimsonCommandDispatcher();
        registerServerCommands();

        console = new CrimsonConsoleHandler(dispatcher);
        console.start();

        final long delta = System.currentTimeMillis() - now;
        runServer(delta);
    }

    /**
     * @return the server.properties in memory
     */
    public ServerPropertiesConfiguration getProperties() {
        return serverProperties;
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    /**
     * Load server.properties file.
     *
     * @return the result
     */
    private boolean loadServerProperties() {
        final Path path = Path.of(PATH + "server.properties");
        if (!serverProperties.generateServerPropertiesIfNeeded(path)) return false;
        return serverProperties.loadServerProperties(path);
    }

    /**
     * Initialize the {@code banLists} array and load player bans from the file.
     */
    private void loadPlayerBans() {
        banLists[BanListType.NAME.ordinal()] = new BanListByName();
        banLists[BanListType.IP.ordinal()] = new BanListByIP();
    }

    /**
     * Create a shutdown hook.
     */
    private void createShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    /**
     * Register server commands
     */
    private void registerServerCommands() {
        register(new CommandServerStop());
    }

    /**
     * Register a command
     *
     * @param command the command
     */
    private void register(ServerCommand command) {
        if (!dispatcher.registerServerCommand(command)) {
            log.warn("Failed to register command: " + command.getName());
        }
    }

    /**
     * Run the server.
     *
     * @param delta time delta
     */
    private void runServer(long delta) {
        log.info("Server startup finished, took " + delta + " ms.");

        while (running.get() && !isStopping) {
            final long now = System.currentTimeMillis();
            pollPendingTasks();

            final long time = System.currentTimeMillis() - now;
            final long sleepTime = Math.max(1L, 50L - time);

            try {
                Thread.sleep(sleepTime);
            } catch (Exception any) {
                any.printStackTrace();
            }
        }

        shutdownInternal();
    }

    /**
     * Poll pending server tasks.
     */
    private void pollPendingTasks() {
        Runnable task;
        while ((task = pendingServerTasks.poll()) != null) {
            task.run();
        }
    }

    /**
     * Shutdown
     */
    @Override
    public void shutdown() {
        isStopping = true;
    }

    /**
     * Shutdown on the main thread.
     */
    private void shutdownInternal() {
        this.isStopping = true;

        log.info("Shutting down server...");

        try {
            console.shutdown();
            network.getServer().shutdown();
        } finally {
            this.running.set(false);
            // finish up any tasks
            pollPendingTasks();

            log.info("Goodbye!");
            LogManager.shutdown();
        }
    }

    @Override
    public BanList getBanList(BanListType type) {
        return banLists[type.ordinal()];
    }
}
