### Introduction

The `streetlight.web.ui` package serves as the primary orchestration and presentation layer for the Streetlight web application's client-side logic. It leverages the `koala.dom` DSL to define reactive UI components and manages the application's global state through the `AppContext` and `ClientContext` interfaces. This package bridges the gap between the domain models and the browser-based interactive experience.

### Dependencies

The most common packages this package depends on are:

* `koala.dom`: For reactive DOM manipulation and UI rendering.
* `koala.model`: For shared state primitives like `storeOf` and `Portal`.
* `streetlight.model.data`: For the core domain data structures.
* `streetlight.model`: For API endpoint definitions.
* `kotlinx.coroutines`: For asynchronous operations and reactive flows.
* `kampfire.api`: For shared user management and common API patterns.

### Structures

The package follows several key structural patterns to maintain a clean and modular web client:

* **Context Interfaces**: `AppContext` and `ClientContext` provide a consistent way to inject dependencies like `ApiClient`, `GeoMap`, `StreetMap`, and `UserGate` throughout the UI components.
* **View Functions**: Functions prefixed with `view` (e.g., `viewHome`, `viewApp`, `viewAccount`) are extension functions on `RenderContext` (from `koala.dom`) that define how a specific screen or component is rendered and wired to the application state.
* **API and Fetch Clients**: `ApiClient` provides high-level suspend functions for interacting with the backend API, while `FetchClient` handles the underlying HTTP requests, authentication, and serialization.
* **State Management**: Classes like `StreetMap` and `ChatRoom` encapsulate complex state and business logic, exposing reactive flows (`stateFlow`) to the UI.
* **Portal and Routing**: The `Portal` class manages the current navigation state and screen transitions, which are typically defined in `viewApp`.

### Workflows

(Initially empty)
