package com.github.slavetto.utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/*
 * Created with ♥
 */

/**
 * A file visitor that copies all the files and folder it encounters to another folder.
 */
public class CopyFileVisitor extends SimpleFileVisitor<Path> {

    private final Path targetPath;
    private CopyOption[] copyOptions;
    private Path sourcePath = null;

    public CopyFileVisitor(Path targetPath, CopyOption... copyOptions) {
        this.targetPath = targetPath;
        this.copyOptions = copyOptions;
    }

    @Override
    public FileVisitResult preVisitDirectory(final Path dir,
                                             final BasicFileAttributes attrs) throws IOException {
        if (sourcePath == null) {
            sourcePath = dir;
        } else {
            Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(final Path file,
                                     final BasicFileAttributes attrs) throws IOException {
        Files.copy(file, targetPath.resolve(sourcePath.relativize(file)), copyOptions);
        return FileVisitResult.CONTINUE;
    }
}