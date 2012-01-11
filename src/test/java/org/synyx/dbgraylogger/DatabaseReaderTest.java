package org.synyx.dbgraylogger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.synyx.dbgraylogger.gelf.GelfMessage;
import org.synyx.dbgraylogger.gelf.GelfSender;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseReaderTest {

    private DatabaseReader databaseReader;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private GelfSender gelfSender;

    private GelfMappings gelfMappings;
    
    @Mock
    private ResultSet resultSet;
    
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    @Before
    public void setUp() throws SQLException {

        Properties mappings = new Properties();
        
        mappings.setProperty("short_message", "short_message");
        mappings.setProperty("full_message", "full_message");
        mappings.setProperty("timestamp", "0");
        mappings.setProperty("level", "level");
        mappings.setProperty("line", "line");
        mappings.setProperty("file", "file");
        mappings.setProperty("facility", "facility");
        mappings.setProperty("host", "host");
       
        gelfMappings = new GelfMappings(mappings);
        
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getString(anyString())).thenReturn("Foobar");
        when(resultSet.getTimestamp(anyString())).thenReturn(timestamp);
        
        databaseReader = new DatabaseReader(preparedStatement, gelfSender, gelfMappings);
    }

    @Test
    public void testRunNoResult() throws SQLException {

        reset(resultSet);
        when(resultSet.next()).thenReturn(false);

        databaseReader.run();

        verify(preparedStatement, times(2)).setTimestamp(anyInt(), any(Timestamp.class));
        verify(preparedStatement).executeQuery();
        
        verify(resultSet).next();
        verify(resultSet).close();
    }

    @Test
    public void testRun() throws SQLException {

        databaseReader.run();

        verify(preparedStatement, times(2)).setTimestamp(anyInt(), any(Timestamp.class));
        verify(preparedStatement).executeQuery();

        verify(resultSet, times(2)).next();
        verify(resultSet).close();
        
        verify(gelfSender).sendMessage(any(GelfMessage.class));
    }
}
