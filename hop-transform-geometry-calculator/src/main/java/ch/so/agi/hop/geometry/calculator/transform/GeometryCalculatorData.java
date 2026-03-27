package ch.so.agi.hop.geometry.calculator.transform;

import ch.so.agi.hop.geometry.calculator.core.GeometryCalculatorFunction;
import ch.so.agi.hop.geometry.calculator.core.GeometryInputReader;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.pipeline.transform.BaseTransformData;
import org.apache.hop.pipeline.transform.ITransformData;

public class GeometryCalculatorData extends BaseTransformData implements ITransformData {
  IRowMeta outputRowMeta;
  int inputGeometryFieldIndex = -1;
  int outputFieldIndex = -1;
  GeometryCalculatorFunction function;
  final GeometryInputReader inputReader = new GeometryInputReader();
  final Map<String, Long> errorCounts = new LinkedHashMap<>();
}
