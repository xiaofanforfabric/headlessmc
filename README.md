<!--suppress HtmlDeprecatedAttribute -->
<div align="right">
  中文 | <a href="https://github.com/headlesshq/headlessmc">English</a>
</div>

<h1 align="center" style="font-weight: normal;"><b>HeadlessMc</b></h1>
<p align="center">Minecraft Java 版的命令行启动器。</p>
<p align="center"><img src="logo.svg" alt="logo" style="width:250px;"></p>
<p align="center"><a href="https://github.com/headlesshq/mc-runtime-test">Mc-Runtime-Test</a> | HMC | <a href="https://github.com/headlesshq/hmc-specifics">HMC-Specifics</a> | <a href="https://github.com/headlesshq/hmc-optimizations">HMC-Optimizations</a></p>

<div align="center">

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/8ac39920ba834d1991c512b83931554a)](https://app.codacy.com/gh/headlesshq/headlessmc/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![GitHub All Releases](https://img.shields.io/github/downloads/headlesshq/headlessmc/total.svg)](https://github.com/headlesshq/headlessmc/releases)
![](https://github.com/headlesshq/HeadlessMc/actions/workflows/gradle-publish.yml/badge.svg)
![GitHub](https://img.shields.io/github/license/headlesshq/HeadlessMc)
[![Docker Image Size](https://img.shields.io/docker/image-size/3arthqu4ke/headlessmc)](https://hub.docker.com/r/3arthqu4ke/headlessmc/)
![Github last-commit](https://img.shields.io/github/last-commit/headlesshq/headlessmc)

</div>

> [!WARNING]
> 本产品并非官方 Minecraft 产品，未经 Mojang 或 Microsoft 批准，也与其无关。
>
> HeadlessMc 不允许你在未购买 Minecraft 的情况下游玩！
> 账号始终会被验证。
> 离线账号只能用于在 CI/CD 流水线中以无头模式运行游戏。

HeadlessMc 是一个功能丰富的 Minecraft Java 版启动器，运行在你的终端中。
它可以让 Minecraft 客户端以无头模式运行，例如让你在 CI/CD 运行器上运行游戏。
此外，它还支持各种模组加载器、整合包、模组、资源包、光影包、数据包……
甚至可以用 HeadlessMc 管理服务器。

下载最新发行版并访问[快速上手](https://headlesshq.github.io/headlessmc/getting-started/)
页面开始使用，只需两步：
```shell
> headlessmc login
> headlessmc launch fabric 26.3
```
