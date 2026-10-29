# Package streetlight.compose.modifier

## Introduction

The theme's styling as extensions on Compose's `Modifier`, the cousins of `koala.modifier`'s classes.

## Dependencies

| Package | Provides |
|---|---|
| `androidx.compose.ui.node` | `Modifier.Node` and its kinds |
| `streetlight.compose.view` | `LocalKoalaAppTheme`, `LocalSchemeColor` |

## Web Counterparts

| Compose | Web |
|---|---|
| `borderRadius(n)` | `BorderRadius(n)`; its `moonShadow` flag gives `MoonShadow`, and its `outline` level gives `OutlineLow` or `Outline`, `--outline-low` or `--outline-mid` |
| `borderRadius(topStart, topEnd, bottomEnd, bottomStart)` | `border-radius` with four values, in units |
| `zenBg()`, `inkBg()`, `cardBg()`, `heavyCardBg()` | `ZenBg`, `InkBg`, `CardBg`, `HeavyCardBg` |
| `shineBg()` | `ShineBg` |
| `schemeBg()` | `SchemeBg` |
| `primaryBg()`, `accentBg()` | `PrimaryBg`, `AccentBg` |
| `padding(n)` | `padding: var(--unit)`, `PaddingX1`, `PaddingY1` |
| `opacityLow()`, `opacityHigh()` | `OpacityLow`, `OpacityHigh` |
| `fadeClip()` | The feed row's text clip and its mask |
| `blockLabel(label, isValid)` | `blockLabel` and `flowValid` (`koala.dom`), and `BlockLabelCss` |
| `filigree()`, `filigree(FiligreeStyle.Ray)` | `filigree` (`koala.component`) and its `ParticleRay`; the rays of `rayHeader` (`streetlight.web.page`) |

## Modifiers

A styling the web gives with a `koala.modifier` class is an extension here, named for its web cousin in Compose's camel case. A length the web gives in units is given in units, as a `Number` the extension turns to a `Float`, so a whole or fractional count reads plainly. The set holds what the app uses; it is not a copy of `koala.modifier`, and a styling Compose already gives plainly, such as a text color, has none.

Each is a `Modifier.Node` that reads the theme with `currentValueOf(LocalKoalaAppTheme)`, so a call site needs no theme in hand and chains like any modifier. The theme holds the values, such as `KoalaOpacity`, and the node holds only how they are applied. A node that reshapes or fades its content places it with a layer; one that draws around it is a `DrawModifierNode`. A styling that follows the corners, such as a shadow or an outline, is a parameter of `borderRadius()` rather than a modifier of its own, so the corners are given once.

A modifier lives in the file that corresponds to its web cousin's stylesheet: `DisplayModifiers.kt` for `DisplayCss`, as borders, backgrounds, shadows, opacity and clips, `LayoutModifiers.kt` for `LayoutCss`, as padding, `MagicModifiers.kt` for `MagicCss`, as `filigree()` and its rays, and `TextModifiers.kt` for `TextCss` when one is needed. A modifier whose drawing outgrows its stylesheet's file takes a file of its own, as `ShineBg.kt`. `ModifierUtility.kt` holds what the nodes share, such as their read of the theme.

## Block Label

`blockLabel` draws its label over the top edge of its content, at the end, as the web's `::after` does, taking no room: the layout around a labeled part leaves the label room above it. The label's background tells a part's validity, as the web's `Required` and `Valid` do: plain for a part with no check, the required color while it is missing, the valid color once given. A form reads a part's validity from its edit's `ValidityCheck` with `isValid(key)`. A `TextField` shows the same check without a label, tinting its icon and outline by its `isValid`, so a form that needs no labels, such as signing in, keeps a light touch.

## Padding

`padding()` takes units, read while measuring. A padding in `Dp` is foundation's, and a file using both imports both.

## Scheme Surface

`schemeBg()`, `primaryBg()` and `accentBg()` paint a color over paper, a circle at the center reaching the farther sides, fading from a stronger mix to a fainter one, through `KoalaColors.radialBgOf`. Compose's gradients take no color hint, so the hint's curve is traced in stops. The web's `SchemeBg`, `--primary-bg` and `--accent-bg` are kept in step with it.

## Shine Surface

`shineBg()` paints paper tinted with ink, lit by the page's lights as if they shone on it from around it, each pooling on the edge its direction from the page's center points to. `ShineReach` sets how far a light reaches. The web's `ShineBg` is kept in step with it.

## Animated Modifiers

A modifier that moves, such as `filigree()`, runs its own frame clock from the node's `coroutineScope`, launched in `onAttach`, and reads the time only while drawing, so a frame redraws its node alone.

A node that both places its content and draws, as `filigree()` does, draws in the space of the content it placed: the origin is the content's corner and `size` is the content's. Drawing beside the content uses the offsets the node measured.
