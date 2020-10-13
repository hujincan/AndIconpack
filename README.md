# AndIconpack

[app下载](https://www.coolapk.com/apk/230207)

And图标包模板是一个相对容易进行二次修改的项目，对于本模板的使用不需要您有丰富的开发经验。跟着下面的步骤将可以打造一个自己的图标包！😉我们开始吧！

😲😲 新手视频教程来了 => [Bilibili](https://www.bilibili.com/video/bv1Ka411A7Yc)

### 💡 准备：

您需要在设备上安装 [Android Studio](https://developer.android.com/studio)

下载本项目，您可以点击本页的绿色“↓Code”按钮，Download Zip，并解压到一个路径不含中文的目录

### 💻 打开本项目

首先打开安装好的Android Studio，如果你是第一次打开它，可能会让你设置一些东西，一路跳过或者继续就可以了。之后会现实欢迎界面，这时候就可以来选择界面上的`Open an existing Android Studio Project`，然后会弹出一个文件选择窗口，选中解压好的项目后点击OK，此时项目就已经加载到Android Studio中了。

不过这个时候还没有结束，应该可以看到(Android Studio简称AS)AS界面下方有个一直在动的进度条，第一次打开项目时请保持网络的通畅，它可能需要下载100-200MB的文件。而且速度不会很快。很有可能失败。如果失败请重新打开项目再试一遍。不过也有可能一直失败，这个时候最可靠的方法就是将项目目录下的`build`、 `.gradle`(注意是.gradle)、以及`app\build`目录删掉，重新再来一遍。

在下方进度条没有消失之前不要做任何对项目的修改。

如果运气好，可能你已经成功的打开项目了，这个时候您可以插上手机，打开设置的开发者人员选项，启用adb，用数据线将手机与电脑连接，然后点击AS右上方的 绿色三角形按钮，运行项目到自己手机上了

### 🚀 修改界面上基本信息

打开app/main/res/values/strings.xml这个文件，在里面你可以看到很多乱七八糟的文字。这些就是你需要修改的。

它里面大概长这样
```xml
    <!-- App 名称 -->
    <string name="app_name">And 图标包模板</string>
    <!-- 首页标题信息 -->
    <string name="home_title">And 图标包模板</string>
```

你需要修改的就是\<string name="xxxxx"> `这之间的文字` \<string/>

当你完成修改后你可以再次点击AS右上方的 绿色三角形按钮，将运行项目到自己手机上查看效果。

如果你需要修改界面上的图片信息，请到app/main/res/drawable下找到您需要更改的图片，名称只能为 小写、下划线、数字的任意组合(不能以数字开头)。如果请确保图片名称是唯一的，尽管后缀名不同也同样需要覆盖掉，将不需要的同名图片删除。

### 🥑 添加需要适配的图标

图标包适配应用的关键文件就在于 app/main/res/xml下的appfilter.xml 和 drawable.xml。

它们需要填写的内容大致是这样的：

`appfilter.xml`
```xml
<item component="ComponentInfo{com.android.chrome/com.google.android.apps.chrome.Main}" drawable="ic_chrome"/>
```

`drawable.xml`
```xml
    <category title="系统"/>
    <item drawable="ic_settings" name="设置" />
```

#### appfilter.xml

其实很简单，它们两个的内容基本都是这些，首先来看看appfilter.xml

\<item componet="" drawable=""/>这个是不变的格式

其中第一个""中填写的格式为 `需要适配软件的包名` + `/` + `需要适配软件的启动Activity名`，第二个""中填写你的`图标文件名`。

首先， `需要适配软件的包名` + `/` + `需要适配软件的启动Activity名`这个其实不需要你来弄清楚每个软件的这个信息到底是什么样的，因为每个软件都不一样，而且也没法直接看到，所以接下来打开模板app，到第三个页面，也就是适配页面随便选中一个未适配的app，点击下面的发送按钮，发送到你的电脑上，解压后里面有个txt文件，里面已经就已经有你刚才选中的app所需要的信息了。实际上这个txt文件里面就是完整的一行。里面那个drawable="软件名"需要自行修改。

再来看`图标文件名`，这个需要你的图标文件，通常是xxx.png，命名规则是小写字母开头，只允许小写字母和下划线和数字的任意组合。比如ic_chrome.png，现在将你制作的图标文件复制到app/main/res/drawable-nodpi目录里，然后drawable="软件名"替换为drawable="图标文件名"，注意不需要后缀名。

#### drawable.xml

这个文件是必须有的，尽管可能没有它也能使用，不过这个文件的主要目的是为了某些桌面可以自由替换某个app的图标，这个时候就会读取drawable.xml里的配置了。它的固定格式为：

```xml
    <category title="类别"/>
    <item drawable="图标文件名" name="APP名" />
    <item drawable="图标文件名" name="APP名" />
    <item drawable="图标文件名" name="APP名" />
    ...
```

首先是一个`<categor title="类别"/>`，这是为了给图标分类，标题随意取，比如`系统`、`游戏`...这个标签写完后就可以在它的下方开始写`<item drawable="图标文件名" name="APP名" />`，`图标文件名`和appfilter一样的做法，`APP名`是对应APP的中文名，其实是可以不写的，但是如果你想写，它可以在模板app内部点击图标后展示name的值，否则默认展示图标文件名。如果不想写可以直接删掉`name="APP名"`，就像这样：

```xml
    <category title="类别"/>
    <item drawable="图标文件名" />
    <item drawable="图标文件名" />
    <item drawable="图标文件名" />
```

以后每一个适配一个新app都需要这样做，当你一个做好一个版本后，别忘了复制appfilter.xml和drawable.xml文件到app/src/main/assets目录下，因为某些桌面使用的是这个目录，不是xml这个目录。

### 😍 打包apk以及更新

一切都完成后，终于可以打包了。先确保在app/build.gradle文件中修改了包名，并且点击了Sync Now，每次修改build.gradle内容都需要点击Sync Now。

你还可以在build.gradle中看到

```groovy
versionCode 1
versionName "1.0"
```

这是版本号以及版本名，每次更新版本时确保并versionCode后的数字比之前大，版本名随意填写，这是你的第一个版本，所以就这样写就好。

接下来利用Android Studio将项目编译为apk，首先点击上方的 工具栏上方的菜单栏中的Build，它会弹出很多选项，如果你需要发布 Release版，选择`Generate Signed Bundle / APK`，之后它会让你选择编译成Bundle还是apk，随你咯。我就选apk了，接下来则需要签名文件，至于怎么创建签名文件，可以看看网上的教程，这里就不介绍了。也是在AS里这个界面就可以创建，时间最好久一点，不然过了签名时间apk就安装不上了。假设你已经得到了签名文件，这个时候选择你的签名文件，然后输入密码，名称，密码。点击next，然后选择myIconRelease，上方是输出apk的目录，可以记下来或者自己选一个目录，点击finish，窗口关闭。然后你又看到AS下方又出现了进度条，等待进度条消失，会在右下角提示的，并且有个蓝色文字可以点击到输出目录，现在你得到了一个apk。注意这个时候签名已经是你自己的了，如果你之前在你的手机上运行过，是不能覆盖安装的，需要先卸载你之前测试的图标包。

### 😜 软件截图

<img src="https://raw.githubusercontent.com/hujincan/AndIconpack/master/Screenshot/Screenshot_20200429-102200.jpg"/>

<img src="https://raw.githubusercontent.com/hujincan/AndIconpack/master/Screenshot/Screenshot_20200429-102803.jpg"/>

🤫 如果你是一名专业的开发者，看得出来代码写的很烂，千万别说出来！
