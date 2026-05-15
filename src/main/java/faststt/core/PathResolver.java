package faststt.core;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Intelligent path resolver for FastSTT models and native binaries.
 * Follows the FastJava ecosystem standards for path discovery.
 */
public final class PathResolver {

    private static final String DEFAULT_INSTALL_DIR = "C:\\ProgramData\\FastSTT";
    private static Properties props = new Properties();

    static {
        // Try to load properties from project root or current dir
        try (FileInputStream fis = new FileInputStream("faststt.properties")) {
            props.load(fis);
        } catch (Exception e) {
            // Fallback: try one level up (common for multi-module or examples)
            try (FileInputStream fis = new FileInputStream("../../faststt.properties")) {
                props.load(fis);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Resolves a path for a given key or default filename.
     * Order: Properties -> Local -> Installer Location -> ProgramData -> Fallback
     */
    public static String resolve(String key, String defaultFilename) {
        // 1. Check properties (explicit user configuration)
        String path = props.getProperty(key);
        if (path != null && new File(path).exists()) return path;

        // 2. Check local directory (Portable / Dev mode)
        File local = new File(defaultFilename);
        if (local.exists()) return local.getAbsolutePath();
        
        // 3. Check models/ directory (specifically for models)
        File modelLoc = new File("models/" + defaultFilename);
        if (modelLoc.exists()) return modelLoc.getAbsolutePath();

        // 4. Check standard install directory
        File standard = new File(DEFAULT_INSTALL_DIR, defaultFilename);
        if (standard.exists()) return standard.getAbsolutePath();

        return defaultFilename; // Fallback to provided name
    }

    public static String getInstallDir() {
        return props.getProperty("install.dir", DEFAULT_INSTALL_DIR);
    }
}
