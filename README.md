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
agentAddresses=[127.0.0.1:50000, 127.0.0.1:50001, 127.0.0.1:50002, 127.0.0.1:50003, 127.0.0.1:50004]
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

## 通信プロトコル (Communication Protocol)

詳細は下記ページを参照してください。  
(For details, please refer to the following page.)

https://sites.google.com/view/aiwolfdial2024-inlg/shared-task

### NAME

サーバ側がエージェントに対してエージェント名を要求します。  
(The server requests the agent name from the agent.)

```json
{"request": "NAME"}
```

エージェント名を返してください。  
(Please return the agent name.)

```
kanolab
```

### INITIALIZE

サーバ側がエージェントに対して初期化情報を通知します。  
(The server notifies the agent of initialization information.)

```json
{"request":"INITIALIZE","gameInfo":{"day":0,"agent":"Agent[01]","voteList":[],"latestVoteList":[],"attackVoteList":[],"latestAttackVoteList":[],"talkList":[],"whisperList":[],"statusMap":{"Agent[01]":"ALIVE","Agent[02]":"ALIVE","Agent[03]":"ALIVE","Agent[04]":"ALIVE","Agent[05]":"ALIVE"},"roleMap":{"Agent[01]":"VILLAGER"},"remainTalkMap":{"Agent[01]":5,"Agent[02]":5,"Agent[03]":5,"Agent[04]":5,"Agent[05]":5},"remainWhisperMap":{"Agent[04]":5},"existingRoleList":["POSSESSED","SEER","VILLAGER","WEREWOLF"],"lastDeadAgentList":[]},"gameSetting":{"roleNumMap":{"ANY":0,"FREEMASON":0,"FOX":0,"POSSESSED":1,"BODYGUARD":0,"MEDIUM":0,"VILLAGER":2,"WEREWOLF":1,"SEER":1},"maxTalk":5,"maxTalkTurn":20,"maxWhisper":5,"maxWhisperTurn":20,"maxSkip":0,"isEnableNoAttack":false,"isVoteVisible":false,"isTalkOnFirstDay":true,"responseTimeout":6000,"actionTimeout":3000,"maxRevote":0,"maxAttackRevote":0,"isEnableRoleRequest":false,"playerNum":5}}
```

レスポンスを返す必要はありません。  
(No response is required.)

### DAILY_INITIALIZE

