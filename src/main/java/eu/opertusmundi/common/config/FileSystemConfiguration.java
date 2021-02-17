package eu.opertusmundi.common.config;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

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
    
    @PostConstruct
    private void initialize() throws IOException {
        for (final Path dataDir : Arrays.asList(this.tempDir, this.userDir, this.draftDir, this.assetDir)) {
            try {
                Files.createDirectories(dataDir);
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

}
