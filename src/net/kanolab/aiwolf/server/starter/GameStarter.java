package net.kanolab.aiwolf.server.starter;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import net.kanolab.aiwolf.server.common.GameConfiguration;

/**
 * 起動中のゲームの管理
 * 新規ゲームの起動
 * 
 * @author tminowa
 * 
 *         追記 2024/04/02
 *         isGameRunning
 *         1ゲームずつ実行するように(本戦用)
 * @author nwatanabe
 *
 */
public class GameStarter extends Thread {
	private List<NLPGameBuilder> gameList = new ArrayList<>();
	private Queue<List<Socket>> socketQue;
	private GameConfiguration config;

	public GameStarter(Queue<List<Socket>> socketQue, GameConfiguration config) {
		this.socketQue = socketQue;
		this.config = config;
	}

	@Override
	public void run() {
		while (true) {
			// 実行が終了しているサーバの削除
			Iterator<NLPGameBuilder> serverIterator = gameList.iterator();
			while (serverIterator.hasNext()) {
				NLPGameBuilder server = serverIterator.next();
				if (!server.isAlive()) {
					serverIterator.remove();
				}
			}

			// 同時起動数未満なら待機Listから1グループ取得してゲームを開始する
			synchronized (socketQue) {
				if (socketQue.size() > 0 && gameList.size() < config.getParallelRunningGameNum()) {
					NLPGameBuilder builder = new NLPGameBuilder(socketQue.poll(), config);
					gameList.add(builder);
					builder.start();
				}
			}

			// CPU使用率上昇対策
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isWaitingGame() {
		return socketQue.size() != 0;
	}

	public boolean isGameRunning() {
		return gameList.size() != 0;
	}

}