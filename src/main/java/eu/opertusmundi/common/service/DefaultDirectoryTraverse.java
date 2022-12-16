package eu.opertusmundi.common.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiPredicate;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.file.DirectoryDto;
import eu.opertusmundi.common.model.file.FileDto;

@Service
public class DefaultDirectoryTraverse implements DirectoryTraverse {

    private static final int MAX_DEPTH = 6;

    @Override
    public DirectoryDto getDirectoryInfo(Path rootDir) throws IOException {
        return this.getDirectoryInfo(rootDir, 0, null);
    }

    @Override
    public DirectoryDto getDirectoryInfo(Path rootDir, int maxDepth) throws IOException {
        return this.getDirectoryInfo(rootDir, maxDepth, null);
    }

    @Override
    public DirectoryDto getDirectoryInfo(Path rootDir, BiPredicate<Integer, String> namePredicate) throws IOException {
        return this.getDirectoryInfo(rootDir, 0, namePredicate);
    }

    @Override
    public DirectoryDto getDirectoryInfo(Path rootDir, int maxDepth, BiPredicate<Integer, String> namePredicate) throws IOException {
        Assert.notNull(rootDir, "A path is required");
        Assert.isTrue(rootDir.isAbsolute(), "The directory is expected as an absolute path");
        Assert.isTrue(Files.isDirectory(rootDir), "The given path is not a directory");
        Assert.isTrue(maxDepth < MAX_DEPTH, "The depth must be less than MAX_DEPTH");

        return this.createDirectoryInfo("/", rootDir, Paths.get("/"), maxDepth, namePredicate);
    }

    private DirectoryDto createDirectoryInfo(String name, Path dir, Path relativePath, int depth, BiPredicate<Integer, String> namePredicate)
    {
        final File dirAsFile = dir.toFile();
        final DirectoryDto di = new DirectoryDto(name, relativePath.toString(), dirAsFile.lastModified());

        for (final File entry : dirAsFile.listFiles()) {
            final String entryName = entry.getName();
            if (namePredicate != null && !namePredicate.test(depth, entryName)) {
                continue;
            }
            final Path relativeEntryPath = relativePath.resolve(entryName);
            if (entry.isDirectory()) {
                if (depth < MAX_DEPTH) {
                    // Descend
                    di.addDirectory(this.createDirectoryInfo(entryName, entry.toPath(), relativeEntryPath, depth + 1, namePredicate));
                } else {
                    // No more recursion is allowed: simply report a directory entry
                    di.addDirectory(new DirectoryDto(entryName, entry.getPath(), entry.lastModified()));
                }
            } else if (entry.isFile()) {
                di.addFile(new FileDto(entryName, relativeEntryPath.toString(), entry.length(), entry.lastModified()));
            }
        }

        return di;
    }

}