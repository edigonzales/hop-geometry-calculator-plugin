package ch.so.agi.hop.geometry.calculator.core;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Lineal;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygonal;
import org.locationtech.jts.operation.valid.IsSimpleOp;
import org.locationtech.jts.operation.valid.IsValidOp;
import org.locationtech.jts.operation.valid.TopologyValidationError;

public final class GeometryCalculatorFunctionRegistry {

  private static final Set<GeometryCalculatorOutputType> DECIMAL_OUTPUTS =
      Set.of(GeometryCalculatorOutputType.NUMBER, GeometryCalculatorOutputType.STRING);
  private static final Set<GeometryCalculatorOutputType> COUNT_OUTPUTS =
      Set.of(
          GeometryCalculatorOutputType.INTEGER,
          GeometryCalculatorOutputType.NUMBER,
          GeometryCalculatorOutputType.STRING);
  private static final Set<GeometryCalculatorOutputType> BOOLEAN_OUTPUTS =
      Set.of(GeometryCalculatorOutputType.BOOLEAN, GeometryCalculatorOutputType.STRING);

  private static final List<GeometryCalculatorFunction> FUNCTIONS =
      List.of(
          function(
              GeometryCalculatorOperationId.AREA,
              "Area",
              "Returns the area of a non-empty polygonal geometry.",
              "Polygon / MultiPolygon",
              GeometryCalculatorCategory.MEASUREMENTS,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> geometry instanceof Polygonal && !geometry.isEmpty(),
              Geometry::getArea),
          function(
              GeometryCalculatorOperationId.LENGTH,
              "Length",
              "Returns the length of a non-empty lineal geometry or polygon boundary length.",
              "LineString / MultiLineString / Polygon / MultiPolygon",
              GeometryCalculatorCategory.MEASUREMENTS,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> (geometry instanceof Lineal || geometry instanceof Polygonal) && !geometry.isEmpty(),
              Geometry::getLength),
          function(
              GeometryCalculatorOperationId.PERIMETER,
              "Perimeter",
              "Returns the perimeter of a non-empty polygonal geometry.",
              "Polygon / MultiPolygon",
              GeometryCalculatorCategory.MEASUREMENTS,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> geometry instanceof Polygonal && !geometry.isEmpty(),
              geometry -> geometry.getBoundary().getLength()),
          function(
              GeometryCalculatorOperationId.WIDTH,
              "Width",
              "Returns the width of the geometry envelope, or null for empty geometries.",
              "Any parseable geometry",
              GeometryCalculatorCategory.EXTENT,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> true,
              geometry -> geometry.isEmpty() ? null : geometry.getEnvelopeInternal().getWidth()),
          function(
              GeometryCalculatorOperationId.HEIGHT,
              "Height",
              "Returns the height of the geometry envelope, or null for empty geometries.",
              "Any parseable geometry",
              GeometryCalculatorCategory.EXTENT,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> true,
              geometry -> geometry.isEmpty() ? null : geometry.getEnvelopeInternal().getHeight()),
          function(
              GeometryCalculatorOperationId.X,
              "X",
              "Returns the X coordinate of a non-empty point geometry.",
              "Point",
              GeometryCalculatorCategory.COORDINATES,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> geometry instanceof Point && !geometry.isEmpty(),
              geometry -> ((Point) geometry).getX()),
          function(
              GeometryCalculatorOperationId.Y,
              "Y",
              "Returns the Y coordinate of a non-empty point geometry.",
              "Point",
              GeometryCalculatorCategory.COORDINATES,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> geometry instanceof Point && !geometry.isEmpty(),
              geometry -> ((Point) geometry).getY()),
          function(
              GeometryCalculatorOperationId.Z,
              "Z",
              "Returns the Z coordinate of a non-empty point geometry when present.",
              "Point",
              GeometryCalculatorCategory.COORDINATES,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> geometry instanceof Point && !geometry.isEmpty(),
              geometry -> {
                double z = ((Point) geometry).getCoordinate().getZ();
                return Double.isNaN(z) ? null : z;
              }),
          function(
              GeometryCalculatorOperationId.CENTROID_X,
              "Centroid X",
              "Returns the X coordinate of the centroid for a non-empty valid geometry.",
              "Any non-empty valid geometry",
              GeometryCalculatorCategory.COORDINATES,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> !geometry.isEmpty() && geometry.isValid(),
              geometry -> geometry.getCentroid().getX()),
          function(
              GeometryCalculatorOperationId.CENTROID_Y,
              "Centroid Y",
              "Returns the Y coordinate of the centroid for a non-empty valid geometry.",
              "Any non-empty valid geometry",
              GeometryCalculatorCategory.COORDINATES,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> !geometry.isEmpty() && geometry.isValid(),
              geometry -> geometry.getCentroid().getY()),
          function(
              GeometryCalculatorOperationId.INTERIOR_POINT_X,
              "Interior Point X",
              "Returns the X coordinate of the geometry interior point, or null for empty geometries.",
              "Any parseable geometry",
              GeometryCalculatorCategory.COORDINATES,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> true,
              geometry -> geometry.isEmpty() ? null : geometry.getInteriorPoint().getX()),
          function(
              GeometryCalculatorOperationId.INTERIOR_POINT_Y,
              "Interior Point Y",
              "Returns the Y coordinate of the geometry interior point, or null for empty geometries.",
              "Any parseable geometry",
              GeometryCalculatorCategory.COORDINATES,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> true,
              geometry -> geometry.isEmpty() ? null : geometry.getInteriorPoint().getY()),
          function(
              GeometryCalculatorOperationId.XMIN,
              "X Min",
              "Returns the minimum X value of the geometry envelope.",
              "Any non-empty geometry",
              GeometryCalculatorCategory.EXTENT,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> !geometry.isEmpty(),
              geometry -> geometry.getEnvelopeInternal().getMinX()),
          function(
              GeometryCalculatorOperationId.YMIN,
              "Y Min",
              "Returns the minimum Y value of the geometry envelope.",
              "Any non-empty geometry",
              GeometryCalculatorCategory.EXTENT,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> !geometry.isEmpty(),
              geometry -> geometry.getEnvelopeInternal().getMinY()),
          function(
              GeometryCalculatorOperationId.XMAX,
              "X Max",
              "Returns the maximum X value of the geometry envelope.",
              "Any non-empty geometry",
              GeometryCalculatorCategory.EXTENT,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> !geometry.isEmpty(),
              geometry -> geometry.getEnvelopeInternal().getMaxX()),
          function(
              GeometryCalculatorOperationId.YMAX,
              "Y Max",
              "Returns the maximum Y value of the geometry envelope.",
              "Any non-empty geometry",
              GeometryCalculatorCategory.EXTENT,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> !geometry.isEmpty(),
              geometry -> geometry.getEnvelopeInternal().getMaxY()),
          function(
              GeometryCalculatorOperationId.GEOMETRY_TYPE,
              "Geometry Type",
              "Returns the JTS geometry type name.",
              "Any parseable geometry",
              GeometryCalculatorCategory.STRUCTURE,
              GeometryCalculatorOutputType.STRING,
              Set.of(GeometryCalculatorOutputType.STRING),
              geometry -> true,
              Geometry::getGeometryType),
          function(
              GeometryCalculatorOperationId.NUM_POINTS,
              "Num Points",
              "Returns the number of coordinates in the geometry.",
              "Any parseable geometry",
              GeometryCalculatorCategory.STRUCTURE,
              GeometryCalculatorOutputType.INTEGER,
              COUNT_OUTPUTS,
              geometry -> true,
              geometry -> Long.valueOf(geometry.getNumPoints())),
          function(
              GeometryCalculatorOperationId.NUM_GEOMETRIES,
              "Num Geometries",
              "Returns the number of top-level geometries in the value.",
              "Any parseable geometry",
              GeometryCalculatorCategory.STRUCTURE,
              GeometryCalculatorOutputType.INTEGER,
              COUNT_OUTPUTS,
              geometry -> true,
              geometry -> Long.valueOf(geometry.getNumGeometries())),
          function(
              GeometryCalculatorOperationId.IS_EMPTY,
              "Is Empty",
              "Returns whether the geometry is empty.",
              "Any parseable geometry",
              GeometryCalculatorCategory.STRUCTURE,
              GeometryCalculatorOutputType.BOOLEAN,
              BOOLEAN_OUTPUTS,
              geometry -> true,
              Geometry::isEmpty),
          function(
              GeometryCalculatorOperationId.IS_VALID,
              "Is Valid",
              "Returns the JTS validity flag for the geometry.",
              "Any parseable geometry",
              GeometryCalculatorCategory.STRUCTURE,
              GeometryCalculatorOutputType.BOOLEAN,
              BOOLEAN_OUTPUTS,
              geometry -> true,
              Geometry::isValid),
          function(
              GeometryCalculatorOperationId.IS_VALID_REASON,
              "Is Valid Reason",
              "Returns the JTS validation reason for an invalid geometry, otherwise null.",
              "Any parseable geometry",
              GeometryCalculatorCategory.STRUCTURE,
              GeometryCalculatorOutputType.STRING,
              Set.of(GeometryCalculatorOutputType.STRING),
              geometry -> true,
              GeometryCalculatorFunctionRegistry::validationReason),
          function(
              GeometryCalculatorOperationId.IS_VALID_ERROR_TYPE,
              "Is Valid Error Type",
              "Returns a stable symbolic JTS validation error type for an invalid geometry, otherwise null.",
              "Any parseable geometry",
              GeometryCalculatorCategory.STRUCTURE,
              GeometryCalculatorOutputType.STRING,
              Set.of(GeometryCalculatorOutputType.STRING),
              geometry -> true,
              GeometryCalculatorFunctionRegistry::validationErrorType),
          function(
              GeometryCalculatorOperationId.IS_VALID_LOCATION_X,
              "Is Valid Location X",
              "Returns the X coordinate of the JTS validation error location, otherwise null.",
              "Any parseable geometry",
              GeometryCalculatorCategory.COORDINATES,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> true,
              GeometryCalculatorFunctionRegistry::validationLocationX),
          function(
              GeometryCalculatorOperationId.IS_VALID_LOCATION_Y,
              "Is Valid Location Y",
              "Returns the Y coordinate of the JTS validation error location, otherwise null.",
              "Any parseable geometry",
              GeometryCalculatorCategory.COORDINATES,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> true,
              GeometryCalculatorFunctionRegistry::validationLocationY),
          function(
              GeometryCalculatorOperationId.IS_SIMPLE,
              "Is Simple",
              "Returns the JTS simplicity flag for the geometry.",
              "Any parseable geometry",
              GeometryCalculatorCategory.STRUCTURE,
              GeometryCalculatorOutputType.BOOLEAN,
              BOOLEAN_OUTPUTS,
              geometry -> true,
              IsSimpleOp::isSimple),
          function(
              GeometryCalculatorOperationId.IS_SIMPLE_LOCATION_X,
              "Is Simple Location X",
              "Returns the X coordinate of the non-simple location, otherwise null.",
              "Any parseable geometry",
              GeometryCalculatorCategory.COORDINATES,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> true,
              GeometryCalculatorFunctionRegistry::nonSimpleLocationX),
          function(
              GeometryCalculatorOperationId.IS_SIMPLE_LOCATION_Y,
              "Is Simple Location Y",
              "Returns the Y coordinate of the non-simple location, otherwise null.",
              "Any parseable geometry",
              GeometryCalculatorCategory.COORDINATES,
              GeometryCalculatorOutputType.NUMBER,
              DECIMAL_OUTPUTS,
              geometry -> true,
              GeometryCalculatorFunctionRegistry::nonSimpleLocationY),
          function(
              GeometryCalculatorOperationId.SRID,
              "SRID",
              "Returns the positive SRID value or null when the SRID is missing.",
              "Any parseable geometry",
              GeometryCalculatorCategory.METADATA,
              GeometryCalculatorOutputType.INTEGER,
              COUNT_OUTPUTS,
              geometry -> true,
              geometry -> geometry.getSRID() > 0 ? Long.valueOf(geometry.getSRID()) : null));

