# AIWolfNLPServer

人狼知能コンテスト2024冬季 国内大会（自然言語部門）の動作確認のために使用する対戦接続システムです。  
前回大会（人狼知能コンテスト2024 国際大会）で使用したシステムと同一になります。  

### ❗ **前回大会（人狼知能コンテスト2024 国際大会）からの変更点**

人狼知能コンテスト2024冬季 国内大会（自然言語部門）から、新しい対戦接続システムに置き換える予定であり、現在開発中になります。  
そのため、新しいシステム公開までの間、ローカル環境でのエージェントの動作確認のために、本システムをご利用ください。  

## 設定 (Configuration)

`config/Config.ini.example` を `config/Config.ini` に名前を変更してください。  
すでに本大会向けで使用する予定のパラメータに設定済みになります。  

## 実行 (Run)

OpenJDK JDK 22.0.2 を推奨します。ビルドと実行は以下のコマンドで行います。  

```bash
make
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

`talkList` の一部を省略しています。  
(Some of `talkList` is omitted.)

```json
{"request":"ATTACK","gameInfo":{"day":2,"agent":"Agent[04]","executedAgent":"Agent[01]","latestExecutedAgent":"Agent[03]","attackedAgent":"Agent[02]","voteList":[],"latestVoteList":[],"attackVoteList":[{"day":1,"agent":"Agent[04]","target":"Agent[02]"}],"latestAttackVoteList":[],"talkList":[{"idx":17,"day":2,"turn":5,"agent":"Agent[04]","text":"Over","skip":false,"over":true}],"whisperList":[],"statusMap":{"Agent[01]":"DEAD","Agent[02]":"DEAD","Agent[03]":"DEAD","Agent[04]":"ALIVE","Agent[05]":"ALIVE"},"roleMap":{"Agent[04]":"WEREWOLF"},"remainTalkMap":{"Agent[03]":0,"Agent[04]":0,"Agent[05]":5},"remainWhisperMap":{"Agent[04]":5},"existingRoleList":["POSSESSED","SEER","VILLAGER","WEREWOLF"],"lastDeadAgentList":["Agent[02]"]}}
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
