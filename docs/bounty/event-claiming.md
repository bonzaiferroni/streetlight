# Event Claiming

Any signed-in user may edit an event that has no host. Once an event has a host, only the host may edit it. `EventTable.hostId` is nullable, and the update guard reads it: an event is editable when `hostId` is null or the caller is the host. An event's creator becomes its host only when the creation dto sets `EventEdit.isHost`. Nothing else sets `hostId`.

Bounty:
* Decide how an event is claimed: who may claim, and whether a claim needs review
* `Api.Events` endpoints to claim and release an event
* `EventTableDao` methods that set and clear `hostId`, allowed only for an unclaimed event or for its current host
* Server-side checks before the DAO is reached: the event exists, the caller may claim it, and a release comes from the host or an admin
* Decide whether an admin may edit a hosted event. Delete is settled: an admin deletes any event, the host deletes a hosted one, and nobody else deletes.
* Rename the `scout` column of `EventTable`, which now holds the host's username, to `host`, as listed in `host-id-migration.md`
* Decide whether a community edit to an unclaimed event goes to review or applies at once, given that `updateEvent` writes an edit log
* View controls to claim and release an event
* The event form needs a control that sets `EventEdit.isHost` when the creator says they are hosting, and the edit form needs to keep the value it read
* Show the host on the event page, and show an unclaimed event as open to community edits
* API tests in `EventApiTest` for claiming, releasing, a second user being refused a claimed event, and an admin override if one is decided
* Schema: the `host_id` column reaches production through `host-id-migration.md`
* Docs: the event conventions in `streetlight.server.routes.md`, if any are decided