  private GeometryCalculatorFunctionRegistry() {}

  public static List<GeometryCalculatorFunction> list() {
    return FUNCTIONS.stream()
        .sorted(
            Comparator.comparing((GeometryCalculatorFunction function) -> function.category().getLabel())
                .thenComparing(GeometryCalculatorFunction::label))
        .toList();
  }

  public static Optional<GeometryCalculatorFunction> find(String operationId) {
    GeometryCalculatorOperationId id = GeometryCalculatorOperationId.fromCode(operationId);
    if (id == null) {
      return Optional.empty();
    }
    return find(id);
  }

  public static Optional<GeometryCalculatorFunction> find(GeometryCalculatorOperationId operationId) {
    return FUNCTIONS.stream().filter(function -> function.id() == operationId).findFirst();
  }

  private static GeometryCalculatorFunction function(
      GeometryCalculatorOperationId id,
      String label,
      String description,
      String geometryHint,
      GeometryCalculatorCategory category,
      GeometryCalculatorOutputType defaultOutputType,
      Set<GeometryCalculatorOutputType> allowedOutputTypes,
      java.util.function.Predicate<Geometry> supports,
      SimpleGeometryCalculatorFunction.Evaluator evaluator) {
    return new SimpleGeometryCalculatorFunction(
        id,
        label,
        description,
        geometryHint,
        category,
        defaultOutputType,
        allowedOutputTypes,
        supports,
        evaluator);
  }

