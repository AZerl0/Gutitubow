package com.azerl0.gutitubow.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public final class WorldFileUtil {


    public static void copyDirectory(Path source, Path target) throws IOException {

        Files.walkFileTree(source, new SimpleFileVisitor<>() {

            @Override
            public @NotNull FileVisitResult preVisitDirectory(@NotNull Path dir, @NotNull BasicFileAttributes attrs)
                    throws IOException {

                Path targetDir = target.resolve(source.relativize(dir));

                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs)
                    throws IOException {

                String fileName = file.getFileName().toString();

                if (fileName.equals("uid.dat") || fileName.equals("session.lock") || fileName.equals("metadata.dat")) {
                    return FileVisitResult.CONTINUE;
                }

                Path targetFile = target.resolve(source.relativize(file));

                Files.copy(
                        file,
                        targetFile,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES
                );

                return FileVisitResult.CONTINUE;
            }

        });
    }

    public static void deleteDirectory(@NotNull Path directory) throws IOException {

        if (!Files.exists(directory)) {
            return;
        }

        Files.walkFileTree(directory, new SimpleFileVisitor<>() {

            @Override
            public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs)
                    throws IOException {

                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public @NotNull FileVisitResult postVisitDirectory(@NotNull Path dir, IOException exc)
                    throws IOException {

                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }

}