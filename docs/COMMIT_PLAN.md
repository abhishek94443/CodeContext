# CodeContext — Commit History Plan

Your code is currently local-only, so you have a clean slate to build a commit history that
tells the real architectural story of this project — instead of one giant "initial commit."
This is standard practice, not deceptive: every commit below only contains code you actually
wrote, grouped in the logical order a system like this gets built in.

Run these from the `CodeContext/` project root, in a Git Bash / MINGW64 terminal (matches
your earlier setup).

---

## Step 0 — One-time setup

```bash
# If not already a git repo
git init

# Create .gitignore BEFORE the first commit — do not skip this
cat > .gitignore << 'EOF'
build/
.gradle/
out/
*.class
.codecontext/
output/
temp_repos/
.idea/
*.iml
.DS_Store
.antigravity/
EOF

git add .gitignore
git commit -m "chore: initial project setup and gitignore"
```

---

## Step 1 — Project scaffolding

```bash
git add build.gradle.kts settings.gradle.kts gradlew gradlew.bat gradle/
git add src/main/kotlin/com/codecontext/Main.kt
git add src/main/kotlin/com/codecontext/cli/MainCommand.kt
git commit -m "chore: bootstrap Gradle project with Clikt CLI entrypoint"
```

## Step 2 — Domain models and error handling

```bash
git add src/main/kotlin/com/codecontext/core/exceptions/CodeContextException.kt
git add src/main/kotlin/com/codecontext/core/parser/ParsedFile.kt
git commit -m "feat: define core domain models and exception hierarchy"
```

## Step 3 — Configuration system

```bash
git add src/main/kotlin/com/codecontext/core/config/CodeContextConfig.kt
git commit -m "feat: add JSON-based config system with sensible defaults"
```

## Step 4 — Repository scanning

```bash
git add src/main/kotlin/com/codecontext/core/scanner/RepositoryScanner.kt
git commit -m "feat: implement repository file scanner with exclusion filters"
```

## Step 5 — Language parsers

```bash
git add src/main/kotlin/com/codecontext/core/parser/ParserFactory.kt
git add src/main/kotlin/com/codecontext/core/parser/JavaRealParser.kt
git commit -m "feat: add AST-based Java parsing via JavaParser"

git add src/main/kotlin/com/codecontext/core/parser/KotlinRegexParser.kt
git commit -m "feat: add Kotlin file parsing (regex-based package/import extraction)"
```

## Step 6 — Caching layer

```bash
git add src/main/kotlin/com/codecontext/core/cache/CacheManager.kt
git commit -m "feat: add thread-safe disk cache with atomic writes and per-key locking

Cache key derived from file path + mtime + size for correct invalidation.
Uses ReentrantReadWriteLock per cache key to avoid contention across
unrelated files during parallel parsing."
```

## Step 7 — Parallel parsing

```bash
git add src/main/kotlin/com/codecontext/cli/CodeParallelParser.kt
git commit -m "feat: parallelize file parsing with coroutines and memory-aware chunking"
```

## Step 8 — Dependency graph and PageRank

```bash
git add src/main/kotlin/com/codecontext/core/graph/RobustDependencyGraph.kt
git commit -m "feat: build dependency graph and rank architectural hotspots via PageRank

Uses JGraphT for graph construction and cycle detection. PageRank
identifies the most structurally critical files in a codebase based
on how many other files depend on them."
```

## Step 9 — Learning path generation

```bash
git add src/main/kotlin/com/codecontext/core/generator/LearningPathGenerator.kt
git commit -m "feat: generate onboarding reading order via topological sort"
```

## Step 10 — Git history analysis (naive version first — shows real iteration)

```bash
git add src/main/kotlin/com/codecontext/core/scanner/GitAnalyzer.kt
git commit -m "feat: add Git history enrichment (per-file log lookup)"
```

## Step 11 — Git analysis optimization (this is a great commit to have in your history)

```bash
git add src/main/kotlin/com/codecontext/core/scanner/OptimizedGitAnalyzer.kt
git commit -m "perf: replace per-file git log with single-pass tree-diff traversal

Previous approach ran a separate 'git log' per file, which scales
poorly on repos with long history. New approach walks all commits
once, diffing each against its parent via TreeWalk, and aggregates
per-file stats in a single pass."
```
*(Optional but recommended: after this commit, actually delete the old `GitAnalyzer.kt` in a
follow-up commit — see Step 18 cleanup below — rather than leaving both in the final state.)*

## Step 12 — HTML report generation

```bash
git add src/main/kotlin/com/codecontext/output/ReportGenerator.kt
git commit -m "feat: generate interactive HTML report with force-directed dependency graph"
```

## Step 13 — Main CLI command tying it together

