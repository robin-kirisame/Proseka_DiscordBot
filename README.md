## 環境構築
任意のバージョンのJDKをインストール  
https://www.oracle.com/java/technologies/downloads/

Gradleをインストール  
https://docs.gradle.org/current/userguide/installation.html
1. Gradleの最新版をダウンロード
1. `C:\Gralde\gradle-x.y.z\`というディレクトリに展開する
1. PATH環境変数に`C:\Gralde\gradle-x.y.z\bin`を追加する

## 開発方法
### 前準備  
`app\src\main\resources`ディレクトリに`bot_token.txt`というファイルを用意し、同テキストファイルにDiscord BotのTokenを入れておく。

### ビルド方法
```
gradle build
```
### 実行方法  
```
gradle :app:run
```