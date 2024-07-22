# AIWolfNLPServer

人狼知能大会 自然言語部門2024国際大会 で使用するサーバプログラムです。

## 設定

`config/AIWolfGameServer.ini.example` を `config/AIWolfGameServer.ini` にリネームして設定を行ってください。

エージェントの待ち受けIPとポートを設定してください。
他の設定項目についてはデフォルトで問題ありません。

```
player1Ip=127.0.0.1		; 1人目の待ち受けIP
player1Port=50000		; 1人目の待ち受けポート
player2Ip=127.0.0.1		; 2人目の待ち受けIP
player2Port=50001		; 2人目の待ち受けポート
player3Ip=127.0.0.1		; 3人目の待ち受けIP
player3Port=50002		; 3人目の待ち受けポート
player4Ip=127.0.0.1		; 4人目の待ち受けIP
player4Port=50003		; 4人目の待ち受けポート
player5Ip=127.0.0.1		; 5人目の待ち受けIP
player5Port=50004		; 5人目の待ち受けポート
```


## 実行

OpenJDK JDK 22.0.2 を推奨します。
ビルドと実行は以下のコマンドで行います。

```
make
```