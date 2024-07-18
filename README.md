# Kookie

基于 `Mirai` 框架的群聊机器人，还在开发中...

kookie的诞生起源于写代码时的无聊，就像写一个玩一下，并且在新建文件夹的时候恰巧正在吃饼干，遂诞生了Kookie，希望各位喜欢她。

本次开源一部分插件功能。

欢迎各位PR，欢迎一起交流。

## Features

- `答案之书` 群聊内发送 `答案之书 <询问内容>` 
- `今日运势` 群聊内发送 `今日运势`  
- `评价一下` 群聊内发送 `评价一下 <@想要评价的目标>`
- `吃什么` 群聊内发送 `吃什么 <具体地址> <city>` （如果地址非常具体可以不加`city`）
- `说一下` 群聊内发送 `说一下 <需要说的文字>`，会发送语音，语音模块需要自己部署推理服务器。
- `今日老婆` 群聊内发送 `今日老婆` 
- `代码运行` 群聊内发送 `代码运行 <语言> <代码>` 此部分用的runoob的API，自己去拿一个token即可使用。
- `今日词云` 群聊内发送 `今日词云`
- `昨日词云` 群聊内发送 `昨日词云`
- `地狱笑话` 群聊内发送 `地狱笑话` 目前是英文的，后面考虑能不能翻译，但是其实很多笑话是欧美梗，如果翻译过来的话，可能就没内味了。

后面的功能还在想，语音功能部分比较的复杂，需要另外自己部署推理服务器，并且自己使用模型，而且还需要自己编译ffmpeg，这部分代码写的比较死，这部分需要一个详细的文档，但是本人学生空余时间并不是很多，所以后续会不定期开发。

## 需要填写的API

### 百度

大模型采用的是百度的 `Yi-34B-Chat` 的中文模型，选择这个的理由是首先中文识别以及短回复比较适合群聊场景，其次是调用它不要钱（bushi，对学生党比较友好。

去 [百度智能云控制台](https://console.bce.baidu.com/) 新建一个应用，获取 `API key` 和 `Secret Key` 填入 `config.json` 即可

### 高德地图

主要是吃什么功能，需要用到高德的周边搜索以及地理坐标转换。\

去 [高德开放平台](https://lbs.amap.com/) 建立一个应用，然后复制 `API Key` 填入即可，注意周边搜索免费额度只能每天免费100次数，所以建议节省一些，后续会开发一个缓存池来延长每天的时间，咕咕。

### 语音功能

这部分需要自己搭建推理服务器并且还涉及本地的ffmpeg的编译，比较的麻烦，所以因为时间限制暂且不表。

### Runoob

需要去菜鸟教程的在线编译器抓包拿一下token，这里感谢菜鸟教程的无私奉献（）

## Config

在kookie插件加载成功后会在 `./kookie/` 目录下生成 `config.json` 文件，需要手动填写一些相关参数：

```json
{
  "botInfo": {
    "birthday": "2024-05-20",
    "name": "Kookie",
    "owner": "your owner",
    "age": 16,
    "baiduApiConfig": {
      "apiKey": "your apiKey",
      "secretKey": "your secretKey"
    },
    "gaodeApiConfig": {
      "apiKey": "your apiKey"
    },
    "voiceApiConfig": {
      "apiUrl": "your apiUrl",
      "ref_audio_path": "your ref_audio_path",
      "gpt_weights_path": "your gpt_weights_path",
      "sovits_weights_path": "your sovits_weights_path"
    },
    "runoobToken": "your runoobToken"
  },
  "maxTodayGirlTimes": 3,
  "adminList": [
    123456789,
    987654321
  ],
  "userBlackList": [
    11111111,
    22222222
  ],
  "enableGroupList": [
    {
      "id": 666666,
      "tag": [
        "test1",
        "test2"
      ]
    },
    {
      "id": 777777,
      "tag": [
        "test3",
        "test4"
      ]
    }
  ]
}
```

`voiceApiConfig` 目前可以先不填写，在后续文档跟进后再使用，这部分只是关于语音推理的。

然后黑名单和允许的群聊这两部分是权限管理必须需要的，所以一定要填写！

字体下载请到 [fonts](https://github.com/GeneralK1ng/Kookies/blob/v0.1.8/src/main/resources/fonts) 这个文件夹下下载然后安装，目前还在研究java的字体安装，如果无法自动安装就手动下载安装吧。