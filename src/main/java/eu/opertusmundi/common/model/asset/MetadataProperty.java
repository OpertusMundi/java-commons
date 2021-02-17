package eu.opertusmundi.common.model.asset;

import java.nio.file.Path;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor(staticName = "of")
@Getter
@Setter
public class MetadataProperty {

    final EnumMetadataPropertyType type;

    final Path path;
}
