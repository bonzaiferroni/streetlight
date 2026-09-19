# kampfire.model

## Problem

A `Problem` communicates a failed condition to the user. Its message is written for the user.

A condition that also implies logic beyond the message is the basis for that logic. The code that handles the condition matches on the `Problem` it received.

A problem is declared once, as a property of an object, and production code returns that property. A problem is never constructed inline where it is returned.

| Scope of the problem | Declared in |
|---|---|
| Shared across domains | `CoreProblem` |
| Specific to a domain | An object named `FooProblem`, where `Foo` is the domain, such as `AuthProblem`, `HttpProblem`, and `LMProblem` |

`CoreProblem.Something` is the catchall. It communicates an unexpected error in response to valid user input.
A test observes the same property production code returns. A fake returns it, and an assertion compares against it. A test never constructs a `Problem` of its own.
