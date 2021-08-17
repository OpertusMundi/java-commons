package eu.opertusmundi.common.model.catalogue.elastic;

import org.elasticsearch.common.geo.ShapeRelation;

public enum EnumSpatialOperation {
    INTERSECTS,
    WITHIN,
    CONTAINS,
    ;

    public ShapeRelation toShapeRelation() throws IllegalArgumentException {
        switch (this) {
            case INTERSECTS :
                return ShapeRelation.INTERSECTS;
            case WITHIN :
                return ShapeRelation.WITHIN;
            case CONTAINS :
                return ShapeRelation.CONTAINS;
            default :
                throw new IllegalArgumentException(String.format("Invalid mapping [value=%s]", this));
        }
    }
}
