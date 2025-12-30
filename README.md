<div align="center">

# invertoTimer

[![Stars](https://img.shields.io/github/stars/Our-Island/invertoTimer?style=flat-square&logo=data:image/svg%2bxml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZlcnNpb249IjEiIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiI+PHBhdGggZD0iTTggLjI1YS43NS43NSAwIDAgMSAuNjczLjQxOGwxLjg4MiAzLjgxNSA0LjIxLjYxMmEuNzUuNzUgMCAwIDEgLjQxNiAxLjI3OWwtMy4wNDYgMi45Ny43MTkgNC4xOTJhLjc1MS43NTEgMCAwIDEtMS4wODguNzkxTDggMTIuMzQ3bC0zLjc2NiAxLjk4YS43NS43NSAwIDAgMS0xLjA4OC0uNzlsLjcyLTQuMTk0TC44MTggNi4zNzRhLjc1Ljc1IDAgMCAxIC40MTYtMS4yOGw0LjIxLS42MTFMNy4zMjcuNjY4QS43NS43NSAwIDAgMSA4IC4yNVoiIGZpbGw9IiNlYWM1NGYiLz48L3N2Zz4=&logoSize=auto&label=Stars&labelColor=444444&color=eac54f)](https://github.com/Our-Island/invertoTimer/)
[![GitHub CI](https://img.shields.io/github/actions/workflow/status/Our-Island/invertoTimer/ci.yml?style=flat-square&labelColor=444444&branch=master&label=GitHub%20CI&logo=github)](https://github.com/Our-Island/invertoTimer/actions/workflows/ci.yml)
[![Hangar](https://img.shields.io/badge/Hangar-invertoTimer-004ee9?style=flat-square&labelColor=444444)](https://hangar.papermc.io/Our-Island/invertoTimer)
[![Modrinth](https://img.shields.io/badge/Modrinth-invertoTimer-22ff84?style=flat-square&labelColor=444444)](https://modrinth.com/plugin/invertotimer/)

</div>

invertoTimer is a lightweight Velocity plugin for global countdowns and scheduled server-wide events.  
Perfect for events like **New Year**, **server openings**, **maintenance reminders**, and more — with flexible timers,
multiple display methods, and configurable actions.

## Introduction

invertoTimer is designed for **“whole network”** events on Velocity.  
It provides:

- **Timers** based on **cron** (repeat) or **fixed time** (one-shot)
- **Showcases** to display countdowns (actionbar / bossbar / chat / title+subtitle)
- **Actions** to trigger operations at offsets (send text, transfer players, run commands)
- **Server limitations** (global + per-timer) to exclude/include specific backend servers
- **i18n** language files and **MiniMessage** formatting for player-visible text

## Timers

Timers are configured under `timers:`. Each timer has an **id**, a **time spec**, optional **limitation**, plus *
*showcases** and **actions**.

### Time specification (choose ONE)

- `cron`: 5-field cron: `min hour day month week`
- `time`: one-shot absolute time: `yyyy-MM-dd HH:mm:ss`

Example:

```yml
timers:
  new-year:
    description: "New year timer."
    cron: "0 0 1 1 *"
    # time: "2026-01-01 00:00:00"
```

### Placeholders

Most player-visible text supports placeholders (depending on your `render()` pipeline):

- `{id}` timer id
- `{description}` timer description
- `{remaining}` formatted time
- `{days}` `{hours}` `{minutes}` `{seconds}`
- `{total_seconds}`
- `{target}` target time string

## Showcases

Showcases are **periodic displays** (countdown views) that run until the target time.

Supported types:

- `actionbar`
- `bossbar`
- `text` (chat message)
- `title` (**new mechanism**: a single `title` showcase supports both title + subtitle)

### Common fields

- `start-at`: when to start showing before the target (e.g. `10m`, `1h`, `30s`)
- `interval`: update frequency (e.g. `1s`, `30s`, `5m`)

### Text rendering features (player-visible text)

Showcase text supports:

- `{i18n:key}` tokens (replaced using language files)
- placeholders like `{remaining}`, `{id}`, etc.
- MiniMessage formatting: `<gold>`, `<red>`, `<gradient:...>`, etc.

### Title / Subtitle (new mechanism)

Instead of separate `title` + `subtitle` showcases, you now configure both under `showcases.title`:

```yml
showcases:
  title:
    start-at: 10m
    interval: 5s
    title: "<gold>New Year</gold>"
    subtitle: "<gray>{remaining}</gray>"
```

## Actions

Actions are **one-shot triggers** executed at a time offset relative to the target time.

### Common fields

- `type`: `text` | `transfer` | `command`
- `shift`: offset from target time
    - negative = before target, e.g. `-30m`
    - zero = at target, e.g. `0s`
    - positive = after target, e.g. `5s`

### Supported action types

#### 1) `text`

Send different types of text to players.

```yml
- type: text
  shift: -30m
  options:
    text-type: message    # message | actionbar | title | subtitle
    info: "<green>Ready?</green>"
```

`info` supports `{i18n:key}` + placeholders + MiniMessage.

#### 2) `transfer`

Transfer matched players to a backend server.

```yml
- type: transfer
  shift: -15m
  options:
    target: hub
    transferee: ".*"
```

- `target`: backend server name in Velocity
- `transferee`: **Java regex** for online player usernames (`matches()` full match)
    - all players: `".*"`
    - starts with `Steve_`: `"^Steve_.*$"`
    - only Alice/Bob: `"^(Alice|Bob)$"`

#### 3) `command`

Execute a configured command.

```yml
- type: command
  shift: 5s
  options:
    executor: player      # console | player
    match: ".*"      # only for executor=player
    command: "/function t"
```

- `executor: console`: runs as proxy console (ignores `match`)
- `executor: player`: runs via player backend (`spoofChatInput`) for matched players
- `command` is treated as a **plain string** — do **not** use MiniMessage here  
  (placeholders depend on your `renderString()` policy)

## Quick Start

1) **Install**

- Download jar from Hangar/Modrinth (or build from source)
- Put it into your Velocity `plugins/` folder
- Start proxy once

2) **Configure**

- Set language and timezone in `config.yml`
- Create/edit timers in `timer.yml`

Example minimal timer:

```yml
timers:
  new-year:
    description: "New year timer."
    cron: "0 0 1 1 *"
    showcases:
      actionbar:
        start-at: 1h
        interval: 1s
        text: "<yellow>New Year in</yellow> <white>{remaining}</white>"
    actions:
      - type: text
        shift: 0s
        options:
          text-type: title
          info: "<gold>Happy New Year!</gold>"
```

3) **Reload**
   Use the reload command (requires permission):

```txt
/itimer reload
```

---

## Feedback

Please use GitHub Issues for bug reports and feature requests.  
When reporting a bug, include your Velocity version, the invertoTimer version, your `config.yml` and `timer.yml`, and
the relevant console logs. This makes it much easier to reproduce and fix the problem.

## Contributing

Contributions are welcome. Fork the repository, create a feature branch, and keep changes focused and easy to review.
When opening a Pull Request, describe what changed, why the change is needed, and how to test it.

If your changes affect configuration structure or i18n text, please update the README, the example configuration files,
and the language files when necessary.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
