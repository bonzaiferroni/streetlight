# Subdomain View Slug Availability

The server rejects a subdomain that is malformed, reserved, or held by another location. `Api.Locations.CheckSubdomain` and `LocationClient.checkSubdomain` report availability, but no view calls them. `subdomainSection` in `locationSettingsForm.kt` checks only the slug format before it sends the save.

Bounty:
* `subdomainSection` checks availability through `api.location.checkSubdomain` before it sends the save
* Show the availability result beside the field, using the view's `MessageStore`
* Decide whether to check while typing, and if so debounce the calls
* Turn a `false` result and a `Problem` into distinct messages
* Give `TestLocationClient.checkSubdomain` a configurable answer, following the fake conventions in `docs/testing.md`
* Integration tests in `streetlight.web.integration` covering an available name, a taken name and a failed check
* Docs: `streetlight.web.integration.md` if the fixtures change
