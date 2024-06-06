package com.efimchick.ifmo.io.filetree;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileTreeImpl implements FileTree {

    @Override
    public Optional<String> tree(Path path) {

        if (path == null || !Files.exists(path)) {
            return Optional.empty();
        }

        String result = "";
        String space = "";

        File file = new File(String.valueOf(path));
        if (file.isFile()) {
            result = file.getName() + " " + file.length() + " bytes";
        }

        if (file.isDirectory()) {
            result = (getHierarchy(file, path, result, "", 0, 0, true));
        }

        return Optional.of(result);

    }

    String getHierarchy(File file, Path path, String result, String pattern, int index, int depth, boolean parentIsLastChild) {

        // Base Case
        if (!file.exists()) {
            return result;
        }

        Path dir = path;
//        result += pattern;
        result += file.getName() + " " + getSize(file) + " bytes\n";
//        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
        try (DirectoryStream<Path> sortedStream = sortDirectories(dir)) {


            for (Path files : sortedStream) {
                index++;
                File ff = new File(String.valueOf(files));
                pattern += index < Objects.requireNonNull(file.listFiles()).length ? "├─ " : "└─ ";

                if (ff.isFile()) {

                    result += pattern;
                    pattern = pattern.substring(0, pattern.length() - 3);
                    result += (files.getFileName() + " " + ff.length() + " bytes\n");

                }
                if (ff.isDirectory()) {
                    depth++;
                    result += pattern;
                    parentIsLastChild = (index == Objects.requireNonNull(file.listFiles()).length);


                    pattern = pattern.substring(0, pattern.length() - 3);
                    pattern += parentIsLastChild ? "   ": "│  ";

                    result = getHierarchy(ff, files, result, pattern, 0, depth, parentIsLastChild);
                    pattern = pattern.substring(0, pattern.length() - 3);
                    depth--;
                }

            }
        } catch (IOException | DirectoryIteratorException x) {
            // IOException can never be thrown by the iteration.
            // In this snippet, it can only be thrown by newDirectoryStream.
            System.err.println(x);
        }


        return result;
    }

    long getSize(File folder) {
        long length = 0;

        File[] files = folder.listFiles();

        int count = files.length;

        // loop for traversing the directory
        for (int i = 0; i < count; i++) {
            if (files[i].isFile()) {
                length += files[i].length();
            } else {
                length += getSize(files[i]);
            }
        }
        return length;
    }

    public static DirectoryStream<Path> sortDirectories(Path dir) throws IOException {
        File file = dir.toFile();
        if (file.isDirectory()) {
            File[] fileNames = file.listFiles();
            if (fileNames != null) {
                Arrays.sort(fileNames, (file1, file2) -> {
                    if (file1.isDirectory() && !file2.isDirectory()) {
                        return -1; // file1 is a directory, file2 is not
                    } else // (!file1.isDirectory() && file2.isDirectory()) {
                    {
                        return 1;
                    } // file1 is not a directory, file2 is
                });

                // Convert the sorted File array back to a DirectoryStream<Path>
                List<Path> sortedPaths = Arrays.stream(fileNames)
                        .map(File::toPath)
                        .collect(Collectors.toList());
                return new DirectoryStream<Path>() {
                    private final List<Path> paths = sortedPaths;
                    private int index = 0;

                    @Override
                    public java.util.Iterator<Path> iterator() {
                        return new java.util.Iterator<Path>() {
                            @Override
                            public boolean hasNext() {
                                return index < paths.size();
                            }

                            @Override
                            public Path next() {
                                return paths.get(index++);
                            }
                        };
                    }

                    @Override
                    public void close() throws IOException {
                        // No resources to close in this implementation
                    }
                };
            }
        }
        return Files.newDirectoryStream(dir);
    }


}
