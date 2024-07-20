package common.bin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketTimeoutException;

import common.data.Player;
import common.data.Role;
import common.net.TcpClient;

public class ClientStarter {
	public static void main(String[] args) throws SocketTimeoutException, IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		String host = null;
		int port = -1;
		String clsName = null;

		// Add RoleRequest
		Role roleRequest = null;

		String playerName = null;

		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-")) {
				switch (args[i]) {
					case "-p" -> {
						i++;
						port = Integer.parseInt(args[i]);
					}
					case "-h" -> {
						i++;
						host = args[i];
					}
					case "-c" -> {
						i++;
						clsName = args[i];

						// RoleRequestStarterのｌ56～ｌ72を参考にして以下追加
						i++;
						try {
							if (i > args.length - 1 || args[i].startsWith("-")) {
								i--;
								roleRequest = null;
								continue;
							}
							roleRequest = Role.valueOf(args[i]);
						} catch (IllegalArgumentException e) {
							System.err.println("No such role as " + args[i]);
							return;
						}
					}
					case "-n" -> {
						i++;
						playerName = args[i];
					}
				}
			}
		}
		if (port < 0 || host == null || clsName == null) {
			System.err.println("Usage:" + ClientStarter.class + " -h host -p port -c clientClass [-n name]");
			return;
		}

		Player player;
		try {
			player = (Player) Class.forName(clsName).getDeclaredConstructor().newInstance();
			// 引数にRoleRequestを追加
			TcpClient client = new TcpClient(host, port, roleRequest);
			if (playerName != null) {
				client.setName(playerName);
				// System.out.println("Set name "+client.getName());
			}
			if (client.connect(player)) {
				// System.out.println("Player connected to server:"+player);
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
