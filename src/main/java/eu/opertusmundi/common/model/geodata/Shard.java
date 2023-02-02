package eu.opertusmundi.common.model.geodata;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Shard {

    /**
     * The geodata shard unique identifier e.g. `s1`
     */
    private String id;

    /**
     * The absolute URL of the geodata shard
     */
    private String endpoint;

    private PostGis postGis;

    @Getter
    @Setter
    public static class PostGis {
        private String url;
        private String driver = "org.postgresql.Driver";
        private String userName;
        private String password;
    }

}
