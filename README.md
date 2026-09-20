# hop-geometry-calculator-plugin

Apache Hop transform plugin that adds a dedicated `Geometry Calculator` for row-wise geometry
calculations on native geometry values and geometry-compatible WKT/WKB inputs.

## Features

- Single-input, row-wise `Geometry Calculator` transform; one configured calculation per transform
- 29 operations for measurements, coordinates, extent, metadata and validation diagnostics
- Input from native geometry values, WKT/EWKT text, WKB bytes and WKB hex
- SQL/MM curve types are parsed and preserved across isolated plugin class loaders
- Configurable error handling: `RETURN_NULL` (default) or `FAIL`

## Requirements

- Apache Hop 2.19.0 and Java 21
- `ch.so.agi:hop-geometry-type:0.2.0-SNAPSHOT`, provided by the separately installed
  [Geometry Type plugin](https://github.com/edigonzales/hop-geometry-type-plugin)

## Install

Download the plugin ZIP from the Maven snapshot repository:

```text
https://jars.interlis.guru/snapshots/ch/so/agi/hop-geometry-calculator-plugin/0.1.0-SNAPSHOT/
```

Install it into Hop:

```bash
unzip -o hop-geometry-calculator-plugin-<version>.zip -d "$HOP_HOME"
```

The ZIP installs to `plugins/transforms/hop-geometry-calculator` and deliberately does not bundle
Geometry Type or JTS. For development, sync the locally built plugin:

```bash
./scripts/dev-sync-hop-plugin.sh "$HOP_HOME"
```

## Documentation

- Rendered handbook: <https://edigonzales.github.io/hop-geometry-calculator-plugin/geometry-calculator/main/>
- Canonical reference: [`docs/transforms/geometry-calculator.adoc`](docs/transforms/geometry-calculator.adoc)
- User examples: [`examples/`](examples/README.md)
- Automated integration tests: `e2e/` (not part of the user documentation)

Build and preview the handbook locally:

```bash
python3 scripts/build-docs-site.py --serve
```

## Build and development

```bash
mvn clean verify
```

`verify` runs the unit tests and the `hop-plugin-doclint-maven-plugin` documentation checks. The
build inherits Java, Hop and plugin versions from
[`hop-plugin-parent`](https://github.com/edigonzales/hop-plugin-parent) and needs access to Maven
Central and `https://jars.interlis.guru`.

Build prerequisites:

- Java 21 (Java 25 is covered by the compatibility matrix)
- Maven
- `ch.so.agi:hop-geometry-type:0.2.0-SNAPSHOT` reachable in the Maven snapshot repository

Produced artifacts after `mvn package`:

- Core JAR: `hop-geometry-calculator-core/target/hop-geometry-calculator-core-<version>.jar`
- Transform JAR: `hop-transform-geometry-calculator/target/hop-transform-geometry-calculator-<version>.jar`
- Plugin ZIP: `assemblies/assemblies-hop-geometry-calculator/target/hop-geometry-calculator-plugin-<version>.zip`

Validate the ZIP and run the installed plugin:

```bash
python3 scripts/verify-package.py
python3 scripts/run-e2e.py --hop-home "$HOP_HOME" --plugin-zip <plugin-zip> --geometry-zip <geometry-zip>
```

`run-e2e.py` installs both ZIPs into a clean Hop installation, runs the deterministic E2E pipeline
and executes every documentation example against the expected results in `e2e/expected/`.

## Modules and artifacts

- `hop-geometry-calculator-core`: geometry input parsing, field detection, function registry,
  scalar conversion
- `hop-transform-geometry-calculator`: transform, dialog, metadata, runtime and icon
- `assemblies/assemblies-hop-geometry-calculator`: install ZIP

## CI and publication

GitHub Actions runs Java 21 and 25 on Linux, macOS and Windows. The canonical Ubuntu/Java 21 job
runs `clean verify` and validates the installation ZIP; a separate job executes the installed E2E
and the documentation examples from the canonical ZIP. Pushes to `main` publish
`ch.so.agi:hop-geometry-calculator-plugin:0.1.0-SNAPSHOT` to
`https://jars.interlis.guru/snapshots/` through the shared
[`hop-plugin-ci`](https://github.com/edigonzales/hop-plugin-ci) workflows. Pull requests publish
nothing. The handbook builds in a separate workflow and deploys to GitHub Pages when `docs/**` or
`examples/**` change.

## License

See [LICENSE](LICENSE).
