package ch.so.agi.hop.geometry.calculator.transform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.so.agi.hop.geometry.calculator.core.GeometryCalculatorErrorMode;
import ch.so.agi.hop.geometry.calculator.core.GeometryCalculatorOutputType;
import com.atolcd.hop.core.row.value.ValueMetaGeometry;
import java.util.ArrayList;
import java.util.List;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.BlockingRowSet;
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
import org.locationtech.jts.io.WKTReader;

class GeometryCalculatorRuntimeTest {

  private final WKTReader wktReader = new WKTReader();

  @BeforeEach
  void initHop() throws Exception {
    HopEnvironment.init();
  }

  @AfterEach
  void resetHop() {
    HopEnvironment.reset();
  }

  @Test
  void returnNullModeProducesValuesAndNullsForCompatibleAndIncompatibleRows() throws Exception {
    GeometryCalculatorMeta meta = new GeometryCalculatorMeta();
    meta.setDefault();
    meta.setInputGeometryFieldName("geometry");
    meta.setOperationId("AREA");
    meta.setOutputFieldName("area");
    meta.setOutputType(GeometryCalculatorOutputType.NUMBER);
    meta.setErrorMode(GeometryCalculatorErrorMode.RETURN_NULL);

    TestGeometryCalculator transform =
        new TestGeometryCalculator(
            new TransformMeta("calculator", meta),
            meta,
            new GeometryCalculatorData(),
            0,
            new PipelineMeta());

    RowMeta inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta(new ValueMetaGeometry("geometry"));
    transform.setInput(
        inputRowMeta,
        List.of(
            new Object[] {wktReader.read("POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0))")},
            new Object[] {wktReader.read("POINT (1 1)")},
            new Object[] {null}));

    BlockingRowSet output = addOutputRowSet(transform);
    runTransform(transform);

    List<Object[]> rows = drainRows(output);
    assertThat(rows).hasSize(3);
    assertThat(rows.get(0)[1]).isEqualTo(4.0d);
    assertThat(rows.get(1)[1]).isNull();
    assertThat(rows.get(2)[1]).isNull();
  }

  @Test
  void supportsCompatibleWktInput() throws Exception {
    GeometryCalculatorMeta meta = new GeometryCalculatorMeta();
    meta.setDefault();
    meta.setInputGeometryFieldName("geometry_wkt");
    meta.setOperationId("GEOMETRY_TYPE");
    meta.setOutputFieldName("geometry_type");
    meta.setOutputType(GeometryCalculatorOutputType.STRING);

    TestGeometryCalculator transform =
        new TestGeometryCalculator(
            new TransformMeta("calculator", meta),
            meta,
            new GeometryCalculatorData(),
            0,
            new PipelineMeta());

    RowMeta inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta(new ValueMetaString("geometry_wkt"));
    transform.setInput(
        inputRowMeta,
        List.<Object[]>of(new Object[] {"LINESTRING (0 0, 1 1)"}, new Object[] {"not a geometry"}));

    BlockingRowSet output = addOutputRowSet(transform);
    runTransform(transform);

    List<Object[]> rows = drainRows(output);
    assertThat(rows).hasSize(2);
    assertThat(rows.get(0)[1]).isEqualTo("LineString");
    assertThat(rows.get(1)[1]).isNull();
  }

  @Test
  void supportsDiagnosticFunctionsForCompatibleWktInput() throws Exception {
    GeometryCalculatorMeta meta = new GeometryCalculatorMeta();
    meta.setDefault();
    meta.setInputGeometryFieldName("geometry_wkt");
    meta.setOperationId("IS_VALID_REASON");
    meta.setOutputFieldName("is_valid_reason");
    meta.setOutputType(GeometryCalculatorOutputType.STRING);

    TestGeometryCalculator transform =
        new TestGeometryCalculator(
            new TransformMeta("calculator", meta),
            meta,
            new GeometryCalculatorData(),
            0,
            new PipelineMeta());

    RowMeta inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta(new ValueMetaString("geometry_wkt"));
    transform.setInput(
        inputRowMeta,
        List.<Object[]>of(
            new Object[] {"POLYGON ((0 0, 2 0, 2 2, 0 2, 0 0))"},
            new Object[] {"POLYGON ((0 0, 2 2, 2 0, 0 2, 0 0))"}));

    BlockingRowSet output = addOutputRowSet(transform);
    runTransform(transform);

    List<Object[]> rows = drainRows(output);
    assertThat(rows).hasSize(2);
    assertThat(rows.get(0)[1]).isNull();
    assertThat((String) rows.get(1)[1]).contains("Self-intersection");
  }

