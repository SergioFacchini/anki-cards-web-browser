package com.github.slavetto.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/*
 * Created with ♥
 */
public class FileUtils {

    /**
     * Removes recursively all the files that are in the folder
     * @param folder the folder to delete
     * @throws IOException if an error happens
     */
    public static void removeAllRecursively(File folder) throws IOException {
        java.nio.file.Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                java.nio.file.Files.delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                java.nio.file.Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }
}
