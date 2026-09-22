# Reintroduce Show All

The route menu on Earth held a `showAll` action, an `IconAction` with `SvgFile.FrameEye` that calls `Earth.showAll` to frame every marker on the map. The route dock holds routes only, so the action has no home.

Bounty:
* Decide where the action lives within the Earth view
* Render the control and bind it to `Earth.showAll`
* Docs: the placement in `streetlight.web.ui.md`, if a convention is decided
