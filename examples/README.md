# Example pipelines

Minimal Apache Hop pipelines that demonstrate the Geometry Calculator. Each example uses exactly
one calculation, reads a small CSV file with WKT geometries and writes the result to a new CSV file.

## Prerequisites

- Apache Hop 2.19.0
- the installed Geometry Calculator plugin (see the repository README)
- the installed `hop-geometry-type` plugin; the Calculator ZIP does not bundle it

## Running an example

1. Open the `.hpl` file in the Hop GUI.
2. Set the `INPUT_CSV` and `OUTPUT_CSV` parameters in the run configuration:
   - `INPUT_CSV`: the `data/*.csv` file of the example
   - `OUTPUT_CSV`: a new output path, for example `/tmp/area.csv`
3. Run the pipeline with the local engine.

All examples keep the error mode at the default `RETURN_NULL`: unparseable or unsupported rows
produce `null` results instead of failing the pipeline.

| Example | Pipeline | Calculation |
| --- | --- | --- |
| [Area](area/README.md) | `area/calculate-area.hpl` | `AREA` of polygons |
| [Centroid](centroid/README.md) | `centroid/calculate-centroid.hpl` | `CENTROID_X` of polygons |
| [Validation](validation/README.md) | `validation/geometry-validity.hpl` | `IS_VALID` of valid, invalid and empty geometries |

CI executes every example from the released plugin ZIP against the expected results in
`e2e/expected/`; see `scripts/run-e2e.py`.
