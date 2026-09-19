# Package Documentation

`docs/packages/` holds one document per package. The filename is the fully qualified package name, for example `streetlight.web.model.md`.

A package gets a document when its contents follow conventions that are not obvious from reading a single file.

This document states what a package document contains. `docs/AGENTS.md` states when one is read and when it is amended.

## Documentation Content

A package document specifies where things go. The code describes what they are.

Include:

- Naming patterns for files and types
- Where a thing belongs when there are two options
- The shape of a typical member
- A rule
- Behavior the compiler does not enforce

Exclude:

- Type signatures and property lists
- Anything that changes when a field is renamed
- Descriptions restating the package name
- History, deprecations, or planned changes
- Exceptions to the convention
- Defects, inconsistencies, and unfinished work, except under Known Issues

State a rule without its justification. The statement is sufficient.

## Accuracy

A document describes the current state of the code and nothing else. It carries no history: no note that a type used to be named something else, no record of what is planned, no marker on a file that has not caught up yet. A reader treats every statement as true of the code as it stands.

An inaccurate document is worse than a missing one, because it will be believed.

## Style

| Rule | Detail |
|---|---|
| Direct language | No metaphors, analogies, or figures of speech |
| Plain headings | Name the topic: "Reading Documents", not "Reading them" |
| Brevity | Say what is required and stop |
| Single-line paragraphs | No hard wrapping within a paragraph |
| Tables | Use them where each row relates its cells to one another |
| Bullet lists | Use them for items that share no per-row relationship |
| Code blocks | Use them where a shape is clearer shown than described |
| Structure first | Reach for the heading, table, list, or code block that fits the content. Prose carries what none of them can |

Recording a convention does not mean writing at length. A convention that fits in a table row is a table row.

A document that runs past a page or two has started describing types.

## Known Issues

A document may carry a `## Known Issues` heading at its end. An entry there names something wrong in the package and what a reader should do when they meet it.

This heading is the one place a document describes something other than the current convention, and the one place it carries something expected to change.

A document with no such heading has no known issues recorded. It does not follow that the package has none.
