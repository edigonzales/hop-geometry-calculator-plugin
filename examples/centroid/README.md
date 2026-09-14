# Polygon centroid

Reads WKT polygons and adds the `centroid_x` field with the `CENTROID_X` operation.

- Pipeline: `calculate-centroid.hpl`
- Input: `data/buildings.csv` (`;` separated, one `geometry` column with WKT polygons)
- Parameters: `INPUT_CSV`, `OUTPUT_CSV`
- Calculation: `CENTROID_X`, output field `centroid_x`, output type `NUMBER`, error mode `RETURN_NULL`

## Expected result

| geometry | centroid_x |
| --- | --- |
| `POLYGON ((0 0, 0 2, 2 2, 2 0, 0 0))` | 1.0 |
| `POLYGON ((10 10, 10 12, 12 12, 12 10, 10 10))` | 11.0 |

Chain a second Geometry Calculator with `CENTROID_Y` to calculate the full centroid.
See the [Geometry Calculator reference](../../docs/transforms/geometry-calculator.adoc) for all
options and the output type matrix.
