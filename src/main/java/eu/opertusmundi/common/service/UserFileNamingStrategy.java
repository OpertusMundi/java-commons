package eu.opertusmundi.common.service;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import eu.opertusmundi.common.model.FileNamingStrategyContext;

public interface UserFileNamingStrategy<C extends FileNamingStrategyContext> {

    /**
     * Get the URI scheme used for representing (as URIs) paths under this
     * hierarchy.
     *
     * @return a URI scheme
     */
    String getScheme();

    /**
     * Resolve a base directory as an absolute path.
     *
     * <p>
     * This method will not interact in any way with the underlying file system;
     * will simply map a context to a directory.
     *
     * @param ctx
     * @throws IOException if an attempt to create the directory fails
     */
    Path getDir(C ctx) throws IOException;

    /**
     * Resolve a path against the base directory
     *
     * @param ctx
     * @param relativePath A relative path to be resolved
     * @return an absolute path
     * @throws IOException if an attempt to create the directory fails
     */
    Path resolvePath(C ctx, String relativePath) throws IOException;

    /**
     * Resolve a path against the base directory
     *
     * @see UserFileNamingStrategy#resolvePath(int, String)
     */
    Path resolvePath(C ctx, Path relativePath) throws IOException;

    /**
     * Convert a relative path (under a user's data directory) to a pseudo-URI.
     *
     * <p>
     * The result URI will have the scheme returned by
     * {@link UserFileNamingStrategy#getScheme()}.
     *
     * @param ctx
     * @param relativePath
     * @return a URI representing the given location
     */
    URI convertToUri(C ctx, Path relativePath);

    /**
     * Convert an absolute path to a pseudo-URI.
     *
     * <p>
     * The result URI will have the scheme returned by
     * {@link UserFileNamingStrategy#getScheme()}.
     *
     * @param path A path to be converted
     * @return a URI representing the given location
     *
     * @throws IllegalArgumentException if given path cannot be represented as a URI (e.g. when not
     *                                  inside central data directory)
     */
    URI convertToUri(Path path);

    /**
     * Resolve a URI to an absolute path
     *
     * @param uri A URI under the scheme returned by {@link UserFileNamingStrategy#getScheme()}
     * @return an absolute path
     *
     * @throws IllegalArgumentException if given URI does not represent a user-scoped file (e.g
     *                                  having an unknown scheme).
     * @throws IOException if an attempt to create the directory fails
     */
    Path resolveUri(URI uri) throws IOException;

}