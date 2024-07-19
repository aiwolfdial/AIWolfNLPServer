# AIWolfNLPServer
人狼知能本戦で運営から参加者に接続をする形にに対応したプログラムです。


## 設定
`res/AIWolfGameServer.ini`に接続先の情報を記載してください。

```
(省略)
isServer=false
listenPort=true
player1Ip=133.70.176.76	; 1人目の待ち受けIP
player1Port=9995		; 1人目の待ち受けポート
player2Ip=133.70.176.76	; 2人目の待ち受けIP
player2Port=9996		; 2人目の待ち受けポート
player3Ip=133.70.176.76	; 3人目の待ち受けIP
player3Port=9997		; 3人目の待ち受けポート
player4Ip=133.70.176.76	; 4人目の待ち受けIP
player4Port=9998		; 4人目の待ち受けポート
player5Ip=133.70.176.76	; 5人目の待ち受けIP
player5Port=9999		; 5人目の待ち受けポート
```

> isServer true: こちらがサーバとなり参加者を待ち受ける false: こちらがクライアントとなり参加者のプログラムへ接続しに行く

> listenPort=true: isServer=falseのとき true: ゲーム開始前に参加者がbindしたポートを聞き、そこに接続する。 false: 下で設定するIP, Portに接続をしに行く。  
(src/server/starter/NLPServerStarter.java参照)

> saveRoleCombination: 本戦以外での使用は想定していない\
true: 対戦を実行したプレイヤー：役職の関係をログに残し、本戦中に同じ配役の対戦が行われないようにする\
false: 考慮しない(デフォルトのランダムのみ)\
/src/game/SynchronousNLPAIWolfGame.javaのincludeAgentNumがtrueだと割り振られた番号も考慮する、falseは配役が同じならはじく

> roleCombinationDir: saveRoleCombination=trueのとき 本戦以外での使用は想定していない\
プレイヤー：役職の関係のログを保存するパスを指定する

> roleCombinationFilename: プレイヤー：役職の関係のログを保存するファイル名を指定する

> allParticipantNum: saveRoleCombination=trueのとき 本戦以外での使用は想定していない\
本戦全体で何人いるか指定する

> continueOtherCombinations: 本戦以外での使用は想定していない\
true: 一度対戦することとなった5人がgameNum回終了後、またランダムに5人選んでgameNum回ゲームをする。\
false: 一度対戦することとなった5人がgameNum回対戦したら終了

> continueCombinationsNum: continueOtherCombinations=trueのとき 本戦以外での使用は想定していない\
continueOtherCombinationsのセットを何回行うか指定する\
continueOtherCombinations * gameNum回 = 全試合数となる

## 実行方法
```
$ make build
$ make run
```

## 補足
2023/10/09現在、javaのバージョン等で`thalys`だと動かないかもしれません(未検証)

## 追記
現在、`roleCombinationFilename`では役職,エージェントの名前で保存しているが、エージェントの名前は名前から数字を除去した物を記載している。\
複数体のエージェントを使用している都合上、kanolab1, kanolab2など本質的には同じエージェントなので、同一視したいためこうしている。\
その都合上、a1b2c3のようにチーム名に数字が含まれている場合それも除去され、abcとなるため、除去先が他のチームメイト被っている場合まずいことになるので要注意\
詳細は`SynchronousNLPAIWolfGame.java`まで