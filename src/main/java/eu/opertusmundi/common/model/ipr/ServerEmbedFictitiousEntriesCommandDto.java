package eu.opertusmundi.common.model.ipr;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class ServerEmbedFictitiousEntriesCommandDto {

    /**
     * Response type, either prompt to initiate the process promptly and wait
     * for the response, either deferred to finish the process asynchronously.
     */
    private String response;

    /**
     * A resolvable path to the original file, relative to the input directory.
     * The file could be in compressed form: zipped or tar(.gz) archive.
     */
    private String original;

    /**
     * In case the file is a delimited text file, the character used to separate
     * values. Ignored for not delimited files.
     */
    @JsonInclude(Include.NON_EMPTY)
    private String delimiter;

    /**
     * The attribute name in delimited text files that corresponds to latitude,
     * if the geometry is given in means of lat, lon. Ignored for not delimited
     * files.
     */
    @JsonInclude(Include.NON_EMPTY)
    private String lat;

    /**
     * The attribute name in delimited text files that corresponds to longitude,
     * if the geometry is given in means of lat, lon. Ignored for not delimited
     * files.
     */
    @JsonInclude(Include.NON_EMPTY)
    private String lon;

    /**
     * The attribute name in delimited text files that corresponds to WKT
     * geometry. Default is 'WKT'; ignored for not delimited files or when
     * 'lat', 'lon' are provided.
     */
    @JsonInclude(Include.NON_EMPTY)
    private String geom;

    /**
     * The Coordinate Reference System of the geometries. If not given, the CRS
     * information is obtained by the dataset; required for spatial files that
     * do not provide CRS information, e.g. CSV.
     */
    @JsonInclude(Include.NON_EMPTY)
    private String crs;

    /**
     * The encoding of the file. If not given, the encoding is automatically
     * detected.
     */
    @JsonInclude(Include.NON_EMPTY)
    private String encoding;

    /**
     * A unique key to embed in dataset.
     */
    @JsonProperty("uuid")
    private String key;   

}
