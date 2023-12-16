# AIWolfNLPServer
人狼知能本戦で運営から参加者に接続をする形に変えたので、それに対応したプログラムです。

## 設定
`res/NLPAIWolfServer_Client.ini`に接続先の情報を記載してください。
```

(省略)
IsServerFlag = false		; 説明が長くなるのでコメントを参照
ISPortListeningFlag = true	; 説明が長くなるのでコメントを参照 
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

## 実行方法
```
$ cd src/	# src以下にいない場合は移動してください
$ make run
```

## 補足
2023/10/09現在、javaのバージョン等で`thalys`だと動かないかもしれません(未検証)