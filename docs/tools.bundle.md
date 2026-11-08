# tools/bundle

`measure.py` reads the production bundles in `web/build/kotlin-webpack/js/productionExecutable` and prints a markdown entry for `logs/bundle`, whose README says when and how to take one.

A chunk's role comes from its file: `web.js` is Main, another named file is a Standalone, and a numbered chunk is named by the section portal it holds, from `ROLES`. A new lazy section adds its portal file there.

Sizes per group come from the source maps: each mapped span of output is credited to its source, and the source to our module and package or to a library by the patterns in `LIBRARIES`. A source no pattern matches lands in `other`; when `other` grows, a pattern is missing.
