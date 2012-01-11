package org.synyx.dbgraylogger;

import com.github.joschi.jadconfig.Parameter;
import com.github.joschi.jadconfig.validators.InetPortValidator;
import com.github.joschi.jadconfig.validators.PositiveIntegerValidator;

public class ConfigBean {

    @Parameter(value = "db.driver", required = true)
    private String databaseDriver;

    @Parameter(value = "db.url", required = true)
    private String databaseUrl;

    @Parameter("db.user")
    private String databaseUser;

    @Parameter("db.password")
    private String databasePassword;

    @Parameter(value = "db.statement", required = true)
    private String preparedStatement;

    @Parameter(value = "pollingInterval", required = true, validator = PositiveIntegerValidator.class)
    private int pollingInterval = 60;

    @Parameter(value = "graylog2.host", required = true)
    private String graylogHostname;

    @Parameter(value = "graylog2.port", validator = InetPortValidator.class)
    private int graylogPort = 12201;

    @Parameter(value = "mappingsFile", required = true)
    private String mappingsFile;

    public String getDatabaseDriver() {
        return databaseDriver;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public String getPreparedStatement() {
        return preparedStatement;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public String getGraylogHostname() {
        return graylogHostname;
    }

    public int getGraylogPort() {
        return graylogPort;
    }

    public String getMappingsFile() {
        return mappingsFile;
    }
}