  private static String validationReason(Geometry geometry) {
    TopologyValidationError error = validationError(geometry);
    return error == null ? null : error.getMessage();
  }

  private static String validationErrorType(Geometry geometry) {
    TopologyValidationError error = validationError(geometry);
    return error == null ? null : validationErrorTypeName(error.getErrorType());
  }

  private static Double validationLocationX(Geometry geometry) {
    return coordinateX(validationCoordinate(geometry));
  }

  private static Double validationLocationY(Geometry geometry) {
    return coordinateY(validationCoordinate(geometry));
  }

  private static Double nonSimpleLocationX(Geometry geometry) {
    return coordinateX(IsSimpleOp.getNonSimpleLocation(geometry));
  }

  private static Double nonSimpleLocationY(Geometry geometry) {
    return coordinateY(IsSimpleOp.getNonSimpleLocation(geometry));
  }

  private static TopologyValidationError validationError(Geometry geometry) {
    return new IsValidOp(geometry).getValidationError();
  }

  private static Coordinate validationCoordinate(Geometry geometry) {
    TopologyValidationError error = validationError(geometry);
    return error == null ? null : error.getCoordinate();
  }

  private static Double coordinateX(Coordinate coordinate) {
    return coordinate == null ? null : coordinate.getX();
  }

  private static Double coordinateY(Coordinate coordinate) {
    return coordinate == null ? null : coordinate.getY();
  }

