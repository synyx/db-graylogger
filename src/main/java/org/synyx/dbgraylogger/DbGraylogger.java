package org.synyx.dbgraylogger;

import com.github.joschi.jadconfig.JadConfig;
import com.github.joschi.jadconfig.RepositoryException;
import com.github.joschi.jadconfig.ValidationException;
import com.github.joschi.jadconfig.repositories.PropertiesRepository;
import org.synyx.dbgraylogger.gelf.GelfSender;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbGraylogger {

    private static final Logger LOG = Logger.getLogger(DbGraylogger.class.getCanonicalName());

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, RepositoryException, ValidationException {

        String configFilename = "settings.properties";

        if (args.length > 0) {
            configFilename = args[0];
        }

        ConfigBean config = readConfig(configFilename);

        GelfMappings mappings = new GelfMappings(config.getMappingsFile());

        Connection conn = createConnection(config.getDatabaseDriver(), config.getDatabaseUrl(), config.getDatabaseUser(), config.getDatabasePassword());
        PreparedStatement ps = conn.prepareStatement(config.getPreparedStatement());

        GelfSender gelfSender = new GelfSender(config.getGraylogHostname(), config.getGraylogPort());

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Add shutdown hook in order to try to cleanup before exiting (e. g. closing database connection)
        Runtime.getRuntime().addShutdownHook(new ShutDownHook(scheduler, conn, ps, gelfSender));

        Runnable dbReader = new DatabaseReader(ps, gelfSender, mappings);
        ScheduledFuture<?> dbReaderHandle = scheduler.scheduleAtFixedRate(dbReader, 0, config.getPollingInterval(), TimeUnit.SECONDS);

        while (!dbReaderHandle.isDone()) {
            // Endless loop to block the main thread from exiting
        }
    }

    private static Connection createConnection(String driver, String url, String user, String password) throws ClassNotFoundException, SQLException {

        Class.forName(driver);

        return DriverManager.getConnection(url, user, password);
    }

    private static ConfigBean readConfig(String configFilename) throws RepositoryException, ValidationException {

        LOG.log(Level.INFO, "Reading configuration from " + configFilename);
        ConfigBean config = new ConfigBean();
        new JadConfig(new PropertiesRepository(configFilename), config).process();

        return config;
    }
}
