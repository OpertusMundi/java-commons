package eu.opertusmundi.common.model.sinergise;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class QueryDto {

    /**
     * Find items with a property that is equal to the specified value. For
     * strings, a case-insensitive comparison is performed.
     */
    @JsonInclude(Include.NON_NULL)
    private Object eq;

    /**
     * Find items that don't contain the specified value. For strings, a
     * case-insensitive comparison is performed.
     */
    @JsonInclude(Include.NON_NULL)
    private Object neq;

    /**
     * Find items with a property value greater than the specified value.
     */
    @JsonInclude(Include.NON_NULL)
    private Object gt;

    /**
     * Find items with a property value less than the specified value.
     */
    @JsonInclude(Include.NON_NULL)
    private Object lt;

    /**
     * Find items with a property value greater than or equal the specified
     * value.
     */
    @JsonInclude(Include.NON_NULL)
    private Object gte;

    /**
     * Find items with a property value less than or equal the specified value.
     */
    @JsonInclude(Include.NON_NULL)
    private Object lte;

    /**
     * Find items with a property that begins with the specified string. A
     * case-insensitive comparison is performed.
     */
    @JsonInclude(Include.NON_NULL)
    private String startsWith;

    /**
     * Find items with a property that ends with the specified string. A
     * case-insensitive comparison is performed.
     */
    @JsonInclude(Include.NON_NULL)
    private String endsWith;

    /**
     * Find items with a property that contains the specified literal string,
     * e.g., matches ".*.*". A case-insensitive comparison is performed.
     */
    @JsonInclude(Include.NON_NULL)
    private String contains;

    /**
     * Find items with a property that equals at least one entry in the
     * specified array. A case-insensitive comparison must be performed.
     */
    @JsonInclude(Include.NON_NULL)
    private Object[] in;

}
