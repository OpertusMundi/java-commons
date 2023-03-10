package eu.opertusmundi.common.model.discovery.client;

import java.util.Optional;

import eu.opertusmundi.common.model.discovery.server.ServerJoinableTableMatchDto;
import eu.opertusmundi.common.model.discovery.server.ServerJoinableTableMatchKeysDto;
import eu.opertusmundi.common.model.discovery.server.ServerJoinableTableMatchRelatedDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientJoinableTableMatchDto {

    private ClientJoinableTableMatchKeysDto keys;

    private ClientJoinableTableMatchRelatedDto related;

    private String explanation;

    public static ClientJoinableTableMatchDto from(ServerJoinableTableMatchDto s) {
        final var c = new ClientJoinableTableMatchDto();

        c.setExplanation(s.getExplanation());
        c.setKeys(Optional.ofNullable(s.getKeys()).map(ClientJoinableTableMatchKeysDto::from).orElse(null));
        c.setRelated(Optional.ofNullable(s.getRelated()).map(ClientJoinableTableMatchRelatedDto::from).orElse(null));

        return c;
    }

    @Getter
    @Setter
    public static class ClientJoinableTableMatchKeysDto {

        private String from;

        private String to;

        public static ClientJoinableTableMatchKeysDto from(ServerJoinableTableMatchKeysDto s) {
            final var c = new ClientJoinableTableMatchKeysDto();

            c.setFrom(s.getFrom());
            c.setTo(s.getTo());

            return c;
        }
    }

    @Getter
    @Setter
    public static class ClientJoinableTableMatchRelatedDto {

        private Double coma;

        public static ClientJoinableTableMatchRelatedDto from(ServerJoinableTableMatchRelatedDto s) {
            final var c = new ClientJoinableTableMatchRelatedDto();

            c.setComa(s.getComa());

            return c;
        }

    }
}
