<div align="center">

# invertoTimer

[![Stars](https://img.shields.io/github/stars/Our-Island/invertoTimer?style=flat-square&logo=data:image/svg%2bxml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZlcnNpb249IjEiIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiI+PHBhdGggZD0iTTggLjI1YS43NS43NSAwIDAgMSAuNjczLjQxOGwxLjg4MiAzLjgxNSA0LjIxLjYxMmEuNzUuNzUgMCAwIDEgLjQxNiAxLjI3OWwtMy4wNDYgMi45Ny43MTkgNC4xOTJhLjc1MS43NTEgMCAwIDEtMS4wODguNzkxTDggMTIuMzQ3bC0zLjc2NiAxLjk4YS43NS43NSAwIDAgMS0xLjA4OC0uNzlsLjcyLTQuMTk0TC44MTggNi4zNzRhLjc1Ljc1IDAgMCAxIC40MTYtMS4yOGw0LjIxLS42MTFMNy4zMjcuNjY4QS43NS43NSAwIDAgMSA4IC4yNVoiIGZpbGw9IiNlYWM1NGYiLz48L3N2Zz4=&logoSize=auto&label=Stars&labelColor=444444&color=eac54f)](https://github.com/Our-Island/invertoTimer/)
[![GitHub CI](https://img.shields.io/github/actions/workflow/status/Our-Island/invertoTimer/ci.yml?style=flat-square&labelColor=444444&branch=master&label=GitHub%20CI&logo=github)](https://github.com/Our-Island/invertoTimer/actions/workflows/ci.yml)
[![Hangar](https://img.shields.io/badge/Hangar-invertoTimer-004ee9?style=flat-square&labelColor=444444)](https://hangar.papermc.io/Our-Island/invertoTimer)
[![Modrinth](https://img.shields.io/badge/Modrinth-invertoTimer-22ff84?style=flat-square&labelColor=444444)](https://modrinth.com/plugin/invertotimer/)

</div>

invertoTimer is a lightweight Velocity plugin for global countdowns and scheduled server-wide events.  
It’s designed for “whole network” moments such as New Year, grand openings, maintenance reminders, and other
timed announcements — with configurable timers, multiple display modes, flexible actions, and text animations.

---

## Introduction

invertoTimer provides:

- Timers based on **cron** (repeat) or **fixed time** (one-shot)
- **Showcases** to display countdowns (actionbar / bossbar / chat / title)
- **Actions** to trigger operations at offsets (send text, transfer players, run commands)
- Server limitations (**global + per-timer**) to include/exclude specific backend servers
- i18n language files and **MiniMessage** formatting for player-visible text
- `{i18n:key}` tokens for translating player-visible texts
- `{animation:<id>}` placeholder to render animated text defined in `animations.yml`

The plugin uses three config files:

- `config.yml`: global settings (language, timezone, global limitation, etc.)
- `timer.yml`: timer definitions (timers, showcases, actions)
- `animations.yml`: animation definitions used by `{animation:<id>}`

---

## Timers

Timers are configured under `timers:` in `timer.yml`.

A timer consists of:

- `id` (the YAML key under `timers:`)
- `description`
- time spec: `cron` **or** `time`
- optional per-timer `limitation`
- `showcases` (periodic displays)
- `actions` (one-shot triggers)

### Time specification

Choose **ONE**:

- `cron`: 5-field cron format: `min hour day month week`
- `time`: one-shot absolute time: `yyyy-MM-dd HH:mm:ss`

Example:

```yml
timers:
  new-year:
    description: "New year timer."
    cron: "0 0 1 1 *"
    # time: "2026-01-01 00:00:00"
```

If `time` is in the past, that timer will not run again.

---

## Text Rendering

Most **player-visible** texts support a unified rendering pipeline:

### 1) `{i18n:key}` token

Use `{i18n:key}` to insert text from language files.

Example:

```yml
text: "{i18n:itimer.command.list.header}"
```

### 2) Placeholders

These are replaced for countdown-related texts:

- `{id}` timer id
- `{description}` timer description
- `{remaining}` formatted remaining time (HH:MM:SS or similar)
- `{days}` `{hours}` `{minutes}` `{seconds}`
- `{total_seconds}`
- `{target}` target time string
- `{animation:<id>}` render an animation frame (see **Animations**)

> `{animation:<id>}` can be used anywhere a normal text string is rendered (showcases, `after.text`, text actions,
> etc.).

### 3) MiniMessage formatting

Player-visible texts support MiniMessage tags such as:

- `<red>text</red>`
- `<gold>`, `<yellow>`, `<gray>`
- `<gradient:red:yellow>...</gradient>`

---

## Animations

Animations are defined in `animations.yml` and referenced using:

```yml
{ animation:<id> }
```

An animation produces a piece of text that changes over time (frames).  
The resulting frame text is then rendered like normal text, meaning it can also contain `{remaining}`, `{i18n:...}` and
MiniMessage tags.

### `animations.yml` format

Top-level key:

- `animations:` map of animation ids → definitions

Two supported definition styles:

#### A) Simple fixed-interval frames

```yml
# ============================================================
# Configured animations
# ============================================================
animations:
  # ----------------------------------------------------------
  # Animation id (used in timer text)
  # ----------------------------------------------------------
  new-year-bossbar:
    # Frame interval in seconds
    interval: 0.5
    # List of frames (cycled)
    text:
      - "Happy New Year!"
      - "New Year in {remaining}"
```

#### B) Per-frame duration

```yml
animations:
  new-year-bossbar2:
    # Explicit frames with individual durations (seconds)
    frames:
      - duration: 0.5
        text: "Happy New Year!"
      - duration: 1
        text: "Happy!"
```

### Using animations in timers

Example (bossbar text uses an animation):

```yml
timers:
  new-year:
    description: "New year timer."
    cron: "0 0 1 1 *"
    showcases:
      bossbar:
        start-at: 1h
        interval: 1s
        color: red
        text: "{animation:new-year-bossbar}"
```

---

## Showcases

Showcases are **periodic displays** that run while a timer is active.  
They are configured under `timers.<id>.showcases`.

Supported showcase types:

- `actionbar`
- `bossbar`
- `text` (chat message)
- `title` (single entry that supports both title + subtitle)

### Common fields

- `start-at`: when to start showing **before** the target time  
  Examples: `10m`, `30s`, `1h`, `1d`
- `interval`: how often to update/show  
  Examples: `1s`, `5s`, `30s`, `1m`

If `start-at` is omitted, the showcase begins immediately once the timer has a next target.

### After-target display (Actionbar / Bossbar / Title)

`actionbar`, `bossbar`, and `title` support an optional `after:` block.  
This allows you to keep showing a message **after the countdown reaches 0**, for a configured duration.

```yml
after:
  text: "Happy New Year!"
  duration: 10m
```

The “after” stage is active during `[target, target + after.duration]`.

### Actionbar showcase

```yml
showcases:
  actionbar:
    start-at: 1h
    interval: 1s
    text: "<yellow>New Year in</yellow> <white>{remaining}</white>"
    after:
      text: "<gold>Happy New Year!</gold>"
      duration: 10m
```

### Bossbar showcase (with color)

Bossbar supports an optional `color` field:

- `pink | blue | red | green | yellow | purple | white` (case-insensitive)
- invalid/missing values fall back to a safe default

```yml
showcases:
  bossbar:
    start-at: 1h
    interval: 1s
    color: red
    text: "<gradient:red:yellow>New Year in {remaining}</gradient>"
    after:
      text: "<gold>Happy New Year!</gold>"
      duration: 10m
```

Bossbar progress is based on `start-at`:

- e.g. `start-at: 1h` means progress decreases from `1.0` → `0.0` over that 1 hour.

### Text (chat) showcase

Chat text does not have `after:` in the current design; it is meant for periodic messages leading up to the target.

```yml
showcases:
  text:
    start-at: 10m
    interval: 30s
    text: "<gray>[Timer]</gray> <white>{remaining}</white> left"
```

### Title showcase (single “title” entry)

The **title** showcase is a single key (`showcases.title`) that contains both title and subtitle text.

It uses a **unified text format** compatible with text actions:

- `text` may be a String or a List/Array
- List/Array supports title+subtitle and optional timing

Recommended format:

```yml
showcases:
  title:
    start-at: 10m
    interval: 5s
    text:
      - "<gold>New Year</gold>"      # title
      - "<gray>{remaining}</gray>"   # subtitle
      - "0"                          # fadeIn seconds (optional)
      - "2"                          # stay seconds   (optional)
      - "0"                          # fadeOut seconds(optional)
    after:
      text:
        - "<gold>Happy New Year!</gold>"
        - "<gray>🎉</gray>"
        - "0"
        - "2"
        - "0"
      duration: 30s
```

If `text` is a plain string, it is treated as the main title, and subtitle becomes an empty string.

---

## Actions

Actions are **one-shot triggers** executed at a time offset relative to the target time.

Configured under `timers.<id>.actions`.

### Common fields

- `type`: `text` | `transfer` | `command`
- `shift`: time offset relative to target
    - negative = before target (e.g. `-30m`)
    - zero = at target (e.g. `0s`)
    - positive = after target (e.g. `5s`)

### 1) Text action

Sends different types of text to players.

```yml
- type: text
  shift: -30m
  options:
    text-type: message
    info: "<green>Ready?</green>"
```

`text-type`:

- `message` (chat)
- `actionbar`
- `title`
- `subtitle`

#### Title/Subitle “info” format + timings

For `text-type: title` or `text-type: subtitle`, `info` can be:

**A) String**

- `text-type: title` → title = info, subtitle = `""`
- `text-type: subtitle` → title = `""`, subtitle = info

**B) Array/List (recommended)**

`info: [title, subtitle, fadeIn, stay, fadeOut]`

Index mapping:

- `0` → title text
- `1` → subtitle text
- `2` → fadeIn seconds (optional)
- `3` → stay seconds (optional)
- `4` → fadeOut seconds (optional)

Example:

```yml
- type: text
  shift: 0s
  options:
    text-type: title
    info:
      - "<gold>Happy New Year!</gold>"
      - "<gray>{remaining}</gray>"
      - "0"
      - "2"
      - "0"
```

Title/subtitle/actionbar/message texts support `{i18n:key}` + placeholders + MiniMessage.  
Animations can also be used here via `{animation:<id>}`.

### 2) Transfer action

Transfers matched players to a backend server.

```yml
- type: transfer
  shift: -15m
  options:
    target: hub
    transferee: ".*"
```

- `target`: backend server name in Velocity
- `transferee`: **Java regex** to match online player usernames (`matches()` full match)
    - all players: `".*"`
    - starts with `Steve_`: `"^Steve_.*$"`
    - only Alice/Bob: `"^(Alice|Bob)$"`

### 3) Command action

Executes a configured command.

```yml
- type: command
  shift: 5s
  options:
    executor: player
    match: ".*"
    command: "/function t"
```

- `executor: console` runs as proxy console (ignores `match`)
- `executor: player` runs via the player's backend (`spoofChatInput`) for matched players
- `match`: Java regex to match online player usernames (`matches()`)
- `command` is treated as a **plain string**. Do not use MiniMessage here.  
  Placeholders are only available if your string-rendering pipeline applies them.

---

## Quick Start

Install:

- Download the jar from Hangar/Modrinth (or build from source)
- Put it into your Velocity `plugins/` folder
- Start the proxy once to generate configs

Configure:

- Edit `config.yml` (language, timezone, global limitation)
- Edit `timer.yml` (timers, showcases, actions)
- (Optional) Edit `animations.yml` and use `{animation:<id>}` in texts

Minimal example:

```yml
# timer.yml
timers:
  new-year:
    description: "New year timer."
    cron: "0 0 1 1 *"
    showcases:
      bossbar:
        start-at: 1h
        interval: 1s
        color: red
        text: "{animation:new-year-bossbar}"
    actions:
      - type: text
        shift: 0s
        options:
          text-type: title
          info:
            - "<gold>Happy New Year!</gold>"
            - "<gray>🎉</gray>"
            - "0"
            - "2"
            - "0"
```

```yml
# animations.yml
animations:
  new-year-bossbar:
    interval: 0.5
    text:
      - "<gold>Happy New Year!</gold>"
      - "<yellow>New Year in</yellow> <white>{remaining}</white>"
```

Reload:

```txt
/itimer reload
```

---

## Feedback

Please use GitHub Issues for bug reports and feature requests.  
When reporting a bug, include your Velocity version, the invertoTimer version, your configuration files, and
the relevant console logs so the issue can be reproduced.

---

## Contributing

Contributions are welcome. Fork the repository, create a feature branch, and keep changes focused and easy to review.  
When opening a Pull Request, explain what changed, why it changed, and how to test it.

If your changes affect configuration structure or i18n, please update the README, the example configs, and language
files when needed.

---

## License

This project is licensed under the MIT License. See
the [LICENSE](https://github.com/Our-Island/invertoTimer/blob/master/LICENSE) file for details.

This project uses the following third-party libraries. All dependencies are compatible with AGPL-3.0, and their licences
are respected:

- **Lombok**
  - Repository: [Mojang/brigadier](https://github.com/projectlombok/lombok)
  - License: [LICENSE](https://github.com/projectlombok/lombok/blob/master/LICENSE)

- **SnakeYaml**
  - Repository: [snakeyaml/snakeyaml](https://bitbucket.org/snakeyaml/snakeyaml/)
  - License: [Apache-2.0](https://bitbucket.org/snakeyaml/snakeyaml/src/master/LICENSE.txt)
