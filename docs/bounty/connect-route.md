# Connect Route

A marketing page, `ConnectRoute`, pitching Streetlight to four audiences. A picker of the four drives the content beneath it.

## Shape

- `ConnectType` enum in `streetlight.model.data`: `BusinessOwner`, `Performer`, `Developer`, `EventHost`, each `Labeled`.
- `ConnectRoute(type: ConnectType?)` on `Screen.Connect`, the type written as the `type` query parameter. Its `screenKey` is `ConnectRoute()`, so choosing a type keeps the view, as a `FeedRoute` does.
- `connectPicker` in `streetlight.web.component`: the 2×2 grid from `homeShell`, each quadrant a navigation to `ConnectRoute(type)`. The quadrant matching the ancestor's `AppAttribute.ConnectType` takes `--primary-bg`.
- `connectShell(type)`: the header logo as on home, then a container row of the Lottie with "Connect to your community" beside the picker, then a panel per type. `AppAttribute.ConnectType` on the shell root selects the panel shown and the quadrant lit. A prompt shows while no type is chosen.
- `viewConnect` adopts the shell and sets the root attribute as the route's type changes.
- Server renders `Screen.Connect` from the query.

## Steps

1. 🍻 Model: `ConnectType`, `ConnectRoute`, `Screen.Connect`, `readConnectType`.
2. 🍻 `AppAttribute.ConnectType` and `ConnectOption`.
3. 🍻 `connectPicker` and its stylesheet; `homeShell` uses it.
4. 🍻 `connectShell`, `ConnectStyle` and its stylesheet.
5. 🍻 `viewConnect`, `viewPortal` branch.
6. 🍻 Server: `renderConnect`, `KtStyles`.
7. 🍻 Docs.
8. Content per audience (future stages).

Server and web JS compile clean after step 7.

## Decisions

- `ConnectType` lives in `streetlight.model.data`, beside `FeedType`.
- `getInvolved` moved out of `homeShell` into `connectPicker`, since both pages share it.
- The page needs no API content, so it has no data island and `AppContentFetcher` falls to `NullContent`, as About does.
- With no type chosen, a prompt panel shows in place of the content.
- The Lottie is `LottieFile.StreetlightNight`.
- The route dock stays empty on Connect. The home picker and outside marketing are its paths in.
- The picker is 3:2 with a 2px gap, each quadrant on `CardBg`.
