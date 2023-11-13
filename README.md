# hashidate

## 橋立とは

橋立はGUIで点図を作成するためのソフトウェアです。

## ビルド手順
必要なソフトウェア
* maven

ビルド

    mvn package

* targetにhashidate.jarが生成されます
* targetに下記のファイルがコピーされます<br>
config.xml,braille.xml,equation.xml,mmlalias.xml,Lisense.txt
* target/libに依存するライブラリがコピーされます

## Windows実行ファイル(hashidate.exe)の作成
必要なソフトウェア
* launch4j

作成

    launch4j hashidate.xml

## Eclipseを使う場合

#### プロジェクトのインポート
1. ワークスペースにGitクローンする。
1. 「ファイル」メニュー→「インポート」→「Maven既存プロジェクト」を選択する。
1. 「ルートディレクトリ」にhashidateフォルダのパスを入力して、「完了」をクリックする。


#### ビルド
1. パッケージエクスプローラーでhashidateを選択して右クリックする。
1. 「実行」→「Mavenビルド」で、「ゴール」に「package」を入力する。
<br/>2回目以降は「実行」→「hashidate(Mevenビルド）」
1. 「実行」ボタンをクリックする。

#### Windows実行ファイル(hashidate.exe)の作成
必要なソフトウェア
* launch4j

1. 「実行」メニュー→「外部ツールの構成」を選択する
1. 「プログラム」を右クリックして「新規構成」を選択する。
1. 「ロケーション」の「ファイル・システムの参照」をクリックして、launch4j.exeのパスを入力する。
1. 「実行」ボタンをクリックする。
<br/> 2回目以降は「実行」メニュー→「外部ツールの構成」→登録した構成
1. フォルダーアイコンの「Open configuration or import」ボタンをクリックする。
1. hashidateフォルダのhashidate.xmlを選択する。
1. 歯車アイコンの「Build wrapper：ボタンをクリックする。


## 起動
必要なソフトウェア
* braillelib

起動

    java -jar hashidate.jar

または

    hashidate.exe

## ライセンス
[ライセンス](Lisense.txt)


## リンク
[障害学生支援プロジェクト Disabled-student Study Support Project(DSSP)](http://dssp.sakura.ne.jp/)

[braillelib](https://github.com/dssp-tools/braillelib)

[launch4j](http://launch4j.sourceforge.net/)
