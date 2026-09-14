# Polygon area

Reads WKT polygons and adds the `area` field with the `AREA` operation.

- Pipeline: `calculate-area.hpl`
- Input: `data/parcels.csv` (`;` separated, one `geometry` column with WKT polygons)
- Parameters: `INPUT_CSV`, `OUTPUT_CSV`
- Calculation: `AREA`, output field `area`, output type `NUMBER`, error mode `RETURN_NULL`

## Expected result

| geometry | area |
| --- | --- |
| `POLYGON ((0 0, 0 2, 2 2, 2 0, 0 0))` | 4.0 |
| `POLYGON ((10 10, 10 11, 11 11, 11 10, 10 10))` | 1.0 |
| `POLYGON ((0 0, 3 0, 3 1, 0 1, 0 0))` | 3.0 |

`AREA` is a planar calculation in the units of the geometry. The example data has no CRS.
See the [Geometry Calculator reference](../../docs/transforms/geometry-calculator.adoc) for all
options and the output type matrix.
