package org.crimson.console;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.crimson.command.CrimsonCommandDispatcher;
import org.crimson.command.sender.CommandSender;
import org.crimson.permission.CrimsonPermissions;
import org.crimson.permission.PermissionHolder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Reads incoming input from the console.
 * <p>
 * TODO: In the future, console may not have all permissions.
 */
@Log4j2
public final class CrimsonConsoleHandler implements CommandSender, Thread.UncaughtExceptionHandler {

    /**
     * Input reader.
     */
    private final BufferedReader reader;

    /**
     * Service
     */
    private final ExecutorService service;

    /**
     * Permissions for the console handler.
     */
    private final PermissionHolder holder;

    /**
     * Command dispatching.
     */
    private final CrimsonCommandDispatcher dispatcher;

    /**
     * Running state
     */
    private boolean running = true;

    /**
     * Initialize this console handler.
     */
    public CrimsonConsoleHandler(CrimsonCommandDispatcher dispatcher) {
        reader = new BufferedReader(new InputStreamReader(System.in));
        this.dispatcher = dispatcher;

        final ThreadFactory factory = new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("Crimson Console Thread")
                .setUncaughtExceptionHandler(this)
                .build();
        service = Executors.newSingleThreadExecutor(factory);

        holder = new PermissionHolder();
        holder.setOp(true);
        holder.setPermissions(CrimsonPermissions.ALL_PERMISSIONS);
    }

    /**
     * Start
     */
    public void start() {
        service.execute(this::read);
    }

    /**
     * Read
     * <p>
     * TODO: Commands are executed async
     */
    public void read() {
        while (running) {
            try {
                if (reader.ready()) {
                    final String input = reader.readLine();
                    final String command = StringUtils.replace(input, "/", "");

                    if (dispatcher.isCommand(command)) {
                        dispatcher.dispatch(command, command.split(" "), this);
                    } else {
                        sendMessage("Command not found!");
                    }
                }
            } catch (Exception any) {
                log.warn("Failed to read input from reader!", any);
            }
        }

    }

    /**
     * Shutdown
     */
    public void shutdown() {
        this.running = false;

        try {
            service.shutdown();
            reader.close();
        } catch (Exception any) {
            log.debug("Failed to close console reader {}", any.getMessage());
        }
    }

    @Override
    public void sendMessage(String message) {
        log.info(message);
    }

    @Override
    public boolean isOp() {
        return holder.isOp();
    }

    @Override
    public void setOp(boolean op) {
        holder.setOp(op);
    }

    @Override
    public boolean hasPermission(String name) {
        return holder.hasPermission(name);
    }

    @Override
    public void setPermission(String... permissions) {
        holder.setPermission(permissions);
    }

    @Override
    public void setPermissions(Collection<String> permissions) {
        holder.setPermissions(permissions);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.warn("Main console thread caught an exception!", e);
    }

}

