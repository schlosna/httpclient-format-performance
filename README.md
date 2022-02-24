## Developing:

### Intellij Idea
Open via Gradle integration with Intellij

### Eclipse
`./gradlew idea` + import

## Running

You can run the benchmarks from the command line with:

`./gradlew jmh`

Or build a jmh jar (based on instructions from the [jmh-gradle-plugin](https://github.com/melix/jmh-gradle-plugin)) which produces `build/libs/format-performance-jmh.jar`:

`./gradlew jmhJar`

Alternatively via the main method in your IDE.

