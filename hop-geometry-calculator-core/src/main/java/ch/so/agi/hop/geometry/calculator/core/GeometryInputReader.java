package ch.so.agi.hop.geometry.calculator.core;

import org.apache.hop.core.row.IValueMeta;
import org.locationtech.jts.geom.Geometry;

public class GeometryInputReader {

  private final GeometryValueParser parser = new GeometryValueParser();

  public GeometryInputResult read(IValueMeta valueMeta, Object value) {
    if (value == null) {
      return GeometryInputResult.nullInput();
    }
    try {
      Geometry geometry = parser.parseGeometry(valueMeta, value);
      if (geometry == null) {
        return GeometryInputResult.nullInput();
      }
      return GeometryInputResult.success(geometry);
    } catch (Exception e) {
      return GeometryInputResult.failure(e);
    }
  }
}
