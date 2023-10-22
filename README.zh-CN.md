# ![Logo](docs/images/app-title.zh-CN.png)

智能写入是一个文本编辑器，它使用插值字符串引用外部定义的值。

## 下载

[KeenWrite.com](https://keenwrite.com/)

## 跑

在第一次运行期间，应用程序将自身解压到本地目录中。随后的启动会更快。

### Windows

双击应用程序以启动。您必须授予应用程序运行权限。 

升级时，删除以下目录:

    C:\Users\%USERNAME%\AppData\Local\warp\packages\keenwrite.exe

### Linux

执行以下命令:

``` bash
chmod +x keenwrite.bin
./keenwrite.bin
```

### MacOS

执行以下命令:

``` bash
chmod +x keenwrite.app
./keenwrite.app
```

### Java

使用Java，首先按照以下一次性设置步骤进行操作：

1. 下载Java Runtime Environment（JRE）的*完整版本*，[JRE 21](https://bell-sw.com/pages/downloads)。
   * 需要BellSoft的*完整版本*中捆绑的JavaFX。
1. 安装JRE（将JRE的`bin`目录包含在`PATH`环境变量中）。
1. 打开一个新的终端。
1. 验证安装：`java -version`
1. 下载[keenwrite.jar](https://keenwrite.com/downloads/keenwrite.jar)。
1. 下载[keenwrite.sh](https://gitlab.com/DaveJarvis/KeenWrite/-/raw/main/keenwrite.sh?inline=false)。
1. 将`.jar`和`.sh`文件放置在同一个目录中。
1. 使`keenwrite.sh`可执行：`chmod +x keenwrite.sh`

按以下方式启动应用程序：

1. 打开一个新的终端。
1. 切换到`.jar`和`.sh`目录。
1. 运行：`./keenwrite.sh`

应用程序已启动。

## 特征

* 用户定义的插值字符串
* 带变量替换的实时预览
* 基于变量值自动完成变量名
* 独立于操作系统
* 打字时拼写检查
* 使用TeX的子集编写数学公式
* 嵌入R语句

## Typesetting

排版到 PDF 文件需要以下內容:

* [Theme Pack](https://github.com/DaveJarvis/keenwrite-themes/releases/latest/download/theme-pack.zip)
* [ConTeXt](https://wiki.contextgarden.net/Installation)

## 软件使用

有關使用該應用程序的信息，請參[閱詳細文檔](docs/README.md)。

## 截图

![GraphViz Diagram Screenshot](docs/images/screenshots/01.png)

![Korean Poem Screenshot](docs/images/screenshots/02.png)

![TeX Equations Screenshot](docs/images/screenshots/03.png)


## 软件许可证

This software is licensed under the [BSD 2-Clause License](LICENSE.md) and
based on [Markdown-Writer-FX](licenses/MARKDOWN-WRITER-FX.md).