```bash
git add src/main/kotlin/com/codecontext/cli/ImprovedAnalyzeCommand.kt
git add src/main/kotlin/com/codecontext/cli/ErrorHandler.kt
git commit -m "feat: wire up 'analyze' command — full scan-to-report pipeline

Includes per-stage error handling so a failure in git analysis or AI
insight generation doesn't crash the whole run."
```

## Step 14 — Evolution / temporal analysis

```bash
git add src/main/kotlin/com/codecontext/core/temporal/TemporalAnalyzer.kt
git add src/main/kotlin/com/codecontext/cli/EvolutionCommand.kt
git commit -m "feat: add 'evolution' command to track codebase growth over time"
```

## Step 15 — AI integration

```bash
git add src/main/kotlin/com/codecontext/core/ai/AICodeAnalyzer.kt
git commit -m "feat: integrate AI layer with multi-provider support (Gemini + Claude)

Supports file-level insight generation, conversational Q&A, and PR
review. Prompts are enriched with graph context (PageRank, dependency
counts, git churn) rather than raw code alone."

git add src/main/kotlin/com/codecontext/cli/AIAssistantCommand.kt
git commit -m "feat: add 'ask' command for conversational codebase Q&A"
```

## Step 16 — REST API server

```bash
git add src/main/kotlin/com/codecontext/server/RateLimiter.kt
git commit -m "feat: add sliding-window rate limiter for API clients"

git add src/main/kotlin/com/codecontext/server/CodeContextServer.kt
git add src/main/kotlin/com/codecontext/cli/ServerCommand.kt
git commit -m "feat: expose analysis engine as a Ktor REST API

Includes /analyze, /ask, and /analyze-org endpoints, path sanitization
to prevent traversal attacks, and remote-repo cloning support via JGit."
```

## Step 17 — Enterprise features

```bash
git add src/main/kotlin/com/codecontext/enterprise/LicenseManager.kt
git add src/main/kotlin/com/codecontext/enterprise/OrganizationAnalyzer.kt
git commit -m "feat: add multi-repo organization analysis and tiered licensing"
```

## Step 18 — Tests

```bash
git add src/test/kotlin/com/codecontext/ErrorHandlerTest.kt
git add src/test/kotlin/com/codecontext/E2ETest.kt
# add any other test files you have
git commit -m "test: add unit and end-to-end test coverage"
```

## Step 19 — Cleanup pass (do this after Epic 1 & 2 from USER_STORIES.md are done)

```bash
git rm src/main/kotlin/com/codecontext/core/scanner/GitAnalyzer.kt
git rm src/main/kotlin/com/codecontext/core/analyzer/Analyzer.kt
git rm src/main/kotlin/com/codecontext/core/generator/ContextGenerator.kt
git rm src/main/kotlin/com/codecontext/core/Core.kt
git rm src/main/kotlin/com/codecontext/core/scanner/Scanner.kt
git commit -m "chore: remove superseded and unused placeholder code

GitAnalyzer.kt is fully superseded by OptimizedGitAnalyzer. The other
files were early scaffolding that was never implemented."
```

## Step 20 — Documentation

```bash
git add README.md
git commit -m "docs: add README with quickstart, architecture overview, and demo"

git add docs/
git commit -m "docs: add project status, action plan, and user stories"
```

## Step 21 — CI

```bash
git add .github/workflows/build.yml
git commit -m "ci: run gradle build and tests on every push"
```

---

## After all commits — push

```bash
git branch -M main
git remote add origin https://github.com/abhishek94443/CodeContext.git
git push -u origin main
```

---

## Notes on doing this well

- **Don't fake commit dates.** Backdating commits with `--date` to simulate months of work is
  easy to spot (author date vs. commit date mismatches, unrealistic spacing) and reads as
  worse than an honest, clean history made in one session. A recruiter cares that your commits
  are atomic and well-described, not that they're spread over months.
- **Run the build after each commit** if you want to be thorough — `./gradlew build` — to
  confirm you never commit a broken intermediate state. This matters less for a portfolio repo
  than production, but it's good practice.
- **Fix the 2 failing tests (ACTION_PLAN.md P0) before Step 18**, so your test commit doesn't
  introduce red tests into the history.
- **Do the AI bug fixes (JSON extraction, Claude parsing, timeout) as separate commits after
  Step 15**, e.g.:
  ```bash
  git commit -m "fix: use robust JSON extraction for AI responses (handles markdown fences)"
  git commit -m "fix: correct Claude response parsing to match Gemini's JSON handling"
  git commit -m "fix: add HTTP client timeout to prevent indefinite hangs on AI calls"
  ```
  These are genuinely good commits to have visible — they show you find and fix real bugs in
  your own AI integration, which is a strong signal for the target companies you're applying to.
