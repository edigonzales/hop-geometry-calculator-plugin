# Repository instructions

## CI and tests

Before changing pipelines or test setup, read the
[shared CI contract](https://github.com/edigonzales/hop-plugin-ci/blob/main/docs/ci-contract.md).
Also read the
[plugin repository contract](https://github.com/edigonzales/hop-plugin-ci/blob/main/docs/plugin-repository-contract.md).
The documentation follows `main`; use the interfaces at this repo's actual workflow/helper
revisions and preserve existing pins and `ci-ref` values.

Run the commands below from this repository root in Bash, using Python 3, Maven and JDK 21
(`JAVA_HOME` and `PATH` pointing to that JDK). Compatibility jobs also use JDK 25. For headless
Linux SWT tests, run Maven under `xvfb-run -a`. Set `HOP_CI_DIR` to an absolute checkout of
`hop-plugin-ci` at the helper revision used by this repo's workflow, then prepare the same Maven
repositories as CI:

```bash
CI_TEST_TMP="$(mktemp -d)"
export MAVEN_SETTINGS="$CI_TEST_TMP/maven-settings.xml"
python3 "$HOP_CI_DIR/scripts/write_maven_settings.py" --output "$MAVEN_SETTINGS"
```

### Build and package verification

See [.github/workflows/verify.yml](.github/workflows/verify.yml).

```bash
mvn -s "$MAVEN_SETTINGS" -U -B -ntp clean verify
python3 scripts/verify-package.py
```

The canonical build is Ubuntu/Java 21. It runs the unit tests and the
`hop-plugin-doclint-maven-plugin` documentation checks for `docs/` and `examples/`. Compatibility
uses the same Maven settings with `clean test` instead of `clean verify`.

The build resolves `ch.so.agi:hop-geometry-type:0.2.0-SNAPSHOT` from the snapshot repository; the
CI [`ci.yml`](.github/workflows/ci.yml) `geometry-type` job builds that dependency from `main`
before verification.

### Installed Hop E2E and documentation examples

Prepare a clean, disposable Apache Hop 2.19.0 installation; it must not already contain Geometry or
Calculator plugin directories. Set `HOP_HOME` to that installation, `PLUGIN_ZIP` to the built
Calculator ZIP in `assemblies/assemblies-hop-geometry-calculator/target`, and `GEOMETRY_ZIP` to the
Geometry Type 0.2.0-SNAPSHOT ZIP. The test requires Java, `javac` and Python.

```bash
python3 scripts/run-e2e.py --hop-home "$HOP_HOME" --plugin-zip "$PLUGIN_ZIP" --geometry-zip "$GEOMETRY_ZIP"
```

The script installs both ZIPs, runs `e2e/geometry-calculator.hpl` and executes every example under
`examples/` with the parameters `INPUT_CSV` and `OUTPUT_CSV`, comparing the output with
`e2e/expected/*.csv`. In CI the canonical plugin artifact and the Geometry Type artifact are
downloaded instead of rebuilt.

### Documentation site

The handbook is a single-page Biblios site built from `docs/` and deployed to GitHub Pages by
[.github/workflows/biblios-docs.yml](.github/workflows/biblios-docs.yml). Build and preview:

```bash
python3 scripts/build-docs-site.py --serve
```

The script resolves the latest `guru.interlis:thoth-biblios:0.0.1-SNAPSHOT:all` snapshot from
`https://jars.interlis.guru/snapshots/`, builds the working tree or an exact `--revision`, copies
`examples/` into the site, and checks links, anchors, example downloads and search entries with
`scripts/check-docs-site.py`. Pull requests only build; pushes to `main` that touch `docs/**` or
`examples/**` deploy to GitHub Pages (Pages must be set to "GitHub Actions" in the repository
settings).

### Publication

Pushes to `main` publish the POM-verified plugin ZIP through the shared `plugin-publish.yml`
workflow to `https://jars.interlis.guru/snapshots/`. Publication depends on the verify workflow and
the installed E2E job; pull requests publish nothing.
