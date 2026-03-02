### Introduction

The `streetlight.web.pages` package contains the high-level DSL definitions for constructing HTML pages in the Streetlight web application. These functions typically combine the application's layout components, such as `appBody`, with specific content shells to form complete `HTML` document structures.

### Dependencies

The most common packages this package depends on are:

* `koala.html`: For core HTML component DSLs and utilities.
* `koala.css`: For styling classes and layout modifications.
* `kotlinx.html`: The base library for Kotlin-based HTML generation.
* `streetlight.model.data`: For accessing domain models like `Event` and `Location` to be displayed on pages.
* `streetlight.web.shells`: For including the reusable UI "shells" that define the main content areas of the pages.

### Structures

The package follows a consistent structure for defining web pages and common layout elements:

* **Page DSLs**: Functions like `homePage`, `eventPage`, and `locationPage` are extensions on `HTML`. They generally call a `head` function to set metadata and external supports (like maps or protobuf), then invoke `appBody` to wrap the main content.
* **Layout Components**: `appBody` and `appHeader` define the persistent visual structure of the site, including the navigation bar, logo, and main content slots (`shellBox`).
* **Support Functions**: Extension functions on `HEAD` (found in `supports.kt`) manage the inclusion of external scripts and stylesheets, such as MapLibre for geographic maps or Protobuf for data serialization.
* **AppBody Object**: A central object that maintains a set of `Id` constants used for targeting specific DOM elements during client-side interactions and styling.
