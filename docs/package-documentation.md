# Package Documentation

`docs/packages/` holds one document per package. The filename is the fully qualified package name, for example `streetlight.web.model.md`.

Each package should have a document that describes the specifications and concepts used within the package.

## Documentation Content

A package document provides an outline for working with code in a particular package. 

Include:

- Naming patterns for files and types
- Where a thing belongs when there are two options
- The shape of a typical member
- A rule
- Behavior the compiler does not enforce

Exclude:

- Type signatures and property lists
- History, deprecations, or planned changes
- Exceptions to the convention
- Justification or explanation that does not add clarity to the specification.
- Defects, inconsistencies, and unfinished work, except under Known Issues

## Style

| Rule                   | Detail                                                                                                      |
|------------------------|-------------------------------------------------------------------------------------------------------------|
| Direct language        | No metaphors, analogies, or figures of speech                                                               |
| Plain headings         | Name the topic: "Reading Documents", not "Reading them"                                                     |
| Brevity                | Say what is required and stop                                                                               |
| Single-line paragraphs | No hard wrapping within a paragraph                                                                         |
| Tables                 | Use them where each row relates its cells to one another                                                    |
| Bullet lists           | Use them for items that share no per-row relationship                                                       |
| Code blocks            | Use them where a shape is clearer shown than described                                                      |
| Placeholder names      | `Foo` stands for any type or val name, as in `FooCss`                                                       |
| Structure first        | Reach for the heading, table, list, or code block that fits the content. Prose carries what none of them can |
| No "what" openers      | A line never opens with "What". Name the thing directly                                                    |

Recording a convention does not mean writing at length. A convention that fits in a table row is a table row.

A document that runs past a page or two has started describing types.

## Sections

A package document is titled `# Package <name>`, or `# Module <name>` when it describes a whole Gradle module, so Dokka reads it as the description of that package or module. It then opens with these sections, in this order.

| Section | Holds |
|---|---|
| `## Introduction` | A brief description of the package domain |
| `## Dependencies` | The packages it depends on, each with the part it provides |
| `## Naming` | The naming patterns of its files and types, where the package has them |

The specification follows, under headings that name its topics. The document closes with these sections, where present, in this order.

| Section | Holds |
|---|---|
| `## Workflows` | Step lists for recurring tasks in the package |
| `## Known Issues` | Defects recorded as described below |

## Known Issues

A document may carry a `## Known Issues` heading at its end. An entry there names something wrong in the package and what a reader should do when they meet it.

This heading is the one place a document describes something other than the current convention, and the one place it carries something expected to change.

A document with no such heading has no known issues recorded. It does not follow that the package has none.
