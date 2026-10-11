# Package streetlight.web.dom.page

## Introduction

The page frame in the browser: the page theme and the footer of a view.

## Dependencies

| Package | Provides |
|---|---|
| `koala.dom` | The view runtime |
| `streetlight.web.page` | The server-rendered page frame and its theme properties |

## Page Frame

| Part | Role |
|---|---|
| [`applyTheme`](../../web/src/jsMain/kotlin/streetlight/web/dom/page/applyTheme.kt) | Sets a theme on the body, or clears the page theme |
| [`appFooter`](../../web/src/jsMain/kotlin/streetlight/web/dom/page/appFooter.kt) | Appends the footer and starts its animation |
