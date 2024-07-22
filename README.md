# AIWolfNLPServer

人狼知能大会 自然言語部門2024国際大会 で使用するサーバプログラムです。  
(This is the server program used for the AIWolf NLP Division 2024 International Tournament.)

## 設定 (Configuration)

`config/Config.ini.example` を `config/Config.ini` にリネームして設定を行ってください。  
(Rename `config/Config.ini.example` to `config/Config.ini` and configure it.)

エージェントの待ち受けIPとポートを設定してください。他の設定項目についてはデフォルトで問題ありません。  
(Set the IP and port of the agent to listen on. Other settings are fine by default.)

```
player1Ip=127.0.0.1		; エージェント1の待ち受けIP (Agent 1 listening IP)
player1Port=50000		; エージェント1の待ち受けポート (Agent 1 listening port)
player2Ip=127.0.0.1		; エージェント2の待ち受けIP (Agent 2 listening IP)
player2Port=50001		; エージェント2の待ち受けポート (Agent 2 listening port)
player3Ip=127.0.0.1		; エージェント3の待ち受けIP (Agent 3 listening IP)
player3Port=50002		; エージェント3の待ち受けポート (Agent 3 listening port)
player4Ip=127.0.0.1		; エージェント4の待ち受けIP (Agent 4 listening IP)
player4Port=50003		; エージェント4の待ち受けポート (Agent 4 listening port)
player5Ip=127.0.0.1		; エージェント5の待ち受けIP (Agent 5 listening IP)
player5Port=50004		; エージェント5の待ち受けポート (Agent 5 listening port)
```

## 実行 (Run)

OpenJDK JDK 22.0.2 を推奨します。ビルドと実行は以下のコマンドで行います。
(OpenJDK JDK 22.0.2 is recommended. Build and run with the following commands.)

```
make
```