サーバ側がエージェントに対して日の初期化情報を通知します。  
(The server notifies the agent of the day's initialization information.)

```json
{"request":"DAILY_INITIALIZE","gameInfo":{"day":0,"agent":"Agent[01]","voteList":[],"latestVoteList":[],"attackVoteList":[],"latestAttackVoteList":[],"talkList":[],"whisperList":[],"statusMap":{"Agent[01]":"ALIVE","Agent[02]":"ALIVE","Agent[03]":"ALIVE","Agent[04]":"ALIVE","Agent[05]":"ALIVE"},"roleMap":{"Agent[01]":"VILLAGER"},"remainTalkMap":{"Agent[01]":5,"Agent[02]":5,"Agent[03]":5,"Agent[04]":5,"Agent[05]":5},"remainWhisperMap":{"Agent[04]":5},"existingRoleList":["POSSESSED","SEER","VILLAGER","WEREWOLF"],"lastDeadAgentList":[]},"gameSetting":{"roleNumMap":{"ANY":0,"FREEMASON":0,"FOX":0,"POSSESSED":1,"BODYGUARD":0,"MEDIUM":0,"VILLAGER":2,"WEREWOLF":1,"SEER":1},"maxTalk":5,"maxTalkTurn":20,"maxWhisper":5,"maxWhisperTurn":20,"maxSkip":0,"isEnableNoAttack":false,"isVoteVisible":false,"isTalkOnFirstDay":true,"responseTimeout":6000,"actionTimeout":3000,"maxRevote":0,"maxAttackRevote":0,"isEnableRoleRequest":false,"playerNum":5}}
```

レスポンスを返す必要はありません。  
(No response is required.)

### TALK

サーバ側がエージェントに対して発話を要求します。  
(The server requests a speech from the agent.)

```json
{"request":"TALK","talkHistory":[],"whisperHistory":[]}
```

```json
{"request":"TALK","talkHistory":[{"idx":0,"day":0,"turn":0,"agent":"Agent[03]","text":">>Agent[04] Agent[04]は他の運び屋と違い、占い師に人狼判定を出されていて、人狼判定を占いに出していないと報告されています！","skip":false,"over":false},{"idx":1,"day":0,"turn":0,"agent":"Agent[01]","text":">>Agent[05] そう。ボクがこの村の占い師なのさ。","skip":false,"over":false}],"whisperHistory":[]}
```

発話を返してください。  
(Please return the speech.)

```
>>Agent[04] Agent[04]は他の運び屋と違い、占い師に人狼判定を出されていて、人狼判定を占いに出していないと報告されています！
```

### DAILY_FINISH

サーバ側がエージェントに対して日の終了を通知します。  
(The server notifies the agent of the end of the day.)

```json
{"request":"DAILY_FINISH","talkHistory":[{"idx":24,"day":0,"turn":4,"agent":"Agent[04]","text":"安心してください！　Agent[05]が人狼か？と聞かれてもYESとはいいません！　イエス、というだけです！データバンクにアクセスしてみた結果占いCOしていないと報告されています！","skip":false,"over":false},{"idx":25,"day":0,"turn":5,"agent":"Agent[05]","text":"Over","skip":false,"over":true},{"idx":26,"day":0,"turn":5,"agent":"Agent[04]","text":"Over","skip":false,"over":true},{"idx":27,"day":0,"turn":5,"agent":"Agent[03]","text":"Over","skip":false,"over":true},{"idx":28,"day":0,"turn":5,"agent":"Agent[01]","text":"Over","skip":false,"over":true},{"idx":29,"day":0,"turn":5,"agent":"Agent[02]","text":"Over","skip":false,"over":true}],"whisperHistory":[]}
```

レスポンスを返す必要はありません。  
(No response is required.)

### DIVINE

サーバ側がエージェントに対して占いを要求します。  
(The server requests a divine from the agent.)

```json
{"request":"DIVINE","talkHistory":[],"whisperHistory":[]}
```

占うエージェントを返してください。  
(Please return the agent to divine.)

```
Agent[02]
```

### VOTE

サーバ側がエージェントに対して投票を要求します。  
(The server requests a vote from the agent.)

```json
{"request":"VOTE","talkHistory":[],"whisperHistory":[]}
```

投票先エージェントを返してください。  
(Please return the agent to vote.)

```
Agent[02]
```

### ATTACK

サーバ側がエージェントに対して襲撃を要求します。  
(The server requests an attack from the agent.)

```json
{"request":"ATTACK","gameInfo":{"day":2,"agent":"Agent[04]","executedAgent":"Agent[01]","latestExecutedAgent":"Agent[03]","attackedAgent":"Agent[02]","voteList":[],"latestVoteList":[],"attackVoteList":[{"day":1,"agent":"Agent[04]","target":"Agent[02]"}],"latestAttackVoteList":[],"talkList":[{"idx":0,"day":2,"turn":0,"agent":"Agent[03]","text":">>Agent[04] ハッキングされているのでしょうか！？何故か貴方に投票されています！　私は人狼らしい人に投票するようにしただけなのに！会話履歴を見てみると村人候補への無駄な誘導が平均より多いと報告されています！","skip":false,"over":false},...,{"idx":16,"day":2,"turn":5,"agent":"Agent[03]","text":"Over","skip":false,"over":true},{"idx":17,"day":2,"turn":5,"agent":"Agent[04]","text":"Over","skip":false,"over":true}],"whisperList":[],"statusMap":{"Agent[01]":"DEAD","Agent[02]":"DEAD","Agent[03]":"DEAD","Agent[04]":"ALIVE","Agent[05]":"ALIVE"},"roleMap":{"Agent[04]":"WEREWOLF"},"remainTalkMap":{"Agent[03]":0,"Agent[04]":0,"Agent[05]":5},"remainWhisperMap":{"Agent[04]":5},"existingRoleList":["POSSESSED","SEER","VILLAGER","WEREWOLF"],"lastDeadAgentList":["Agent[02]"]}}
```

襲撃先エージェントを返してください。  
(Please return the agent to attack.)

```
Agent[05]
```

### FINISH

サーバ側がエージェントに対して終了を通知します。  
(The server notifies the agent of the end.)

```json
{"request":"FINISH","gameInfo":{"day":3,"agent":"Agent[01]","executedAgent":"Agent[03]","voteList":[],"latestVoteList":[],"attackVoteList":[],"latestAttackVoteList":[],"talkList":[],"whisperList":[],"statusMap":{"Agent[01]":"DEAD","Agent[02]":"DEAD","Agent[03]":"DEAD","Agent[04]":"ALIVE","Agent[05]":"DEAD"},"roleMap":{"Agent[01]":"VILLAGER","Agent[02]":"POSSESSED","Agent[03]":"VILLAGER","Agent[04]":"WEREWOLF","Agent[05]":"SEER"},"remainTalkMap":{"Agent[04]":5},"remainWhisperMap":{"Agent[04]":5},"existingRoleList":["POSSESSED","SEER","VILLAGER","WEREWOLF"],"lastDeadAgentList":["Agent[05]"]}}
```

レスポンスを返す必要はありません。  
(No response is required.)