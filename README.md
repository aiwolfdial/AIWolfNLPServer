# AIWolfNLPServer

人狼知能大会 自然言語部門2024国際大会 で使用するサーバプログラムです。  
(This is the server program used for the AIWolf NLP Division 2024 International Tournament.)

### ❗ **前回大会からの変更点**  (❗ **Changes from the previous tournament**)

- アクションタイムアウトを追加しました。  
  (Added action timeout.)  
  `GameSetting` に `actionTimeout` が追加され、エージェントがアクションを行う時間の制限をサーバ側からエージェントに通知します。単位はミリ秒です。
   (`actionTimeout` has been added to `GameSetting`, and the server notifies the agent of the time limit for agents to perform actions. The unit is milliseconds.)
- エージェントの指定方法を変更しました。  
  (Changed the way to specify agents.)  
  エージェントの番号のみを指定する方法から `Agent[%02d]` (例: Agent[01]) の形式で指定する方法に変更しました。  
  (Changed from specifying only the agent number to specifying in the format `Agent[%02d]` (e.g. Agent[01]).)

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

実行ログ (対戦ログではない) をファイルに出力しない場合は以下の設定を行ってください。  
(If you do not want to output the execution log (not the battle log) to a file, please make the following settings.)

`config/log4j2.xml` の以下の行をコメントアウトしてください。  
(Comment out the following line in `config/log4j2.xml`.)

```
<AppenderRef ref="File" />
```

```
<!-- <AppenderRef ref="File" /> -->
```

また、実行ログ内に通信パケットをリアルタイムに表示しない場合は以下の設定を行ってください。
(If you do not want to display communication packets in real time in the execution log, please make the following settings.)

`config/log4j2.xml` の以下の行の `level` を `debug` に変更してください。
(Change the `level` of the following line in `config/log4j2.xml` to `debug`.)

```
<Root level="trace">
```

```
<Root level="debug">
```

## 実行 (Run)

OpenJDK JDK 22.0.2 を推奨します。ビルドと実行は以下のコマンドで行います。  
(OpenJDK JDK 22.0.2 is recommended. Build and run with the following commands.)

```
make
```

実行のみ行う場合は以下のコマンドを実行してください。  
(If you want to run only, please run the following command.)

```
make run
```