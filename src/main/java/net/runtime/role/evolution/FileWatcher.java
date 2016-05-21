package net.runtime.role.evolution;

import net.runtime.role.registry.RegistryManager;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by nguonly on 10/29/15.
 */
public class FileWatcher {
    private static FileWatcher watcherService;

    private final WatchService watcher;
    private final Map<WatchKey,Path> keys;
    private boolean recursive=false;
    private boolean trace = false;

    private Deque<String> files;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    private static final ReentrantLock lock = new ReentrantLock();

    public static FileWatcher getInstance() throws IOException {
        lock.lock();
        try {
            if (watcherService == null) {
                watcherService = new FileWatcher();
            }
        }finally {
            lock.unlock();
        }

        return watcherService;
    }

    /**
     * Register the given directory with the WatchService
     */
    public void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        System.out.format("register: %s\n", dir);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    public void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * File name to be monitor in the registered watching directory
     * @param file
     */
    public void monitor(String file){
        this.files.push(file);

        files.forEach((v)->System.out.println(v));
    }

    public FileWatcher() throws IOException{
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.files = new ArrayDeque<>();
    }
    /**
     * Creates a WatchService and registers the given directory
     */
    public FileWatcher(Path dir, boolean recursive) throws IOException{
        this();
        this.recursive = recursive;

        try {
            if (recursive) {
                System.out.format("Scanning %s ...\n", dir);
                registerAll(dir);
                System.out.println("Done.");
            } else {
                register(dir);
            }
        }catch(IOException ioe){
            ioe.printStackTrace();
        }

        // enable trace after initial registration
        this.trace = true;
    }

    /**
     * Process all events for keys queued to the watcher
     */
    public void processEvents() {
        for (;;) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                if(kind == ENTRY_MODIFY && files.contains(name.toString())) {
                    // print out event
                    System.out.format("%s: %s\n", event.kind().name(), child);

                    if(child.toString().endsWith(".xml")) {
                        RegistryManager.getInstance().evolve(child.toString());
                    }

                }

                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                            registerAll(child);
                        }
                    } catch (IOException x) {
                        // ignore to keep sample readable
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    static void usage() {
        System.err.println("usage: java FileWatcher [-r] dir");
        System.exit(-1);
    }

    public static void main(String[] args) throws IOException {
        // parse arguments
        if (args.length == 0 || args.length > 2)
            usage();
        boolean recursive = false;
        int dirArg = 0;
        if (args[0].equals("-r")) {
            if (args.length < 2)
                usage();
            recursive = true;
            dirArg++;
        }

        // register directory and process its events
        Path dir = Paths.get(args[dirArg]);
        new FileWatcher(dir, recursive).processEvents();
    }
}
