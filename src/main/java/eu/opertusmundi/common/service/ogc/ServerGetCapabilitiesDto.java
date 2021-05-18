package eu.opertusmundi.common.service.ogc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import eu.opertusmundi.common.util.StreamUtils;
import lombok.Getter;
import lombok.Setter;

@JacksonXmlRootElement(namespace = "wfs", localName = "WFS_Capabilities")
@Getter
@Setter
public class ServerGetCapabilitiesDto {

    @Getter
    @Setter
    public static class OperationsMetadata {

        @JacksonXmlProperty(namespace = "ows", localName = "Operation")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<Operation> operations;
    }

    @Getter
    @Setter
    public static class Operation {

        @JacksonXmlProperty(isAttribute = true)
        private String name;

        @JacksonXmlProperty(namespace = "ows", localName = "Parameter")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<Parameter> parameters;

    }

    @Getter
    @Setter
    public static class Parameter {

        @JacksonXmlProperty(isAttribute = true)
        private String name;

        @JacksonXmlProperty(namespace = "ows", localName = "AllowedValues")
        @JacksonXmlElementWrapper(useWrapping = true)
        private List<AllowedValue> allowedValues;

    }

    @Getter
    @Setter
    public static class AllowedValue {

        @JacksonXmlText
        private String value;

    }

    @Getter
    @Setter
    public static class FilterCapabilities {

        @JacksonXmlProperty(namespace = "fes", localName = "Scalar_Capabilities")
        private ScalarCapabilities scalarCapabilities;

        @JacksonXmlProperty(namespace = "fes", localName = "Spatial_Capabilities")
        private SpatialCapabilities spatialCapabilities;

        @JacksonXmlProperty(namespace = "fes", localName = "Temporal_Capabilities")
        private TemporalCapabilities temporalCapabilities;
    }

    @Getter
    @Setter
    public static class ScalarCapabilities {

        @JacksonXmlProperty(namespace = "fes", localName = "ComparisonOperators")
        @JacksonXmlElementWrapper(useWrapping = true)
        private List<ComparisonOperator> comparisonOperators;

    }

    @Getter
    @Setter
    public static class ComparisonOperator {

        @JacksonXmlProperty(isAttribute = true)
        private String name;

    }

    @Getter
    @Setter
    public static class SpatialCapabilities {

        @JacksonXmlProperty(namespace = "fes", localName = "SpatialOperators")
        @JacksonXmlElementWrapper(useWrapping = true)
        private List<SpatialOperator> spatialOperators;

    }

    @Getter
    @Setter
    public static class SpatialOperator {

        @JacksonXmlProperty(isAttribute = true)
        private String name;

    }

    @Getter
    @Setter
    public static class TemporalCapabilities {

        @JacksonXmlProperty(namespace = "fes", localName = "TemporalOperators")
        @JacksonXmlElementWrapper(useWrapping = true)
        private List<TemporalOperator> temporalOperators;

    }

    @Getter
    @Setter
    public static class TemporalOperator  {

        @JacksonXmlProperty(isAttribute = true)
        private String name;

    }

    @Getter
    @Setter
    public static class FeatureTypeList {

        @JacksonXmlProperty(localName = "FeatureType")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<FeatureType> featureTypes;

    }

    @Getter
    @Setter
    public static class FeatureType {

        @JacksonXmlProperty(localName = "Name")
        private FeatureTypeName name;

        @JacksonXmlProperty(localName = "DefaultCRS")
        private DefaultCrs defaultCrs;

        @JacksonXmlProperty(localName = "OtherCRS")
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<OtherCrs> otherCrs;

        @JacksonXmlProperty(namespace = "ows", localName = "WGS84BoundingBox")
        private WGS84BoundingBox bbox;

    }

    @Getter
    @Setter
    public static class FeatureTypeName {

        @JacksonXmlText
        private String value;

    }

    @Getter
    @Setter
    public static class DefaultCrs {

