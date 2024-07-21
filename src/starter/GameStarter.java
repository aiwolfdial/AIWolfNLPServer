package starter;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import common.GameConfiguration;

public class GameStarter extends Thread {
	private final List<NLPGameBuilder> gameList = new ArrayList<>();
	private final Queue<List<Socket>> socketQue;
	private final GameConfiguration config;

	public GameStarter(Queue<List<Socket>> socketQue, GameConfiguration config) {
		this.socketQue = socketQue;
		this.config = config;
	}

	@Override
	public void run() {
		while (true) {
			// 実行が終了しているサーバの削除
			gameList.removeIf(server -> !server.isAlive());

			// 同時起動数未満なら待機Listから1グループ取得してゲームを開始する
			synchronized (socketQue) {
				if (!socketQue.isEmpty() && gameList.size() < config.getParallelRunningGameNum()) {
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
		return !socketQue.isEmpty();
	}

	public boolean isGameRunning() {
		return !gameList.isEmpty();
	}

}