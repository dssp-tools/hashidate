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
