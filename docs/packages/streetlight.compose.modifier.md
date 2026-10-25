# Package streetlight.compose.modifier

## Introduction

The theme's styling as extensions on Compose's `Modifier`, the cousins of `koala.modifier`'s classes.

## Dependencies

| Package | Provides |
|---|---|
| `androidx.compose.ui.node` | `Modifier.Node` and its kinds |
| `streetlight.compose.view` | `LocalKoalaAppTheme` |

## Web Counterparts

| Compose | Web |
|---|---|
| `borderRadius(n)` | `BorderRadius(n)` |
| `outline()` | `Outline`, `--outline-low` |
| `moonShadow()` | `MoonShadow` |
| `zenBg()`, `cardBg()` | `ZenBg`, `CardBg` |
| `opacityLow()`, `opacityHigh()` | `OpacityLow`, `OpacityHigh` |
| `fadeClip()` | The feed row's text clip and its mask |
| `filigree()` | `filigree` (`koala.component`) and its `ParticleRay` |

## Modifiers

A styling the web gives with a `koala.modifier` class is an extension here, named for its web cousin in Compose's camel case. A length the web gives in units is given in units. The set holds what the app uses; it is not a copy of `koala.modifier`, and a styling Compose already gives plainly, such as a text color or a padding of `currentTheme.unit`, has none.

Each is a `Modifier.Node` that reads the theme with `currentValueOf(LocalKoalaAppTheme)`, so a call site needs no theme in hand and chains like any modifier. The theme holds the values, such as `KoalaOpacity`, and the node holds only how they are applied. A node that reshapes or fades its content places it with a layer; one that draws around it is a `DrawModifierNode`.

A modifier lives in the file that corresponds to its web cousin's stylesheet: `DisplayModifiers.kt` for `DisplayCss`, as borders, backgrounds, shadows, opacity and clips, `MagicModifiers.kt` for `MagicCss`, as `filigree()` and its rays, and `TextModifiers.kt` for `TextCss` when one is needed. `ModifierUtility.kt` holds what the nodes share, such as their read of the theme.

## Animated Modifiers

A modifier that moves, such as `filigree()`, runs its own frame clock from the node's `coroutineScope`, launched in `onAttach`, and reads the time only while drawing, so a frame redraws its node alone.

A node that both places its content and draws, as `filigree()` does, draws in the space of the content it placed: the origin is the content's corner and `size` is the content's. Drawing beside the content uses the offsets the node measured.