        @JacksonXmlText
        private String value;

    }

    @Getter
    @Setter
    public static class OtherCrs {

        @JacksonXmlText
        private String value;

    }

    @Getter
    @Setter
    public static class WGS84BoundingBox {

        @JacksonXmlProperty(namespace = "ows", localName = "LowerCorner")
        private Coordinate lowerCorner;

        @JacksonXmlProperty(namespace = "ows", localName = "UpperCorner")
        private Coordinate upperCorner;

    }

    @Getter
    @Setter
    public static class Coordinate {

        public Coordinate(String value) {
            this.value = value;
        }

        private String value;

        public List<Double> getCoordinates() {
            return Arrays.asList(value.split(" ")).stream().map(Double::parseDouble).collect(Collectors.toList());
        }

    }

    @JacksonXmlProperty(namespace = "ows", localName = "OperationsMetadata")
    private OperationsMetadata operationsMetadata;

    @JacksonXmlProperty(namespace = "fes", localName = "Filter_Capabilities")
    private FilterCapabilities filterCapabilities;

    @JacksonXmlProperty(localName = "FeatureTypeList")
    private FeatureTypeList featureTypeList;

    public WGS84BoundingBox getBoundingBox(String workspace, String typeName) {
        if (this.featureTypeList == null) {
            return null;
        }
        final String name = workspace + ":" + typeName;

        final FeatureType type = this.featureTypeList.featureTypes.stream()
            .filter(f -> f.name.getValue().equals(name))
            .findFirst()
            .orElse(null);
        if (type == null) {
            return null;
        }
        return type.getBbox();
    }

    public List<String> getCrs(String workspace, String typeName) {
        if (this.featureTypeList == null) {
            return null;
        }
        final String name = workspace + ":" + typeName;

        final FeatureType type = this.featureTypeList.featureTypes.stream()
            .filter(f -> f.name.getValue().equals(name))
            .findFirst()
            .orElse(null);
        if (type == null) {
            return null;
        }
        if (type.getDefaultCrs() == null) {
            return Collections.emptyList();
        }
        final List<String> result = new ArrayList<String>();

        result.add(type.getDefaultCrs().getValue());

        if (type.getOtherCrs() != null) {
            type.getOtherCrs().stream().forEach(c -> result.add(c.value));
        }
        return result;
    }

    public List<String> getFilterCapabilities() {
        final List<String> result = new ArrayList<>();

        if (this.filterCapabilities != null && this.filterCapabilities.scalarCapabilities != null) {
            StreamUtils.from(this.filterCapabilities.scalarCapabilities.comparisonOperators)
                .forEach(o -> result.add("comparison:" + o.name));
        }

        if (this.filterCapabilities != null && this.filterCapabilities.spatialCapabilities != null) {
            StreamUtils.from(this.filterCapabilities.spatialCapabilities.spatialOperators)
                .forEach(o -> result.add("spatial:" + o.name));
        }

        if (this.filterCapabilities != null && this.filterCapabilities.temporalCapabilities != null) {
            StreamUtils.from(this.filterCapabilities.temporalCapabilities.temporalOperators)
                .forEach(o -> result.add("temporal:" + o.name));
        }

        return result;
    }

    public List<String> getOutputFormats() {
        if (this.operationsMetadata == null || this.operationsMetadata.operations == null) {
            return Collections.emptyList();
        }
        final Operation getFeature = this.operationsMetadata.operations.stream()
            .filter(o -> o.name.equals("GetFeature"))
            .findFirst()
            .orElse(null);
        if (getFeature == null) {
            return Collections.emptyList();
        }
        final Parameter outputFormat = getFeature.getParameters().stream()
            .filter(p -> p.name.equals("outputFormat"))
            .findFirst()
            .orElse(null);
        if (outputFormat == null) {
            return Collections.emptyList();
        }
        return StreamUtils.from(outputFormat.allowedValues).map(v -> v.value).collect(Collectors.toList());
    }

}
