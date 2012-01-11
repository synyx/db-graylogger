package org.synyx.dbgraylogger;

import org.synyx.dbgraylogger.gelf.GelfSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShutDownHook extends Thread {

    private static final Logger LOG = Logger.getLogger(ShutDownHook.class.getCanonicalName());

    private ScheduledExecutorService scheduler;
    private Connection conn;
    private PreparedStatement ps;
    private GelfSender gelfSender;

    public ShutDownHook(ScheduledExecutorService scheduler, Connection conn, PreparedStatement ps, GelfSender gelfSender) {

        super();

        this.scheduler = scheduler;
        this.conn = conn;
        this.ps = ps;
        this.gelfSender = gelfSender;
    }

    @Override
    public void run() {
        
        LOG.log(Level.INFO, "Running shutdown hook to clean up");

        if (null != scheduler) {
            scheduler.shutdown();
        }
        
        if (null != gelfSender) {
            gelfSender.close();
        }

        if (null != ps) {
            try {
                ps.close();
            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "Couldn't close prepared statement", ex);
            }
        }

        if (null != conn) {
            try {
                conn.close();
            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "Couldn't close database connection", ex);
            }
        }
    }
}
