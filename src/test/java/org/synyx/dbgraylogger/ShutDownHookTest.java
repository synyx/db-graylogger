package org.synyx.dbgraylogger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.synyx.dbgraylogger.gelf.GelfSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ShutDownHookTest {

    private ShutDownHook shutDownHook;

    @Mock
    private ScheduledExecutorService scheduler;

    @Mock
    private Connection conn;

    @Mock
    private PreparedStatement ps;

    @Mock
    private GelfSender gelfSender;


    @Before
    public void setUp() {

        shutDownHook = new ShutDownHook(scheduler, conn, ps, gelfSender);
    }

    @Test
    public void testNullChecks() {

        shutDownHook = new ShutDownHook(null, null, null, null);

        // NPE shouldn't occur
        shutDownHook.run();
    }

    @Test
    public void testRun() throws SQLException {

        shutDownHook.run();

        verify(scheduler).shutdown();
        verify(conn).close();
        verify(ps).close();
        verify(gelfSender).close();
    }

    @Test
    public void testExceptions() throws SQLException {

        doThrow(SQLException.class).when(conn).close();
        doThrow(SQLException.class).when(ps).close();

        try {
            shutDownHook.run();
        } catch (Exception ex) {
            fail("This exception should not be thrown");
        }

        verify(scheduler).shutdown();
        verify(conn).close();
        verify(ps).close();
        verify(gelfSender).close();
    }
}
