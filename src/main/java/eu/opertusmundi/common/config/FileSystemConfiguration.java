package eu.opertusmundi.common.config;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
public class FileSystemConfiguration {

    private Path tempDir;

    private Path userDir;

    private Path draftDir;

    private Path assetDir;

    private Path contractDir;

    private Path invoiceDir;

    private Path orderDir;

    private Path userServiceDir;

    private static final Set<PosixFilePermission> DEFAULT_DIRECTORY_PERMISSIONS = PosixFilePermissions.fromString("rwxrwxr-x");

    @Autowired
    private void setTempDir(@Value("${opertusmundi.file-system.temp-dir}") String d) {
        final Path path = Paths.get(d);
        Assert.isTrue(path.isAbsolute(), "Expected an absolute directory path");
        this.tempDir = path;
    }

    @Autowired
    private void setUserDir(@Value("${opertusmundi.file-system.data-dir}") String d) {
        final Path path = Paths.get(d);
        Assert.isTrue(path.isAbsolute(), "Expected an absolute directory path");
        this.userDir = path;
    }

    @Autowired
    private void setDraftDir(@Value("${opertusmundi.file-system.draft-dir}") String d) {
        final Path path = Paths.get(d);
        Assert.isTrue(path.isAbsolute(), "Expected an absolute directory path");
        this.draftDir = path;
    }

    @Autowired
    private void setAssetDir(@Value("${opertusmundi.file-system.asset-dir}") String d) {
        final Path path = Paths.get(d);
        Assert.isTrue(path.isAbsolute(), "Expected an absolute directory path");
        this.assetDir = path;
    }

    @Autowired
    private void setContractDir(@Value("${opertusmundi.file-system.contract-dir}") String d) {
        final Path path = Paths.get(d);
        Assert.isTrue(path.isAbsolute(), "Expected an absolute directory path");
        this.contractDir = path;
    }

    @Autowired
    private void setInvoiceDir(@Value("${opertusmundi.file-system.invoice-dir}") String d) {
        final Path path = Paths.get(d);
        Assert.isTrue(path.isAbsolute(), "Expected an absolute directory path");
        this.invoiceDir = path;
    }

    @Autowired
    private void setOrderDir(@Value("${opertusmundi.file-system.order-dir}") String d) {
        final Path path = Paths.get(d);
        Assert.isTrue(path.isAbsolute(), "Expected an absolute directory path");
        this.orderDir = path;
    }

    @Autowired
    private void setUserServiceDir(@Value("${opertusmundi.file-system.user-service-dir}") String d) {
        final Path path = Paths.get(d);
        Assert.isTrue(path.isAbsolute(), "Expected an absolute directory path");
        this.userServiceDir = path;
    }

    @PostConstruct
    private void initialize() throws IOException {
        for (final Path dataDir : Arrays.asList(
            this.assetDir,
            this.contractDir,
            this.draftDir,
            this.invoiceDir,
            this.orderDir,
            this.tempDir,
            this.userDir,
            this.userServiceDir
        )) {
            try {
                if(!dataDir.toFile().exists()) {
                    Files.createDirectories(dataDir);
                    Files.setPosixFilePermissions(dataDir, DEFAULT_DIRECTORY_PERMISSIONS);
                }
            } catch (final FileAlreadyExistsException ex) {

            }
        }
    }

    @Bean
    Path tempDirectory() {
        return this.tempDir;
    }

    @Bean
    Path userDirectory() {
        return this.userDir;
    }

    @Bean
    Path draftDirectory() {
        return this.draftDir;
    }

    @Bean
    Path assetDirectory() {
        return this.assetDir;
    }

    @Bean
    Path contractDirectory() {
        return this.contractDir;
    }

    @Bean
    Path invoiceDirectory() {
        return this.invoiceDir;
    }

    @Bean
    Path orderDirectory() {
        return this.orderDir;
    }

    @Bean
    Path userServiceDirectory() {
        return this.userServiceDir;
    }

}
