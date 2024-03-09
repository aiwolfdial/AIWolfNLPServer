# AIWolfNLPServer
人狼知能本戦で運営から参加者に接続をする形に変えたので、それに対応したプログラムです。

## 設定
`res/AIWolfGameServer.ini`に接続先の情報を記載してください。
```

(省略)
IsServerFlag = false								; 説明が長くなるのでコメントを参照
ISPortListeningFlag = true							; 同上
IsSaveRoleCombinations = true 						; 同上
RoleCombinationsSavePath = ./log/					; 同上
RoleCombinationsSaveFileName = DoneCombinations.txt	; 同上
AllParticipantNum = 6								; 同上
IsContinueByOtherCombinations = true				; 同上
ContinueCombinationsNum = 5 						; 同上
PlayerHost1=133.70.176.76	; 1人目の待ち受けIP
PlayerPort1=9995			; 1人目の待ち受けポート
PlayerHost2=133.70.176.76	; 2人目の待ち受けIP
PlayerPort2=9996			; 2人目の待ち受けポート
PlayerHost3=133.70.176.76	; 3人目の待ち受けIP
PlayerPort3=9997			; 3人目の待ち受けポート
PlayerHost4=133.70.176.76	; 4人目の待ち受けIP
PlayerPort4=9998			; 4人目の待ち受けポート
PlayerHost5=133.70.176.76	; 5人目の待ち受けIP
PlayerPort5=9999			; 5人目の待ち受けポート
```

> IsServerFlag = false		; true: こちらがサーバとなり参加者を待ち受ける	false: こちらがクライアントとなり参加者のプログラムへ接続しに行く

> ISPortListeningFlag = true	; IsServerFlag = falseの時のみ関係する  true: ゲーム開始前に参加者がbindしたポートを聞き、そこに接続する。 false: 下で設定するIP,Portに接続をしに行く。  
詳細は(src/net/kanolab/aiwolf/server/starter/NLPServerStarter.javaを参照)

> IsSaveRoleCombinations: 本戦以外での使用は想定していない\
True: 対戦を実行したプレイヤー：役職の関係をログに残し、本戦中に同じ配役の対戦が行われないようにする\
False: 特に考慮しない(デフォルトのランダムのみ)\
/src/net/kanolab/aiwolf/server/game/SynchronousNLPAIWolfGame.javaのincludeAgentNumがtrueだと割り振られた番号も考慮する、falseは配役が同じならはじく

> RoleCombinationsSavePath: IsSaveRoleCombinations=trueの時のみ。本戦以外での使用は想定していない\
プレイヤー：役職の関係のログを保存するパスを指定する

> RoleCombinationsSaveFileName: RoleCombinationsSavePathとほぼ同じ
プレイヤー：役職の関係のログを保存するファイル名を指定する

> AllParticipantNum: IsSaveRoleCombinations=trueの時のみ。本戦以外での使用は想定していない\
本戦全体で何人いるか指定する

> IsContinueByOtherCombinations: 本戦以外での使用は想定していない\
True: 一度対戦することとなった5人がgameNum回終了後、またランダムに5人選んでgameNum回ゲームをする。\
Flase: 一度対戦することとなった5人がgameNum回対戦したら終了

> ContinueCombinationsNum: IsContinueByOtherCombinations=trueのときのみ。本戦以外での使用は想定していない\
IsContinueByOtherCombinationsのセットを何回行うか指定する。
IsContinueByOtherCombinations * gameNum回 = 全試合数となる。

## 実行方法
```
$ make run
```

## 補足
2023/10/09現在、javaのバージョン等で`thalys`だと動かないかもしれません(未検証)