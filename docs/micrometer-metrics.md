# Micrometer Metrics Registry Catalog

## Critical Note

`ktor.http.server.requests` — the timer we depend on for request count and latency — is **absent** from the registry. Only `ktor.http.server.requests.active` (a gauge of in-flight requests) is present. This explains why the daemon records zero for all request-based metrics.

## Ktor

| Metric | Tags | Type | Description |
|--------|------|------|-------------|
| ktor.http.server.requests.active | — | Gauge | Number of currently in-flight requests |

## System

| Metric | Tags | Type | Description |
|--------|------|------|-------------|
| system.cpu.count | — | Gauge | Number of available CPU cores |
| system.cpu.usage | — | Gauge | System-wide CPU usage (0.0–1.0) |
| system.load.average.1m | — | Gauge | 1-minute system load average |

## Process

| Metric | Tags | Type | Description |
|--------|------|------|-------------|
| process.cpu.time | — | Counter | Total CPU time consumed by the JVM process (nanoseconds) |
| process.cpu.usage | — | Gauge | CPU usage of the JVM process (0.0–1.0) |
| process.files.max | — | Gauge | Maximum file descriptors allowed |
| process.files.open | — | Gauge | Currently open file descriptors |
| process.start.time | — | Gauge | Process start time (epoch seconds) |
| process.uptime | — | Gauge | Process uptime (seconds) |

## JVM Memory

| Metric | Tags | Type | Description |
|--------|------|------|-------------|
| jvm.memory.used | area (heap/nonheap), id | Gauge | Memory currently in use |
| jvm.memory.committed | area, id | Gauge | Memory committed by the JVM |
| jvm.memory.max | area, id | Gauge | Maximum memory available |

Observed memory pool IDs: G1 Eden Space, G1 Old Gen, G1 Survivor Space, Metaspace, Compressed Class Space, CodeHeap 'profiled nmethods', CodeHeap 'non-profiled nmethods', CodeHeap 'non-nmethods'.

## JVM Garbage Collection

| Metric | Tags | Type | Description |
|--------|------|------|-------------|
| jvm.gc.pause | action, cause, gc | Timer | GC pause duration |
| jvm.gc.concurrent.phase.time | action, cause, gc | Timer | Concurrent GC phase duration |
| jvm.gc.memory.allocated | — | Counter | Bytes allocated in young generation |
| jvm.gc.memory.promoted | — | Counter | Bytes promoted to old generation |
| jvm.gc.live.data.size | — | Gauge | Size of live data in old gen after full GC |
| jvm.gc.max.data.size | — | Gauge | Maximum size of old generation |

## JVM Threads

| Metric | Tags | Type | Description |
|--------|------|------|-------------|
| jvm.threads.states | state | Gauge | Thread count by state |
| jvm.threads.live | — | Gauge | Current live thread count |
| jvm.threads.peak | — | Gauge | Peak live thread count |
| jvm.threads.started | — | Counter | Total threads started since JVM start |
| jvm.threads.daemon | — | Gauge | Current daemon thread count |

Observed thread states: new, runnable, blocked, waiting, timed-waiting, terminated.

## JVM Buffers

| Metric | Tags | Type | Description |
|--------|------|------|-------------|
| jvm.buffer.count | id | Gauge | Number of buffers in the pool |
| jvm.buffer.memory.used | id | Gauge | Memory used by buffers |
| jvm.buffer.total.capacity | id | Gauge | Total capacity of buffers |

Observed buffer pool IDs: direct, mapped, mapped - 'non-volatile memory'.

## JVM Classes

| Metric | Tags | Type | Description |
|--------|------|------|-------------|
| jvm.classes.loaded | — | Gauge | Currently loaded classes |
| jvm.classes.unloaded | — | Counter | Total classes unloaded since JVM start |