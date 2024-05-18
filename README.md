# DevoutServer

一款正在开发中的基于Minestom的RPG服务器核心

DevoutServer秉持**精简、稳定、可拓展**的原则，提供:
1. 完善的控制台，包含性能监控、实体管理、玩家管理等
2. 常用指令，地图编辑、传送等
3. 用于缓存/持久化实体数据的API
4. 用于缓存/持久化玩家数据(跨服支持)的API
5. RPG常用的世界实例和区块加载器，如固定地图等
6. 类似Bukkit的插件系统、依赖管理系统
7. 来自Pouvoir的解耦框架、脚本管理系统

核心只保留基础功能和插件最常用的功能，具体的职业、技能、属性系统等由插件自行实现