  private static String validationErrorTypeName(int errorType) {
    return switch (errorType) {
      case TopologyValidationError.ERROR -> "ERROR";
      case TopologyValidationError.REPEATED_POINT -> "REPEATED_POINT";
      case TopologyValidationError.HOLE_OUTSIDE_SHELL -> "HOLE_OUTSIDE_SHELL";
      case TopologyValidationError.NESTED_HOLES -> "NESTED_HOLES";
      case TopologyValidationError.DISCONNECTED_INTERIOR -> "DISCONNECTED_INTERIOR";
      case TopologyValidationError.SELF_INTERSECTION -> "SELF_INTERSECTION";
      case TopologyValidationError.RING_SELF_INTERSECTION -> "RING_SELF_INTERSECTION";
      case TopologyValidationError.NESTED_SHELLS -> "NESTED_SHELLS";
      case TopologyValidationError.DUPLICATE_RINGS -> "DUPLICATE_RINGS";
      case TopologyValidationError.TOO_FEW_POINTS -> "TOO_FEW_POINTS";
      case TopologyValidationError.INVALID_COORDINATE -> "INVALID_COORDINATE";
      case TopologyValidationError.RING_NOT_CLOSED -> "RING_NOT_CLOSED";
      default -> "ERROR";
    };
  }
}
