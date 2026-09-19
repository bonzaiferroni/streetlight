# koala

The base set of components and functionality for web apps. It declares `View`, a unit of UI within the browser, and the tools views are built from.

`koala` depends on `kampfire`. `kampfire` does not depend on `koala`.

## Launching Coroutines

Start a coroutine with `koala.utils.launch`, not the raw builder.

The browser does not give a coherent call stack for an exception thrown inside a coroutine. `launch` names the coroutine and carries the name in its context as `LaunchTelemetry`. `appExceptionHandler` appends that name to the exception message, along with the path of the view and element the coroutine belongs to, and rethrows the same throwable.

A coroutine launched with the raw builder fails without a name, and its message says nothing about where it came from.

Code that wraps launching builds on `koala.utils.launch` so that the telemetry is kept.

## DropWhileBusy

`DropWhileBusy` launches a block only when its previous launch has finished. A launch requested while one is active is dropped and returns `null`. The previous launch is never cancelled.

An action that must not run twice at once owns one instance. Two actions never share an instance.
