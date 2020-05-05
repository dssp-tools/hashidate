# hashidate

# ビルド手順
必要なソフトウェア
* maven

ビルド

    mvn install

* targetにhashidate.jarが生成されます
* hashidate.jarはローカルのmavenリポジトリに登録されます

# Windows実行ファイル(hashidate.exe)の作成
必要なソフトウェア
* launch4j

作成

    launch4j hashidate.xml

# 起動
必要なソフトウェア
* braillelib

起動

    java -jar hashidate.jar

または

    hashidate.exe

# ライセンス
[ライセンス](Lisense.txt)


# リンク
[障害学生支援プロジェクト Disabled-student Study Support Project(DSSP)](http://dssp.sakura.ne.jp/)

[braillelib](https://github.com/dssp-tools/braillelib)

[launch4j](http://launch4j.sourceforge.net/)
