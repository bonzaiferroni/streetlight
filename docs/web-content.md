### Introduction

The `www` directory serves as the root for static assets and client-side resources for the project. It contains all the CSS, JavaScript, images, and other resources that are delivered directly to the browser or consumed by the client-side application.

### Structure

The directory is organized into several functional subdirectories:

* **css/**: Contains all standard CSS files. The project is currently transitioning from dynamic Kotlin CSS DSL (in `koala.css`) to static CSS files in this directory.
* **img/**: Static image assets, including JPEGs and PNGs used throughout the application.
* **js/**: JavaScript source and compiled outputs. This includes:
    * **koala/** and **streetlight/**: Compiled Kotlin/JS modules (e.g., `koala.js`, `web.js`).
    * **Handwritten scripts**: Files like `launchApp.js`, `utils.js`, and `geoMap.js` provide targeted functionality.
* **lottie/**: JSON animation files for use with the Lottie web library.
* **svg/**: Vector graphics used for icons, markers, and UI elements.
* **proto/**: Protocol Buffer definitions and serialized binary data used for communication (e.g., `gtfs-realtime.proto`).

### CSS Organization

The CSS is split into several functional files to maintain a modular structure:

* `styles.css`: The main entry point, containing root variables (`:root`), base HTML element styles, and core layout components like `.query-row`.
* `utilities.css`: A comprehensive set of utility classes for margins, padding, layout alignment, and common visual properties.
* `layout.css`: Fundamental layout classes (e.g., `.column`, `.row`).
* `typography.css`: Definitions for fonts, sizes, and text-specific styling.
* `animation.css`: Keyframes and classes for UI animations.

### Workflows

The following workflows are used to manage content in this directory:

#### AddStaticAsset(AssetType, Name)
* Place the new asset in the appropriate subdirectory (`img`, `svg`, `lottie`).
* If it is an image or icon used in the UI, check if it needs to be referenced in a CSS file or a Kotlin model.

#### CreateCssUtility(Name)
* Identify the appropriate file in `www/css/` (usually `utilities.css`).
* Add the standard CSS rule.
* If it replaces a Kotlin CSS DSL object, ensure the Kotlin object is updated to point to the new CSS class name (if different) and eventually deprecated or removed.

#### UpdateStyles(Category)
* When updating core styles (e.g., branding, layout), modify `www/css/styles.css` and the corresponding variable in `:root`.
