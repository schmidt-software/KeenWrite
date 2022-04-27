# ![Logo](docs/images/app-title.zh-CN.png)

智能写入是一个文本编辑器，它使用插值字符串引用外部定义的值。

## 下载

下载以下版本之一:

* [Windows](https://github.com/DaveJarvis/keenwrite/releases/latest/download/keenwrite.exe)
* [Linux](https://github.com/DaveJarvis/keenwrite/releases/latest/download/keenwrite.bin)
* [Java Archive](https://github.com/DaveJarvis/keenwrite/releases/latest/download/keenwrite.jar)

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

### Other

Download and install a full version of [OpenJDK 15](https://bell-sw.com/pages/downloads/?version=java-15#mn) that includes JavaFX module support, then run:

``` bash
java -jar keenwrite.jar
```

## 特征

* 用户定义的插值字符串
* 带变量替换的实时预览
* 基于变量值自动完成变量名
* 独立于操作系统
* 打字时拼写检查
* 使用TeX的子集编写数学公式
* 嵌入R语句

## 软件使用

See the [detailed documentation](docs/README.md) for information about
using the application.

## 截图

![GraphViz Diagram Screenshot](docs/images/screenshots/01.png)

![Korean Poem Screenshot](docs/images/screenshots/02.png)

![TeX Equations Screenshot](docs/images/screenshots/03.png)


## 软件许可证

This software is licensed under the [BSD 2-Clause License](LICENSE.md) and
based on [Markdown-Writer-FX](licenses/MARKDOWN-WRITER-FX.md).

