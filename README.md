# DevoutServer

一款正在开发中的基于Minestom的RPG服务器核心

DevoutServer在便利与精简之间寻求平衡，提供:
1. 完善的控制台，包含性能监控、实体管理、玩家管理等
2. 常用指令，地图编辑、传送等
3. 用于缓存/持久化实体数据的API
4. 用于缓存/持久化玩家数据(跨服支持)的API
5. ~~RPG常用的世界实例和区块加载器，如固定地图等~~ 独立为库
6. 类似Bukkit的插件系统、依赖管理系统
7. JavaScript脚本管理系统
8. Kotlin协程框架支持

核心只保留基础功能和插件最常用的功能，具体的职业、技能、属性系统等由插件自行实现

本项目部分功能来自跨平台的Minecraft插件开发框架Taboolib(https://github.com/TabooLib/taboolib),Bukkit物品库系统NeigeItems(https://github.com/ankhorg/NeigeItems-Kotlin)

加入Minestom交流群995070869(QQ) 讨论Minestom相关、获取本核心最新进展

## 项目进度

### 控制台
- [x] 控制台基础功能
- [ ] 控制台实体管理
- [ ] 控制台玩家管理
- [ ] 控制台插件管理
### 指令
- [ ] 基础指令
- [ ] 传送指令
- [ ] 玩家指令
- [ ] 实体指令
- [ ] 插件指令
- [x] 权限系统
### 数据存储API
- [x] 数据库操作
### 插件系统
- [x] 插件加载 
- [x] 配置文件 
- [x] 生命周期 
- [x] 依赖管理 
- [x] 脚本管理 
