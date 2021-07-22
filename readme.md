# WinDrop

[TOC]

## 简介

​		windows(10)与iPhone、ipad或其他含有苹果公司(Apple Inc.)`快捷指令`功能的设备之间，`共享`、`同步`、`下载文件`以及`上传文件`等功能。

​		该工具本质是一个web服务端，使用Java语言开发，依托于任务栏图标启动，界面功能较少且使用频率相对较低，故未设计UI，还请谅解。

## 功能介绍

### 设备端

- 扫描二维码
  - 连接WinDrop
    - 可选10分钟、1小时、3小时、1天、1周、1月或永久
  - 下载指定文件
- 分享
  - 分享相册图片、视频
  - 分享文本类型文件
    - txt类型的文件，以文本方式上传
      - 部分文本（如GBK编码的文本）可能无法上传，可以通过拷贝后以剪贴板方式上传
      - 使用该方式上传的文本建议不超过1MB，超过该限制其速度可能会很慢
    - json类型的文件无法通过分享方式上传（可以拷贝后以剪贴板方式上传）
  - 分享单个文件（不包括文件夹）
  - 分享文本
  - 分享剪贴板（拷贝目标后直接使用`分享`快捷指令）
    - 拷贝的文本
    - 拷贝的相册图片、视频
    - 拷贝的单个文件（包括文本类型文件）
- 同步
  - 复制的文本
  - 复制的图片
  - 复制的文件

## 安装



## 进阶