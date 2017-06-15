package com.github.slavetto.utils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/*
 * Created with ♥
 */
public class FileUtils {

    private FileUtils() { }

    /**
     * Removes recursively all the files that are in the folder
     * @param folder the folder to delete
     * @throws IOException if an error happens
     */
    public static void removeAllRecursively(File folder) throws IOException {
        Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>(){
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

    /**
     * Copies recursively all the files that are in one folder to another folder
     * @param from the folder to copy the files from
     * @param to the folder to copy all the files to
     * @throws IOException if an error happens
     */
    public static void copyAllRecursively(File from, File to, boolean overwriteExisting) throws IOException {
        CopyOption[] options;
        if (overwriteExisting) {
            options = new CopyOption[]{ StandardCopyOption.REPLACE_EXISTING };
        } else {
            options = new CopyOption[0];
        }

        Files.walkFileTree(from.toPath(), new CopyFileVisitor(to.toPath(), options));
    }

    /**
     * Write the stream into the specified file
     * @param from Input stream to which read from
     * @param to Destination file
     * @param overwriteExisting Should an existing file be overwritten ?
     * @throws IOException
     */
    public static void writeStreamTo (InputStream from, File to, boolean overwriteExisting) throws IOException {
        CopyOption[] options;
        if (overwriteExisting) {
            options = new CopyOption[]{ StandardCopyOption.REPLACE_EXISTING };
        } else {
            options = new CopyOption[0];
        }

        // Create missing parent directories
        if (to.getParentFile() != null) {
            to.getParentFile().mkdirs();
        }

        Files.copy(from, to.toPath(), options);
    }
}
