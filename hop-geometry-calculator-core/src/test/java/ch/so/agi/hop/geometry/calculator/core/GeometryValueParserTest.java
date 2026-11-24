package ch.so.agi.hop.geometry.calculator.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HexFormat;
import org.apache.hop.core.row.value.ValueMetaString;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;

class GeometryValueParserTest {

  private final GeometryValueParser parser = new GeometryValueParser();

  @Test
  void parsesWktEwktAndWkb() throws Exception {
    ValueMetaString valueMeta = new ValueMetaString("geom");

    Geometry wkt = parser.parseGeometry(valueMeta, "POINT (1 2)");
    assertThat(wkt.getGeometryType()).isEqualTo("Point");

    Geometry ewkt = parser.parseGeometry(valueMeta, "SRID=2056;POINT (7 8)");
    assertThat(ewkt.getSRID()).isEqualTo(2056);

    Geometry source = new WKTReader().read("LINESTRING (0 0, 1 1)");
    byte[] wkb = new WKBWriter().write(source);
    assertThat(parser.parseGeometry(valueMeta, wkb)).isEqualTo(source);
    assertThat(parser.parseGeometry(valueMeta, HexFormat.of().formatHex(wkb))).isEqualTo(source);
  }

  @Test
  void keepsEmptyGeometryDistinctFromNull() throws Exception {
    ValueMetaString valueMeta = new ValueMetaString("geom");
    assertThat(parser.parseGeometry(valueMeta, null)).isNull();
    assertThat(parser.parseGeometry(valueMeta, "  ")).isNull();
    assertThat(parser.parseGeometry(valueMeta, "POINT EMPTY").isEmpty()).isTrue();
  }

  @Test
  void raisesParseExceptionForInvalidGeometryText() {
    assertThatThrownBy(() -> parser.parseGeometry(new ValueMetaString("geom"), "not a geometry"))
        .isInstanceOf(ParseException.class);
  }
}
