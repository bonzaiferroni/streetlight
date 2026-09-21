# koala.html

Server-renderable components declared on `FlowContent`.

## Route Menu

`routeMenu` renders the route menu, which sits at the bottom of the viewport and floats above the content. It holds the routes directly relevant to the view.

| Part | Holds |
|---|---|
| Context | A label above the menu naming what the routes belong to |
| Main section | The route labels as text, each linking to its route |
| Trays | Icons at the left and right edges, each an `IconRoute` |

The route equal to `routeNow` is marked as the current one. A `null` route in the list is skipped.

A tray icon is for a route that not every view wants, such as a config route.

`koala.dom` declares a `ViewScope` variant that shares the `RouteMenu` class object and its stylesheet.
