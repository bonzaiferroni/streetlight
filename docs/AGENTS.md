# Agents

Read this first, then `docs/package-documentation.md`.

## Documentation

`docs/packages/` holds one document per package, named for the fully qualified package. A package document says where things go and why. The code says what they are.

`docs/package-documentation.md` states what a package document contains, how it is written, and the style it follows. This document states when to read one and when to amend one.

## Reading Documents

Read the document for a package before adding a file to it, before changing a convention its files follow, and before reporting on code inside it.

A convention that looks arbitrary in a single file is usually explained there. Reading the document first is what separates a finding from a misreading.

## Amending Documents

Documentation upkeep is part of the work, not a pass that follows it. A package document is written or amended on any of the following. Each is sufficient on its own.

| Trigger | Action |
|---|---|
| A convention is decided | Record it before the code is written |
| Code changes a convention other files follow | Amend in the same pass as the code |
| A convention is described in conversation | Record it in the same turn |

The third is the one most often missed. A convention described is a convention decided. An explanation of how a package works is the information the document exists to hold, and it is recorded on first hearing — not once the code catches up, and not once the point is raised a second time.

An explanation often covers several packages at once. Each package named gets its own amendment, in its own document.

Accuracy outranks completeness. A document describes the code as it stands, and an inaccurate one is worse than a missing one, because it will be believed.

These are live documents. Drift noticed is drift to correct, in the turn it is noticed, whether or not the current task put it there. Reporting that a document has fallen behind and leaving it that way is not a finding, it is a second reader misled. Correct the drift and say what changed.

## Reporting Issues

Report defects, inconsistencies, and unfinished edges in conversation. They do not belong in a package document.

An issue is written down only once it is decided that it should be, and then it goes under the `## Known Issues` heading of the document for the package that holds it. The entry is removed in the same pass as the fix.

## Testing

`docs/testing.md` states how tests are written here: the kinds of test and when each is worth its cost, the plain voice test names use, and the discipline for assertions, utilities, and fakes. Read it before writing or changing a test.

The fixtures belonging to a test package are documented in that package's own document, not in `docs/testing.md`.

## Dependencies

Every dependency is declared in `gradle/libs.versions.toml` and referenced from the build file through `libs`. A coordinate written as a string in a `build.gradle.kts` is not an accepted declaration, and is moved to the catalog when found.

A catalog entry puts its version in `[versions]` and reaches it with `version.ref`. Artifacts released together share one version entry, so that a bump moves the set rather than half of it.

The catalog holds older entries that do not follow this. Do not undertake a cleanup of the whole file. Bring the entries you add or touch up to the rule, and leave the rest where they lie.

Confirm a version before raising it. An unverified bump is reported, not made.

## Scope

Do the task given. When work uncovers a second thing worth doing, report it and wait, rather than widening the change.

Do not restructure existing code without agreeing to it first. Extracting a function, moving code between files, renaming, changing a signature, and reorganizing a package are all changes that should be discussed.

This holds when the task cannot be finished without the change. Say what the task needs and why, and stop there. 

When the code needed to answer a question is not in hand, ask for it. Do not infer its shape from the code around it.