  @Test
  void widthAndInteriorPointReturnNullForEmptyGeometries() throws Exception {
    GeometryCalculatorMeta widthMeta = new GeometryCalculatorMeta();
    widthMeta.setDefault();
    widthMeta.setInputGeometryFieldName("geometry");
    widthMeta.setOperationId("WIDTH");
    widthMeta.setOutputFieldName("width");
    widthMeta.setOutputType(GeometryCalculatorOutputType.NUMBER);
    widthMeta.setErrorMode(GeometryCalculatorErrorMode.FAIL);

    TestGeometryCalculator widthTransform =
        new TestGeometryCalculator(
            new TransformMeta("calculator", widthMeta),
            widthMeta,
            new GeometryCalculatorData(),
            0,
            new PipelineMeta());

    RowMeta inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta(new ValueMetaGeometry("geometry"));
    widthTransform.setInput(
        inputRowMeta,
        List.<Object[]>of(
            new Object[] {wktReader.read("POLYGON EMPTY")},
            new Object[] {wktReader.read("POLYGON ((0 0, 4 0, 4 3, 0 3, 0 0))")}));

    BlockingRowSet widthOutput = addOutputRowSet(widthTransform);
    runTransform(widthTransform);

    List<Object[]> widthRows = drainRows(widthOutput);
    assertThat(widthRows).hasSize(2);
    assertThat(widthRows.get(0)[1]).isNull();
    assertThat(widthRows.get(1)[1]).isEqualTo(4.0d);

    GeometryCalculatorMeta interiorMeta = new GeometryCalculatorMeta();
    interiorMeta.setDefault();
    interiorMeta.setInputGeometryFieldName("geometry");
    interiorMeta.setOperationId("INTERIOR_POINT_X");
    interiorMeta.setOutputFieldName("interior_x");
    interiorMeta.setOutputType(GeometryCalculatorOutputType.NUMBER);
    interiorMeta.setErrorMode(GeometryCalculatorErrorMode.FAIL);

    TestGeometryCalculator interiorTransform =
        new TestGeometryCalculator(
            new TransformMeta("calculator", interiorMeta),
            interiorMeta,
            new GeometryCalculatorData(),
            0,
            new PipelineMeta());

    interiorTransform.setInput(
        inputRowMeta,
        List.<Object[]>of(
            new Object[] {wktReader.read("POLYGON EMPTY")},
            new Object[] {wktReader.read("POINT (5 6)")}));

    BlockingRowSet interiorOutput = addOutputRowSet(interiorTransform);
    runTransform(interiorTransform);

    List<Object[]> interiorRows = drainRows(interiorOutput);
    assertThat(interiorRows).hasSize(2);
    assertThat(interiorRows.get(0)[1]).isNull();
    assertThat(interiorRows.get(1)[1]).isEqualTo(5.0d);
  }

  @Test
  void failModeRaisesOnIncompatibleGeometry() throws Exception {
    GeometryCalculatorMeta meta = new GeometryCalculatorMeta();
    meta.setDefault();
    meta.setInputGeometryFieldName("geometry");
    meta.setOperationId("X");
    meta.setOutputFieldName("x");
    meta.setOutputType(GeometryCalculatorOutputType.NUMBER);
    meta.setErrorMode(GeometryCalculatorErrorMode.FAIL);

    TestGeometryCalculator transform =
        new TestGeometryCalculator(
            new TransformMeta("calculator", meta),
            meta,
            new GeometryCalculatorData(),
            0,
            new PipelineMeta());

    RowMeta inputRowMeta = new RowMeta();
    inputRowMeta.addValueMeta(new ValueMetaGeometry("geometry"));
    transform.setInput(
        inputRowMeta, List.<Object[]>of(new Object[] {wktReader.read("LINESTRING (0 0, 1 1)")}));

    addOutputRowSet(transform);

    assertThatThrownBy(() -> runTransform(transform))
        .isInstanceOf(HopException.class)
        .hasMessageContaining("Geometry is not compatible with the configured operation");
  }

  private BlockingRowSet addOutputRowSet(TestGeometryCalculator transform) {
    BlockingRowSet output = new BlockingRowSet(10);
    output.setThreadNameFromToCopy("calculator", 0, "main", 0);
    transform.addRowSetToOutputRowSets(output);
    return output;
  }

  private void runTransform(GeometryCalculator transform) throws HopException {
    while (transform.processRow()) {
      // keep consuming until the transform signals completion
    }
  }

  private List<Object[]> drainRows(IRowSet rowSet) {
    List<Object[]> rows = new ArrayList<>();
    Object[] row;
    while ((row = rowSet.getRow()) != null) {
      rows.add(row);
    }
    return rows;
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
