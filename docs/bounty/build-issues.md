# Build Issues

Two failures of the dev build, each cleared by hand. The bounty is to find their causes and fix them at the source, so neither returns.

## Stale Mangled Names

Signing out in the dev build threw:

```
Uncaught Error: this.this$0__1.portal_1.go_90ugqf_k$ is not a function
Launch: signOut
```

`SessionClient.signOut` calls `Portal.go(route: AppRoute)`, which exists with that signature. The `Portal` instance is there, but its compiled class lacks the mangled name the caller used, so the caller and the callee were compiled against different signatures. The same error family turned up on 2026-09-24 as `copy$default_jkz4uy_k$` in `LocationEditor`, after `CityId` changed from `Int` to `Uuid`. A clean fixed it then.

Theories:
* The Kotlin/JS IR incremental cache keeps a fragment of `streetlight-web` compiled against an older `koala`. The mangled hash of `go` covers its parameter types, and `AppRoute` changed when its label moved to `LabeledRoute`. Upstream: KT-63207, KT-61795.
* The incremental cache is on by default in Kotlin 2.4, and `gradle.properties` does not set `kotlin.incremental.js.ir`. Production builds do not use it, so only dev is affected.

To confirm, when it next appears:
* Compare the mangled name of the method in `streetlight-koala.js` and in `streetlight-web.js` under `web/build/compileSync`, before cleaning.
* Note which declaration changed since the last good build.

Candidate fixes:
* Disable `kotlin.incremental.js.ir` and measure what it costs a dev build.
* Keep it, and find the change that the cache fails to invalidate, for an upstream report.

## Root-Owned Build Directory

After a `clean`, the build failed:

```
Execution failed for task ':kampfire:jsPackageJson'.
> Failed to create parent directory '.../streetlight/build/js' ...
Failed to stop service '...GradleNodeModulesCache'.
> .../build/js/packages_imported/.visited-gradle (No such file or directory)
```

The root `build/` folder was owned by root, created at 17:32 on 2026-10-07 with only `reports/problems` inside. Both Gradle daemons ran as `starfox`, so Gradle had run once as root, and the user's build could not write into the folder. Removing it with `sudo rm -rf build` cleared the failure. A Gradle update was seen during the clean. The wrapper is pinned at 8.13.

Theories:
* The clean ran under `sudo`, or from a tool that runs as root, such as an IDE, a container that mounts the project, or a system `gradle` rather than `./gradlew`. A root run creates `build/reports/problems` for its problems report.
* The update came from a Gradle other than the wrapper, which points to a system `gradle` run as root.

Bounty:
* Find what ran Gradle as root at 17:32: shell history, IDE run configurations, and any container or script that invokes Gradle
* Find which Gradle performed the update, and whether it was the wrapper
* Confirm the cause of the stale mangled names when they next appear, and choose between the candidate fixes
* Docs: the dev build's known failures and their fixes in `docs/AGENTS.md`, or where builds are documented
