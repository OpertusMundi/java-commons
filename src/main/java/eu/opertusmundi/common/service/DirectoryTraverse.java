package eu.opertusmundi.common.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Predicate;

import eu.opertusmundi.common.model.file.DirectoryDto;

public interface DirectoryTraverse
{
    /**
     * @see DirectoryTraverse#getDirectoryInfo(Path, int, Predicate)
     */
    DirectoryDto getDirectoryInfo(Path rootDir) throws IOException;

    /**
     * @see DirectoryTraverse#getDirectoryInfo(Path, int, Predicate)
     */
    DirectoryDto getDirectoryInfo(Path rootDir, int maxDepth) throws IOException;

    /**
     * @see DirectoryTraverse#getDirectoryInfo(Path, int, Predicate)
     */
    DirectoryDto getDirectoryInfo(Path rootDir, Predicate<String> namePredicate) throws IOException;
    
    /**
     * Traverse directory entries (recursively) and collect detailed information on
     * file-system entries (files and nested directories).
     *
     * @param rootDir The root directory of this traversal
     * @param maxDepth A maximum depth to descend
     * @param namePredicate A predicate that filters names to be included (directories or files)
     * @throws IOException
     */
    DirectoryDto getDirectoryInfo(Path rootDir, int maxDepth, Predicate<String> namePredicate) throws IOException;

}