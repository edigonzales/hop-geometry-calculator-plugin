package ch.so.agi.hop.geometry.calculator.core;

import com.atolcd.hop.gis.geometry.curve.CircularString;
import com.atolcd.hop.gis.geometry.curve.CompoundCurve;
import com.atolcd.hop.gis.geometry.curve.CurvePolygon;
import com.atolcd.hop.gis.geometry.curve.MultiCurve;
import com.atolcd.hop.gis.geometry.curve.MultiSurface;
import org.locationtech.jts.geom.Geometry;

final class GeometryTypeNames {

  private GeometryTypeNames() {}

  static String name(Geometry geometry) {
    if (geometry instanceof CircularString) {
      return "CircularString";
    }
    if (geometry instanceof CompoundCurve) {
      return "CompoundCurve";
    }
    if (geometry instanceof CurvePolygon) {
      return "CurvePolygon";
    }
    if (geometry instanceof MultiCurve) {
      return "MultiCurve";
    }
    if (geometry instanceof MultiSurface) {
      return "MultiSurface";
    }
    return geometry.getGeometryType();
  }
}
