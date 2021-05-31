package eu.opertusmundi.common.model.message.server;

import java.util.UUID;

import eu.opertusmundi.common.model.message.EnumMessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ServerBaseMessageCommandDto {

    protected EnumMessageType type;

    protected UUID recipient;

    protected String text;

}
