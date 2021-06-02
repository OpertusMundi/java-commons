package eu.opertusmundi.common.service.contract;

import java.io.IOException;
import java.nio.file.Path;

public interface ContractFileNamingStrategy<C extends ContractNamingStrategyContext> {

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
     * @see ContractFileNamingStrategy#resolvePath(int, String)
     */
    Path resolvePath(C ctx, Path relativePath) throws IOException;

}