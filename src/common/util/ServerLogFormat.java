package common.util;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ServerLogFormat extends Formatter {
	boolean isDetail;

	public ServerLogFormat(boolean isDetail) {
		this.isDetail = isDetail;

	}

	public ServerLogFormat() {
		this(true);
	}

	@Override
	public String format(LogRecord record) {
		final StringBuilder buf = new StringBuilder();

		if (isDetail) {
			buf.append(record.getLevel());
			buf.append(" ");
			buf.append(CalendarTools.toDateTime(record.getMillis()));
			buf.append("@");
			buf.append(record.getSourceClassName() + "#" + record.getSourceMethodName());
		}
		buf.append(record.getLoggerName());
		buf.append(":");
		buf.append(record.getMessage());
		buf.append("\n");

		return buf.toString();
	}

}
