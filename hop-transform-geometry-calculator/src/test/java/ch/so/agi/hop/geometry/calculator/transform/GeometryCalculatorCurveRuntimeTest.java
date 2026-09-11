package ch.so.agi.hop.geometry.calculator.transform;

import static org.assertj.core.api.Assertions.assertThat;

import ch.so.agi.hop.geometry.calculator.core.GeometryCalculatorOutputType;
import com.atolcd.hop.gis.geometry.curve.CircularString;
import com.atolcd.hop.gis.geometry.curve.CurveGeometrySupport;
import com.atolcd.hop.gis.geometry.curve.CurvePolygon;
import java.util.ArrayList;
import java.util.List;
import org.apache.hop.core.BlockingRowSet;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.IRowSet;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

class GeometryCalculatorCurveRuntimeTest {

  @BeforeEach
  void initHop() throws Exception {
    HopEnvironment.init();
  }

  @AfterEach
  void resetHop() {
    HopEnvironment.reset();
  }

  @Test
  void geometryTypeReportsTrueCurveTypeFromWkbInput() throws Exception {
    CurvePolygon curvePolygon = curvePolygon(new GeometryFactory());

    Object result =
        runCalculator(
            "GEOMETRY_TYPE",
            GeometryCalculatorOutputType.STRING,
            CurveGeometrySupport.writeWkb(curvePolygon));

    assertThat(result).isEqualTo("CurvePolygon");
  }

  @Test
  void lengthUsesSegmentedCircularStringRepresentationFromWkbInput() throws Exception {
    CircularString circularString = openCircularString(new GeometryFactory());

    Object result =
        runCalculator(
            "LENGTH",
            GeometryCalculatorOutputType.NUMBER,
            CurveGeometrySupport.writeWkb(circularString));

    assertThat(result).isEqualTo(circularString.getLength());
  }

  private Object runCalculator(
      String operationId, GeometryCalculatorOutputType outputType, Object geometryValue)
      throws Exception {
    GeometryCalculatorMeta meta = new GeometryCalculatorMeta();
    meta.setDefault();
    meta.setInputGeometryFieldName("geometry_wkb");
    meta.setOperationId(operationId);
    meta.setOutputFieldName("result");
    meta.setOutputType(outputType);

    TestGeometryCalculator transform =
        new TestGeometryCalculator(
            new TransformMeta("calculator", meta),
            meta,
            new GeometryCalculatorData(),
            0,
            new PipelineMeta());

    RowMeta inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta(new ValueMetaString("geometry_wkb"));
    transform.setInput(inputRowMeta, List.<Object[]>of(new Object[] {geometryValue}));

    BlockingRowSet output = new BlockingRowSet(10);
    output.setThreadNameFromToCopy("calculator", 0, "main", 0);
    transform.addRowSetToOutputRowSets(output);

    while (transform.processRow()) {
      // keep consuming until the transform signals completion
    }

    Object[] row = output.getRow();
    assertThat(row).isNotNull();
    assertThat(output.getRow()).isNull();
    return row[1];
  }

  private static CircularString openCircularString(GeometryFactory factory) {
    return new CircularString(
        new Coordinate[] {
          new Coordinate(0, 0), new Coordinate(1, 1), new Coordinate(2, 0)
        },
        factory);
  }

  private static CurvePolygon curvePolygon(GeometryFactory factory) {
    CircularString ring =
        new CircularString(
            new Coordinate[] {
              new Coordinate(0, 0),
              new Coordinate(4, 0),
              new Coordinate(4, 4),
              new Coordinate(0, 4),
              new Coordinate(0, 0)
            },
            factory);
    return new CurvePolygon(List.of(ring), factory);
  }

  private static class TestGeometryCalculator extends GeometryCalculator {
    private IRowMeta inputRowMeta;
    private List<Object[]> inputRows = List.of();
    private int inputIndex;

    TestGeometryCalculator(
        TransformMeta transformMeta,
        GeometryCalculatorMeta meta,
        GeometryCalculatorData data,
        int copyNr,
        PipelineMeta pipelineMeta) {
      super(transformMeta, meta, data, copyNr, pipelineMeta, null);
    }

    @Override
    public void dispatch() {
      // Tests attach output row sets explicitly.
    }

    void setInput(IRowMeta rowMeta, List<Object[]> rows) {
      this.inputRowMeta = rowMeta;
      this.inputRows = new ArrayList<>(rows);
      this.inputIndex = 0;
    }

    @Override
    public Object[] getRow() {
      if (inputIndex >= inputRows.size()) {
        return null;
      }
      return inputRows.get(inputIndex++);
    }

    @Override
    public IRowMeta getInputRowMeta() {
      return inputRowMeta;
    }

    @Override
    public void putRow(IRowMeta rowMeta, Object[] row) {
      for (IRowSet rowSet : getOutputRowSets()) {
        rowSet.putRow(rowMeta, row);
      }
    }
  }
}
