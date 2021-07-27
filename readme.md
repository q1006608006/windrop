# WinDrop

[TOC]

## 简介

​		windows(10)与iPhone、ipad或其他含有苹果公司(Apple Inc.)`快捷指令`功能的设备之间，`共享`、`同步`、`下载文件`以及`上传文件`等功能。

​		该工具本质是一个web服务端，使用Java语言开发，服务端（PC）依托于任务栏小图标进行交互，设备端仅使用快捷指令交互即可。

## 功能介绍

### 设备端

> 未连接过的设备无法使用`分享`、`同步`以及`上传`功能

- 扫描二维码
  - 连接WinDrop及设备认证
  - 下载指定文件
- 分享
  - 分享相册图片、视频
  - 分享文本类型文件
    - txt类型的文件，以文本方式上传
      - 部分文本（如GBK编码的文本文件）可能无法上传，可以通过拷贝后以剪贴板方式上传
      - 使用该方式上传的文本建议不超过1MB，超过该限制其速度可能会很慢
    - json类型的文件无法通过分享方式上传（可以拷贝后以剪贴板方式上传）
  - 分享单个文件（不包括文件夹）
  - 分享文本
  - 分享剪贴板（拷贝目标后直接使用`分享`快捷指令）
    - 拷贝的文本
    - 拷贝的相册图片、视频
    - 拷贝的单个文件（包括文本类型文件）
- 同步
  - 电脑上复制的文本到剪贴板
  - 电脑上复制的图片到剪贴板
    - 非文件保存的图片可能无法保证精度
  - 电脑上复制的文件
    - 默认拷贝至剪贴板
    - 可以选择是否保存到文件
- 上传
  - 照片图库
  - 拍照或录像
  - 普通文件

### Windows端

> 所有操作均通过右键单击右下角应用图标唤出的菜单完成

- 共享文件

  >  选择确定共享文件后，设备端使用`扫描二维码`功能扫描后即可操作下载指定文件

- `启动`、`关闭`、`重启`服务，`退出`应用

- 连接码

  > 设备端使用`扫描二维码`功能扫描后即可操作绑定设备

  - 可选10分钟、1小时、3小时、1天、1周、1月及永久

- 打开配置文件

  - 示例

  ```properties
  #------------------------------------------------------------------------
  # 服务端口
  windrop.port=8898
  
  #------------------------------------------------------------------------
  # 是否在通知栏通知，格式: [${operator}.][${type}]
  # 使用","(逗号，半角)隔开
  # operator: 可选pull(同步)、push(分享)
  # type: 可选"text"(文本)、"image"(图片)、"file"(文件)以及"*"(全部)
  # 不填或注释该项默认不通知
  windrop.notify=*,push.*,pull.file
  
  #------------------------------------------------------------------------
  # 操作是否需要弹窗确认后才可进行，格式: 同"windrop.notify"
  # 不填或注释该项默认需要确认
  windrop.confirm=file,push.image
  
  #------------------------------------------------------------------------
  # 超过该长度限制的文本内容会自动保存为txt文件
  windrop.textFileLimit=2021
  
  #------------------------------------------------------------------------
  # 直传文件最大大小
  windrop.maxFileLength=29360128
  
  #------------------------------------------------------------------------
  # 中文文本编码
  windrop.encoding=utf-8
  ```

- 打开白名单文件

  > 空白默认所有IP地址都可以访问

  - 每行可输入一个IP或IP段
    - 格式：`${起始IP地址}`[**>**`${偏移位数}`]
    - 案例
      - "192.168.0.2>254"： 表示运行[192.168.0.2 ~ 192.168.0.255]的IP段访问
      - "172.16.0.1"：表示只允许[172.16.0.1]的IP地址访问

- 清空设备

  > 清空所有连接

- 查看日志

  > 打开日志所在文件夹

- 已收文件

  > 打开设备上传的文件所在文件夹

## 安装

### *JDK*

> 此处不提供下载链接及安装教程，请下载[Oracle](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html)或其他主流JDK，版本不低于JDK1.8（高于JDK16无法保证兼容性）

### 下载Windrop

* [Release](https://github.com/q1006608006/windrop/releases/download/v1.0.0/windrop-v1.0.0.tar.gz)

  > 下载解压后进入文件夹，双击run.bat文件运行，右键单击右下角（任务栏）图标即可选择使用功能。

  ![image](https://user-images.githubusercontent.com/31004882/126967866-cd1e1f94-4bbf-4dac-9b6a-4ce67ef37b37.png)

  ![image](https://user-images.githubusercontent.com/31004882/126968443-aa7141ba-9db7-4d7e-9125-ae73a25034e8.png)

* 快捷指令

  1. 共享

     ![共享](https://user-images.githubusercontent.com/31004882/126964422-97d062e2-06c3-455e-be4a-5528beb24fdf.png)
     
  2. 同步
  
     ![同步](https://user-images.githubusercontent.com/31004882/126964579-b0a8bc88-7c6a-4ded-82f7-f1af7489cf64.png)
  
  3. 扫描二维码

     ![扫描二维码](https://user-images.githubusercontent.com/31004882/126964686-be6f8087-fc5e-4734-9900-f0207d56e6fd.png)

  4. 上传

     ![上传](https://user-images.githubusercontent.com/31004882/126964770-9de705d9-81fa-4f34-b3b1-6f9add2da4aa.png)


## 进阶
待续。。。。。。