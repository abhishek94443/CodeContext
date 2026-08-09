# Development Guide

## 🛠️ Setting Up Your Development Environment

### Prerequisites

- **JDK 21+** - [Download Temurin](https://adoptium.net/)
- **Git** - For version control and Git analysis features
- **IDE** - IntelliJ IDEA (recommended) or VS Code with Kotlin plugin

### Clone and Build

```bash
# Clone the repository
git clone https://github.com/abhishek94443/CodeContext.git
cd CodeContext

# Build the project
./gradlew build

# Run tests
./gradlew test

# Install distribution locally
./gradlew installDist
```

---

## 🏃 Running Locally

### Run via Gradle

```bash
# Analyze current directory
./gradlew run --args="analyze ."

# Analyze specific directory
./gradlew run --args="analyze /path/to/project"

# Clear cache and analyze
./gradlew run --args="analyze . --clear-cache"

# Disable cache
./gradlew run --args="analyze . --no-cache"
```

### Run via Installed Distribution

```bash
# After running ./gradlew installDist
./build/install/codecontext/bin/codecontext analyze .
```

---

## 🧪 Testing

### Run All Tests

```bash
./gradlew test
```

### Run Specific Test Class

```bash
./gradlew test --tests "com.codecontext.core.parser.ParserTest"
```

### Run Tests with Coverage

```bash
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

### Test Categories

- **Unit Tests** - `src/test/kotlin/com/codecontext/core/`
  - `ParserTest.kt` - Parser functionality
  - `DependencyGraphTest.kt` - Graph building
  - `PropertyTest.kt` - Property-based testing
  
- **Integration Tests** - `src/test/kotlin/com/codecontext/`
  - `E2ETest.kt` - End-to-end workflows
  - `BackendVerificationTest.kt` - Full backend verification

- **Edge Case Tests** - `src/test/kotlin/com/codecontext/core/`
  - `EdgeCaseTest.kt` - Error handling
  - `StressTest.kt` - Performance testing

---

## 🐛 Debugging

### Debug in IntelliJ IDEA

1. Open the project in IntelliJ IDEA
2. Create a new "Gradle" run configuration
3. Set task to: `run --args="analyze ."`
4. Set breakpoints in the code
5. Click "Debug" button

### Debug Tests

1. Right-click on a test class or method
2. Select "Debug 'TestName'"

### Enable Debug Logging

Edit `src/main/resources/logback.xml`:

```xml
<root level="DEBUG">
    <appender-ref ref="STDOUT" />
</root>
```

---

## 📁 Project Structure

```
src/
├── main/kotlin/com/codecontext/
│   ├── Main.kt                    # Entry point
│   ├── cli/                       # CLI commands
│   │   ├── MainCommand.kt         # Root command
│   │   ├── ImprovedAnalyzeCommand.kt  # Analyze command
│   │   ├── ServerCommand.kt       # API server
│   │   └── AIAssistantCommand.kt  # AI features
│   ├── core/                      # Core logic
│   │   ├── scanner/
│   │   │   ├── RepositoryScanner.kt      # File scanning
│   │   │   └── OptimizedGitAnalyzer.kt   # Git analysis
│   │   ├── parser/
│   │   │   ├── JavaRealParser.kt         # Java parsing
│   │   │   ├── KotlinRegexParser.kt      # Kotlin parsing
│   │   │   ├── ParsedFile.kt             # Data model
│   │   │   └── ParserFactory.kt          # Parser selection
│   │   ├── graph/
│   │   │   └── RobustDependencyGraph.kt  # Graph building
│   │   ├── generator/
│   │   │   └── LearningPathGenerator.kt  # Learning paths
│   │   ├── cache/
│   │   │   └── CacheManager.kt           # Caching
│   │   └── config/
│   │       └── CodeContextConfig.kt      # Configuration
│   ├── output/
│   │   └── ReportGenerator.kt     # HTML generation
│   └── server/
│       └── ApiServer.kt           # REST API
└── test/kotlin/com/codecontext/   # Tests
```

---

## 🎨 Code Style

### Kotlin Conventions

We follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

- **Indentation:** 4 spaces
- **Line length:** 120 characters max
- **Naming:**
  - Classes: `PascalCase`
  - Functions: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Private properties: `_camelCase` (optional)

### KDoc Comments

Add KDoc for public APIs:

```kotlin
/**
 * Parses a source file and extracts package and import information.
 *
 * @param file The file to parse
 * @return ParsedFile containing extracted metadata
 * @throws IllegalArgumentException if file type is unsupported
 */
fun parse(file: File): ParsedFile
```

---

## 🔧 Common Development Tasks

### Add a New Language Parser

1. Create parser class implementing `LanguageParser`:
```kotlin
class PythonParser : LanguageParser {
    override fun parse(file: File): ParsedFile {
        // Implementation
    }
}
```

2. Register in `ParserFactory.kt`:
```kotlin
"py" -> pythonParser
```

3. Add tests in `src/test/kotlin/com/codecontext/core/parser/`

### Add a New CLI Command

1. Create command class extending `CliktCommand`:
```kotlin
class MyCommand : CliktCommand(name = "mycommand", help = "Description") {
    override fun run() {
        // Implementation
    }
}
```

2. Register in `Main.kt`:
```kotlin
MainCommand()
    .subcommands(
        ImprovedAnalyzeCommand(),
        MyCommand()  // Add here
    )
```

### Modify Report Output

Edit `src/main/kotlin/com/codecontext/output/ReportGenerator.kt`

The report uses kotlinx.html DSL:
```kotlin
div("my-section") {
    h2 { +"My Section" }
    p { +"Content" }
}
```

---

## 🚀 Release Process

### Version Bumping

1. Update version in `build.gradle.kts`:
```kotlin
version = "0.2.0"
```

2. Update `CHANGELOG.md` with release notes

3. Commit and tag:
```bash
git commit -m "chore: bump version to 0.2.0"
git tag v0.2.0
git push origin main --tags
```

### Build Release Artifacts

```bash
./gradlew clean build installDist
```

---

## 📊 Performance Profiling

### Measure Analysis Time

```bash
time ./gradlew run --args="analyze /path/to/large/project"
```

### Profile with VisualVM

1. Run with JMX enabled:
```bash
./gradlew run --args="analyze ." -Dcom.sun.management.jmxremote
```

2. Connect VisualVM to the process

---

## 🤝 Getting Help

- **Questions:** Open a [Discussion](https://github.com/abhishek94443/CodeContext/discussions)
- **Bugs:** Open an [Issue](https://github.com/abhishek94443/CodeContext/issues)
- **Chat:** Join our [Discord](#) (coming soon)

---

Happy coding! 🎉
