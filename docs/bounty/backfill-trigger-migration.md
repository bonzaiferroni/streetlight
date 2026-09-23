# Backfill Trigger Migration

`CityTable.locationCount` was added with a default of 0 and is kept by `cityLocationCountTrigger`. Existing cities read 0 until backfilled. `galaxy.location_count` was inflated by a `CounterTrigger` whose filter never passed on delete.

`galaxyEventCountTrigger` was retired in favor of `TableDaemon`. Startup does not drop a trigger, so its trigger and functions remain in any database that had it.

Bounty:
* Generate the grouped migration with `generateMigration`
* Append a backfill `UPDATE` for `city.location_count` from a count of `location` by `city_id`
* Append a backfill `UPDATE` for `galaxy.location_count` from a count of `post` by `galaxy_id` where `post_type` is `Location`
* Append `DROP TRIGGER` for `trg_post_event_count_insert` and `trg_post_event_count_delete`, and `DROP FUNCTION` for `post_event_count_increment` and `post_event_count_decrement`
