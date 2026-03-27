package ch.so.agi.hop.geometry.calculator.core;

import org.locationtech.jts.geom.Geometry;

public record GeometryInputResult(Geometry geometry, boolean nullValue, Exception failure) {

  public static GeometryInputResult nullInput() {
    return new GeometryInputResult(null, true, null);
  }

  public static GeometryInputResult success(Geometry geometry) {
    return new GeometryInputResult(geometry, false, null);
  }

  public static GeometryInputResult failure(Exception exception) {
    return new GeometryInputResult(null, false, exception);
  }

  public boolean hasFailure() {
    return failure != null;
  }
}
