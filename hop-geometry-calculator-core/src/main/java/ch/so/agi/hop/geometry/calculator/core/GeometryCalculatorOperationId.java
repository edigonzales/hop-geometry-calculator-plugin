package ch.so.agi.hop.geometry.calculator.core;

public enum GeometryCalculatorOperationId {
  AREA,
  LENGTH,
  PERIMETER,
  WIDTH,
  HEIGHT,
  X,
  Y,
  Z,
  CENTROID_X,
  CENTROID_Y,
  INTERIOR_POINT_X,
  INTERIOR_POINT_Y,
  XMIN,
  YMIN,
  XMAX,
  YMAX,
  GEOMETRY_TYPE,
  NUM_POINTS,
  NUM_GEOMETRIES,
  IS_EMPTY,
  IS_VALID,
  IS_VALID_REASON,
  IS_VALID_ERROR_TYPE,
  IS_VALID_LOCATION_X,
  IS_VALID_LOCATION_Y,
  IS_SIMPLE,
  IS_SIMPLE_LOCATION_X,
  IS_SIMPLE_LOCATION_Y,
  SRID;

  public static GeometryCalculatorOperationId fromCode(String code) {
    if (code == null || code.isBlank()) {
      return null;
    }
    return GeometryCalculatorOperationId.valueOf(code);
  }
}
