package org.synyx.dbgraylogger;

import org.synyx.dbgraylogger.gelf.GelfMessage;
import org.synyx.dbgraylogger.gelf.GelfSender;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DatabaseReader implements Runnable {

    private PreparedStatement ps;
    private Timestamp lastRun = new Timestamp(System.currentTimeMillis());

    private GelfSender gelfSender;
    private GelfMappings mappings;

    public DatabaseReader(PreparedStatement ps, GelfSender gelfSender, GelfMappings mappings) {
        this.ps = ps;
        this.gelfSender = gelfSender;
        this.mappings = mappings;
    }

    public void run() {

        try {
            ps.setTimestamp(1, lastRun);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));

            ResultSet rs = ps.executeQuery();
            lastRun = new Timestamp(System.currentTimeMillis());

            try {
                while (rs.next()) {

                    String shortMessage = sanitizeMessageText(getValueFromMapping("short_message", rs));
                    String fullMessage = sanitizeMessageText(getValueFromMapping("full_message", rs));
                    
                    Timestamp timestamp = getTimestampFromMapping("timestamp", rs);
                    String level = getValueFromMapping("level", rs);
                    String line = getValueFromMapping("line", rs);
                    String file = getValueFromMapping("file", rs);
                    String facility = getValueFromMapping("facility", rs);
                    String host = getValueFromMapping("host", rs);

                    GelfMessage message = new GelfMessage(shortMessage, fullMessage, timestamp.getTime(), level, line, file);

                    message.setFacility(facility);
                    message.setHost(host);

                    for (String field : mappings.getAdditionalFields()) {

                        message.addField(field, getValueFromMapping(field, rs));
                    }

                    gelfSender.sendMessage(message);
                }
            } finally {
                try {
                    rs.close();
                } catch (Throwable ignore) {
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String sanitizeMessageText(String text) {
        return text.replace("\"", "\\\"");
    }

    private String getValueFromMapping(String key, ResultSet resultSet) throws SQLException {

        if (mappings.isFixedString(key)) {
            return mappings.getFixedString(key);
        } else {
            String columnName = mappings.getColumnName(key);
            return columnName == null ? null : resultSet.getString(columnName);
        }
    }

    private Timestamp getTimestampFromMapping(String key, ResultSet resultSet) throws SQLException {

        String columnName = mappings.getColumnName(key);
        return columnName == null ? new Timestamp(System.currentTimeMillis()) : resultSet.getTimestamp(columnName);
    }
}
