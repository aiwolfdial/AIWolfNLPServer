package common.util;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class GameLogFormat extends Formatter {
	public GameLogFormat() {
	}

	@Override
	public String format(LogRecord record) {

		return record.getMessage() +
				"\n";
	}

}
