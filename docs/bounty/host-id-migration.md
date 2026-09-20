# Host Id Migration

`EventTable.scoutId` was replaced by `EventTable.hostId`, and the `eventUsernameSync` trigger now reads `hostId`. The development database was updated by dropping `trg_event_sync_scout` and `scout_id`, then letting `raiseSchema` add `host_id`. That is enough only where `event` holds no rows. A database that holds events needs a rename so each creator becomes the host.

Bounty:
* Confirm on production, with `\d event`, the name of the foreign key on `scout_id` and whether an index exists on it
* Confirm the foreign key name and the index name that Exposed generates for `host_id`
* Write the migration for the migration tool: drop `trg_event_sync_scout`, rename `scout_id` to `host_id`, rename the foreign key to the name Exposed expects, create the `host_id` index if it is missing
* Decide the meaning of existing rows: the rename makes every existing creator a host, which closes their events to community edits
* Run the migration where the tool applies it, once the change is stable in development
* Confirm the first startup after it changes nothing further: no second foreign key, no duplicate index, the trigger and both functions recreated on `host_id`
* Rename the `scout` text column of `event` to `host` in a later migration, together with the query mappers that read it
* Docs: a line in `streetlight.server.db.tables.md` if the migration tool has a convention worth recording
