# Earth Clustering Efficiency

Zooming lags once the Events map has filled from its queries. Clustering is the suspect, but the cost may be split between scripting and rendering. Each change is benchmarked before and after, so the diagnosis and the improvement are both on record.

The code is in `koala/src/jsMain/kotlin/koala/model/GeoLayerRender.kt` and `PointMarkerElement.kt`.

## Benchmarking

* Record a Performance profile in the browser during a zoom on a filled map, such as downtown Denver after zooming in and out over the city.
* Note the marker count, and the split between scripting and rendering time per zoom.
* Repeat after each change with the same route and marker count.

## Findings

### Clustering is O(n²) — pardoned

`defineClusters` compares every loaded marker with every other. Measured over a session of zooming over Denver: 63 runs, median 2.3ms, max 9ms, about 180ms in all. It does not break a frame and is not worth changing.

### Hidden cluster members may still render

MapLibre repositions every HTML marker added to the map on every frame of a zoom. If `MarkerStyle.ClusterMember` hides a member with CSS while it stays attached, it still costs every frame. Not yet checked.

Suggestion: detach cluster members from the map, as `setIsVisible` does for markers outside the view, so the per-frame work covers only what is shown.

### Stale render removal is O(n²)

`setPoints` checks each existing render against the incoming markers with `markers.any { ... }`, on every query page. The cost was a `markerId` getter that formats a Uuid on each read: median 14ms and max 39ms per run. Storing `markerId` on `InflateMarker` brought it to a max of 1ms.

The model entities (`EventGroup`, `Location`, `Event`, `Media`, `Galaxy`) still format `markerId` on each read, and `MarkerMap.createAndSetMarkers` reads it in an O(n²) loop.

Suggestion: build a set of the incoming marker ids and check against it.

### Cluster styles are rewritten every pass

`setCluster` rewrites the classes and the count `textContent` of every marker on every clustering pass, changed or not.

Suggestion: keep each render's current cluster and skip the update when it is unchanged.

## Order

Profiling with `koala.bench.markAndMeasure` put the main cost in MapLibre repositioning HTML markers each frame and the layout that follows. Hidden cluster members come next.
