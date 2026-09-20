# Delegate Galaxy Host

A galaxy is managed by its hosts and by admins. The creator is recorded as a host of type `Creator` when the galaxy is founded. Nothing yet creates a host of type `Delegate`.

Bounty:
* Decide who may add and remove a delegate: the creator, any host, or admins as well.
* `Api.Galaxies` endpoints to add and remove a delegate host
* `GalaxyTableDao` methods that write and delete `GalaxyHostTable` rows of type `Delegate`
* Server-side checks before the DAO is reached: the caller may manage hosts, the target user exists, the target is not already a host, and the creator cannot be removed
* Confirm every guard that reads `GalaxyHostTable` (`UpdateGalaxy`, the live-post rule in `PostTableDao`) treats a delegate as a host
* View for the host list of a galaxy
* API tests in `GalaxyApiTest` covering a delegate updating the galaxy, a non-host being refused, and a removed delegate losing access
* Docs: the galaxy conventions in `streetlight.server.routes.md`, if any are decided
