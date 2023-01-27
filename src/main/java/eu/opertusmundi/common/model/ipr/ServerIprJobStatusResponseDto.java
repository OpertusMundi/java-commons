package eu.opertusmundi.common.model.ipr;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerIprJobStatusResponseDto {

    /**
     * Request ticket
     */
    private String ticket;

    /**
     * The X-Idempotency-Key sent in the headers of the request (null if the
     * request was not associated with an idempotency key)
     */
    private String idempotencyKey;

    /**
     * Type of the request
     */
    private String requestType;

    /**
     * The timestamp of the request
     */
    private String initiated;

    /**
     * The execution time in seconds
     */
    private Long executionTime;

    /**
     * Whether the process has been completed
     */
    private boolean completed;

    /**
     * Whether the process has been completed successfully
     */
    private boolean success;

    /**
     * The error message in case of failure
     */
    private String errorMessage;

    /**
     * The resource, in case the process result is a resource
     */
    private ResourceDto resource;

    /**
     * The result of the process
     */
    private String key;

    /**
     * Error message
     */
    private String status;

    @Getter
    @Setter
    public static class ResourceDto {
        /**
         * The link to download a resource resulted from an export request; null
         * for any other type of request
         */
        private String link;

        /**
         * The relative path of the resource resulted from an export request in
         * the output directory; null for any other type of request or if copy
         * to the output directory was not requested
         */
        private String outputPath;
    }
}
