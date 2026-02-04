<div align="center">

# invertoTimer

[![Visitors](https://api.visitorbadge.io/api/visitors?path=https%3A%2F%2Fgithub.com%2FOur-Island%2FinvertoTimer&labelColor=%23444444&countColor=%23f24822&style=flat-square&labelStyle=none)](https://visitorbadge.io/status?path=https://github.com/Our-Island/invertoTimer/)
[![Stars](https://img.shields.io/github/stars/Our-Island/invertoTimer?style=flat-square&logo=data:image/svg%2bxml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZlcnNpb249IjEiIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiI+PHBhdGggZD0iTTggLjI1YS43NS43NSAwIDAgMSAuNjczLjQxOGwxLjg4MiAzLjgxNSA0LjIxLjYxMmEuNzUuNzUgMCAwIDEgLjQxNiAxLjI3OWwtMy4wNDYgMi45Ny43MTkgNC4xOTJhLjc1MS43NTEgMCAwIDEtMS4wODguNzkxTDggMTIuMzQ3bC0zLjc2NiAxLjk4YS43NS43NSAwIDAgMS0xLjA4OC0uNzlsLjcyLTQuMTk0TC44MTggNi4zNzRhLjc1Ljc1IDAgMCAxIC40MTYtMS4yOGw0LjIxLS42MTFMNy4zMjcuNjY4QS43NS43NSAwIDAgMSA4IC4yNVoiIGZpbGw9IiNlYWM1NGYiLz48L3N2Zz4=&logoSize=auto&label=Stars&labelColor=444444&color=eac54f)](https://github.com/Our-Island/invertoTimer/)
[![GitHub CI](https://img.shields.io/github/actions/workflow/status/Our-Island/invertoTimer/ci.yml?style=flat-square&labelColor=444444&branch=master&label=GitHub%20CI&logo=github)](https://github.com/Our-Island/invertoTimer/actions/workflows/ci.yml)
[![Hangar](https://img.shields.io/badge/Hangar-invertoTimer-004ee9?style=flat-square&labelColor=444444)](https://hangar.papermc.io/Our-Island/invertoTimer)
[![Modrinth](https://img.shields.io/badge/Modrinth-invertoTimer-22ff84?style=flat-square&labelColor=444444)](https://modrinth.com/plugin/invertotimer/)

</div>

invertoTimer is a lightweight Velocity plugin for global countdowns and scheduled server-wide events.  
It’s designed for “whole network” moments such as New Year, grand openings, maintenance reminders, and other
timed announcements — with configurable timers, multiple display modes, flexible actions, and text animations.

## Introduction

invertoTimer provides:

- Timers based on **cron** (repeat) or **fixed time** (one-shot)
- **Showcases** to display countdowns (actionbar / bossbar / chat / title)
- **Actions** to trigger operations at offsets (send text, transfer players, run commands)
- Server limitations (**global + per-timer**) to include/exclude specific backend servers
- i18n language files and **MiniMessage** formatting for player-visible text
- A variety of placeholder and MiniPlaceholders support

The plugin uses three config files:

- `config.yml`: global settings (language, timezone, global limitation, etc.)
- `timer.yml`: timer definitions (timers, showcases, actions)
- `animations.yml`: animation definitions used by `{animation:<id>}`

## Documentation

You can read the documentation at the [wiki page](https://github.com/Our-Island/invertoTimer/wiki/), introducing you
basic concepts and usages of the plugin.

## Feedback

Please use GitHub Issues for bug reports and feature requests.  
When reporting a bug, include your Velocity version, the invertoTimer version, your configuration files, and
the relevant console logs so the issue can be reproduced.

## Contributing

Contributions are welcome. Fork the repository, create a feature branch, and keep changes focused and easy to review.  
When opening a Pull Request, explain what changed, why it changed, and how to test it.

If your changes affect configuration structure or i18n, please update the README, the example configs, and language
files when needed.

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
