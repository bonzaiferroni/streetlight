# Package Documentation

`docs/packages/` holds one document per package. The filename is the fully qualified package name, for example `streetlight.web.model.md`.

A package gets a document when its contents follow conventions that are not obvious from reading a single file.

## Documentation Content

A package document describes where things go and why. The code describes what they are.

Include:

- Naming patterns for files and types
- Where a thing belongs when there are two options
- The shape of a typical member
- A rule and the reason behind it
- Behavior the compiler does not enforce

Exclude:

- Type signatures and property lists
- Anything that changes when a field is renamed
- Descriptions restating the package name
- History, deprecations, or planned changes
- Exceptions to the convention

State a rule with its reason. The reason lets a reader apply the rule to a case the document does not cover.

## Reading Documents

Read a package document before adding a file to that package, or before changing a convention other files follow.

## Writing Documents

Write a section when the convention is decided, not afterward. Update the document in the same pass as the code that changes it.

### Accuracy

A document describes the current state of the code and nothing else. It carries no history: no note that a type used to be named something else, no record of what is planned, no marker on a file that has not caught up yet. A reader treats every statement as true of the code as it stands.

An inaccurate document is worse than a missing one, because it will be believed.

### Style

| Rule | Detail |
|---|---|
| Direct language | No metaphors, analogies, or figures of speech |
| Plain headings | Name the topic: "Reading Documents", not "Reading them" |
| Brevity | Say what is required and stop |
| Single-line paragraphs | No hard wrapping within a paragraph |
| Tables | Use them where each row relates its cells to one another |
| Bullet lists | Use them for items that share no per-row relationship |
| Code blocks | Use them where a shape is clearer shown than described |

A document that runs past a page or two has started describing types.
