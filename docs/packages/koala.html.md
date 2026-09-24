# koala.html

Server-renderable components declared on `FlowContent`.

## Buttons

`btn` renders a link styled by `BtnStyle.Class`. A flair, when present, is its first child in a fixed column, and the text is centered in the region to its right, kept a `--unit` from the flair. Without a flair the text is centered in the whole button.

## Images

`image` and `containImage` share `configureImage`, which sets the `srcset`, `sizes="auto"`, the aspect ratio and lazy loading from an `Image`, falling back to `SiteImage.placeholder`.

`containImage` shows the image over a blurred, scaled backdrop of the same image. Its `object-fit` is set on the root and inherited by the content image, so a caller changes the fit on the root alone.

## Root Switch

`rootSwitch(attribute, mod) { value -> }` renders a row of element buttons, one per value of an enum root setting. The lambda builds each button's content. A button applies its value with `applyRootSwitch` and carries it under `attribute.optionAttribute`.

The app marks the selected button by adding `rootSwitchCss(attribute, values)` to its own stylesheet. The default style sets `color` to `--primary-fg`; a caller passes another style to change it.

`rootSwitchScript(attribute, fallback)` is the head script that restores the setting on load, applying the fallback when nothing is stored. The app places it in its page head.
