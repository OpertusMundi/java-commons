package eu.opertusmundi.common.model.file;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
@Getter
@Setter
public class FileCopyResourceCommandDto {

    @JsonIgnore
    protected String sourcePath;

    @JsonIgnore
    protected String targetPath;

    protected UUID   accountKey;
    protected String assetPid;
    protected String resourceKey;
    protected Long   size;

}