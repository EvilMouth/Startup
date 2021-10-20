# Startup
[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![API](https://img.shields.io/badge/API-21%2B-blue.svg?style=flat)](https://developer.android.com/about/versions/android-5.0)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/evilmouth/startup)](https://github.com/EvilMouth/Startup/releases/)
[![Author](https://img.shields.io/badge/Author-EvilMouth-red.svg?style=flat)](https://www.evilmouth.net/)
![GitHub Workflow Status](https://img.shields.io/github/workflow/status/evilmouth/startup/Android%20CI?logo=github)

Android多模块任务启动方案

本方案旨在解决 **Android模块化** 之后模块之间启动依赖关系，并优化 App 启动速度

## Feature

- 支持模块化（模块各自声明自己的初始化启动任务）
- 支持并发（初始化任务并行执行，加速App启动）
- 支持阻塞启动线程（异步任务也可以阻塞App启动）
- 支持多进程（启动链区分进程）
- 编译期校验任务依赖合法性（即时纠错）
- 编译期生成启动任务执行顺序以及依赖关系
- 已配置混淆
- 同度任务支持优先级配置（1.0.0-beta05+）
- 支持配置excludeTaskList排除启动任务

## 使用以及说明

见[Wiki](https://github.com/EvilMouth/Startup/wiki)

## 辅助生成的启动任务执行顺序和依赖关系

> 本插件在 Bytex 基础上开发，生成的日志都在 Bytex 的标准日志输出位置

<img src="./img/1603941191923.jpg" width="360"></img>

```
process->main
任务排序:
g.G -> f.F -> a.A -> h.H -> b.B -> e.E -> i.I -> d.D -> c.C
同步任务分发顺序:
a.A -> h.H -> e.E -> i.I
异步任务分发顺序:
g.G -> f.F -> b.B -> d.D -> c.C
App启动时任务分发顺序:
g.G -> f.F -> b.B -> d.D -> c.C -> a.A -> h.H -> e.E -> i.I
```

<img src="./img/1603941343055.jpg"></img>

## Thanks

[Bytex](https://github.com/bytedance/ByteX)
[android-startup](https://github.com/idisfkj/android-startup)
