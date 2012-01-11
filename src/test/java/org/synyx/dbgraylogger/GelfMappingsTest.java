package org.synyx.dbgraylogger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class GelfMappingsTest {
    
    GelfMappings gelfMappings;
    
    @Before
    public void setUp() {
        
        Properties properties = new Properties();
        
        properties.setProperty("short_message", "foobar");
        properties.setProperty("full_message", "${column}");
        properties.setProperty("additional_field", "foobaz");
                
        gelfMappings = new GelfMappings(properties);
    }

    @Test
    public void testGetValue() {

        Assert.assertEquals("foobar", gelfMappings.getValue("short_message"));
        Assert.assertEquals("foobaz", gelfMappings.getValue("additional_field"));
        Assert.assertEquals("${column}", gelfMappings.getValue("full_message"));
        Assert.assertEquals(null, gelfMappings.getValue("does-not-exist"));
    }

    @Test
    public void testIsFixedString() {

        Assert.assertTrue(gelfMappings.isFixedString("short_message"));
        Assert.assertTrue(gelfMappings.isFixedString("additional_field"));
        Assert.assertFalse(gelfMappings.isFixedString("full_message"));
        Assert.assertFalse(gelfMappings.isFixedString("does-not-exist"));
    }
    
    @Test
    public void testGetColumnName() {
        
        Assert.assertEquals("", gelfMappings.getColumnName("short_message"));
        Assert.assertEquals("", gelfMappings.getColumnName("additional_field"));
        Assert.assertEquals("column", gelfMappings.getColumnName("full_message"));
        Assert.assertNull(gelfMappings.getColumnName("does-not-exist"));
    }

    @Test
    public void testGetFixedString() {

        Assert.assertEquals("foobar", gelfMappings.getFixedString("short_message"));
        Assert.assertEquals("foobaz", gelfMappings.getFixedString("additional_field"));
        Assert.assertEquals("", gelfMappings.getFixedString("full_message"));
        Assert.assertEquals("", gelfMappings.getFixedString("does-not-exist"));
    }
    
    @Test
    public void testGetFields() {

        Assert.assertEquals(3, gelfMappings.getFields().size());
        Assert.assertTrue(gelfMappings.getFields().contains("short_message"));
        Assert.assertTrue(gelfMappings.getFields().contains("additional_field"));
        Assert.assertTrue(gelfMappings.getFields().contains("full_message"));
        Assert.assertFalse(gelfMappings.getFields().contains("does-not-exist"));
    }

    @Test
    public void testGetAdditionalFields() {

        Assert.assertEquals(1, gelfMappings.getAdditionalFields().size());
        Assert.assertTrue(gelfMappings.getAdditionalFields().contains("additional_field"));
        Assert.assertFalse(gelfMappings.getAdditionalFields().contains("short_message"));
        Assert.assertFalse(gelfMappings.getAdditionalFields().contains("full_message"));
        Assert.assertFalse(gelfMappings.getFields().contains("does-not-exist"));
    }
    
}
