package common.net;

public interface GameClient {
	Object receive(Packet packet);
}