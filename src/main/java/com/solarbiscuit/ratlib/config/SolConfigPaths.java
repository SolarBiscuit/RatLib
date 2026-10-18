package com.solarbiscuit.ratlib.config;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * On-disk layout for SolarBiscuit configs: {@code config/SolConfig/<mod>/...}.
 * Call migrate helpers before {@code registerConfig} so existing files keep their values.
 */
public final class SolConfigPaths {
    public static final String ROOT = "SolConfig";
    private static final Logger LOGGER = LogUtils.getLogger();

    private SolConfigPaths() {}

    public static String folder(String folder) {
        return ROOT + "/" + folder;
    }

    public static String file(String folder, String fileName) {
        return folder(folder) + "/" + fileName;
    }

    public static Path resolve(String relative) {
        return FMLPaths.CONFIGDIR.get().resolve(relative);
    }

    public static void migrateFile(String oldRelative, String newRelative) {
        Path source = resolve(oldRelative);
        Path dest = resolve(newRelative);
        if (Files.exists(dest) || !Files.isRegularFile(source)) {
            return;
        }
        move(source, dest);
    }

    public static void migrateDirectory(String oldRelative, String newRelative) {
        Path source = resolve(oldRelative);
        Path dest = resolve(newRelative);
        if (Files.exists(dest) || !Files.isDirectory(source)) {
            return;
        }
        move(source, dest);
    }

    private static void move(Path source, Path dest) {
        try {
            Path parent = dest.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.move(source, dest);
            LOGGER.info("Moved config {} to {}", source, dest);
        } catch (IOException moveFailed) {
            try {
                copyRecursive(source, dest);
                deleteRecursive(source);
                LOGGER.info("Copied config {} to {}", source, dest);
            } catch (IOException copyFailed) {
                LOGGER.warn("Could not migrate config {} to {}: {}", source, dest, copyFailed.toString());
            }
        }
    }

    private static void copyRecursive(Path source, Path dest) throws IOException {
        if (Files.isDirectory(source)) {
            Files.createDirectories(dest);
            try (var stream = Files.list(source)) {
                for (Path child : stream.toList()) {
                    copyRecursive(child, dest.resolve(child.getFileName()));
                }
            }
            return;
        }
        Path parent = dest.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void deleteRecursive(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (var stream = Files.list(path)) {
                for (Path child : stream.toList()) {
                    deleteRecursive(child);
                }
            }
        }
        Files.deleteIfExists(path);
    }
}
