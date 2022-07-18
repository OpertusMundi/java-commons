package eu.opertusmundi.common.model.message.client;

import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ClientMessageCommandDto {

    private String subject;

    @NotEmpty
    private String text;

}
