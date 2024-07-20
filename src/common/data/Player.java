package common.data;

import common.net.GameInfo;
import common.net.GameSetting;

public interface Player {
	String getName();

	void update(GameInfo gameInfo);

	void initialize(GameInfo gameInfo, GameSetting gameSetting);

	void dayStart();

	String talk();

	String whisper();

	Agent vote();

	Agent attack();

	Agent divine();

	Agent guard();

	void finish();
}
