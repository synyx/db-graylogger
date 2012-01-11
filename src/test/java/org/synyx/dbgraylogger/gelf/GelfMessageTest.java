package org.synyx.dbgraylogger.gelf;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;

public class GelfMessageTest {
    
    private GelfMessage gelfMessage;       
    
    @Test
    public void testTimestamp() {
        
        gelfMessage = new GelfMessage("", "", 0l, "", "", "");
        Assert.assertEquals(Long.valueOf(0l), gelfMessage.getTimestamp());
        Assert.assertEquals(Long.valueOf(0l), gelfMessage.getJavaTimestamp());

        gelfMessage = new GelfMessage("", "", 200l, "", "", "");
        Assert.assertEquals(Long.valueOf(0l), gelfMessage.getTimestamp());
        Assert.assertEquals(Long.valueOf(200l), gelfMessage.getJavaTimestamp());
        
        gelfMessage = new GelfMessage("", "", 2000l, "", "", "");
        Assert.assertEquals(Long.valueOf(2l), gelfMessage.getTimestamp());
        Assert.assertEquals(Long.valueOf(2000l), gelfMessage.getJavaTimestamp());
    }
    
    @Test
    public void testIsValid() {

        gelfMessage = new GelfMessage("", "", 0l, "", "", "");
        Assert.assertFalse(gelfMessage.isValid());

        gelfMessage.setVersion("1.0");
        gelfMessage.setHost("example.com");
        gelfMessage.setShortMessage("short message");
        gelfMessage.setFacility("facility");
        Assert.assertTrue(gelfMessage.isValid());
        
        gelfMessage.setHost("");
        Assert.assertFalse(gelfMessage.isValid());
    }
    
    @Test
    public void testAdditionalFields() {

        gelfMessage = new GelfMessage("", "", 0l, "", "", "");
        
        Assert.assertTrue(gelfMessage.getAdditonalFields().isEmpty());

        gelfMessage.addField("Foo", "Bar");
        Assert.assertEquals(1, gelfMessage.getAdditonalFields().size());

        gelfMessage.addField("Foo", new Object());
        Assert.assertEquals(1, gelfMessage.getAdditonalFields().size());

        gelfMessage.addField("Date", new Date());
        Assert.assertEquals(2, gelfMessage.getAdditonalFields().size());

        gelfMessage.setAdditonalFields(Collections.<String, Object>emptyMap());
        Assert.assertTrue(gelfMessage.getAdditonalFields().isEmpty());
    }
}
