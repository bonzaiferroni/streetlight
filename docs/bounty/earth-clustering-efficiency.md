# Earth Clustering Efficiency

Zooming lags once the Events map has filled from its queries. Clustering is the suspect, but the cost may be split between scripting and rendering. Each change is benchmarked before and after, so the diagnosis and the improvement are both on record.

The code is in `koala/src/jsMain/kotlin/koala/model/GeoLayerRender.kt` and `PointMarkerElement.kt`.

## Benchmarking

* Record a Performance profile in the browser during a zoom on a filled map, such as downtown Denver after zooming in and out over the city.
* Note the marker count, and the split between scripting and rendering time per zoom.
* Repeat after each change with the same route and marker count.

## Findings

### Clustering is O(n²)

`defineClusters` compares every loaded marker with every other, visible or not. `setBounds` runs it at each whole zoom step during a zoom and again when the map settles.

Suggestion: bucket points into a grid whose cells are one cluster radius wide, and compare each point only with the points in its own and the 8 neighboring cells. Roughly O(n), contained in `defineClusters`.

### Hidden cluster members may still render

MapLibre repositions every HTML marker added to the map on every frame of a zoom. If `MarkerStyle.ClusterMember` hides a member with CSS while it stays attached, it still costs every frame. Not yet checked.

Suggestion: detach cluster members from the map, as `setIsVisible` does for markers outside the view, so the per-frame work covers only what is shown.

### Stale render removal is O(n²)

`setPoints` checks each existing render against the incoming markers with `markers.any { ... }`, on every query page.

Suggestion: build a set of the incoming marker ids and check against it.

### Cluster styles are rewritten every pass

`setCluster` rewrites the classes and the count `textContent` of every marker on every clustering pass, changed or not.

Suggestion: keep each render's current cluster and skip the update when it is unchanged.

## Order

Profile first. Scripting time points to the clustering and removal findings; rendering time points to hidden members and style rewrites.
