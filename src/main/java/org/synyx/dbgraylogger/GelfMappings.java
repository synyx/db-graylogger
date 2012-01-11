package org.synyx.dbgraylogger;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

public class GelfMappings {

    private static final Logger LOG = Logger.getLogger(GelfMappings.class.getCanonicalName());

    private static final Set<String> GELF_DEFAULT_FIELDS = new HashSet<String>();
    
    static {
        GELF_DEFAULT_FIELDS.add("host");
        GELF_DEFAULT_FIELDS.add("short_message");
        GELF_DEFAULT_FIELDS.add("full_message");
        GELF_DEFAULT_FIELDS.add("timestamp");
        GELF_DEFAULT_FIELDS.add("level");
        GELF_DEFAULT_FIELDS.add("facility");
        GELF_DEFAULT_FIELDS.add("file");
        GELF_DEFAULT_FIELDS.add("line");
    }

    private Properties mappings;

    public GelfMappings(String mappingsFilename) throws IOException {

        mappings = new Properties();

        loadMapping(mappingsFilename);
    }

    public GelfMappings(Properties properties) {

        mappings = properties;
    }

    public boolean isFixedString(String key) {

        String value = getValue(key);

        return value != null && !(value.startsWith("${") && value.endsWith("}"));
    }

    public String getColumnName(String key) {

        if (isFixedString(key)) {
            return "";
        } else {
            String value = getValue(key);
            return value == null ? null : value.substring(2, value.length() - 1);
        }
    }

    public String getFixedString(String key) {

        return isFixedString(key) ? getValue(key) : "";
    }

    public String getValue(String key) {

        return mappings.getProperty(key);
    }

    public Set<String> getFields() {

        return mappings.stringPropertyNames();
    }

    public Set<String> getAdditionalFields() {
        Set<String> result = new HashSet<String>();

        for (String field : getFields()) {

            if (!GELF_DEFAULT_FIELDS.contains(field)) {
                result.add(field);
            }
        }

        return result;
    }

    private void loadMapping(String filename) throws IOException {

        Reader reader = null;

        try {
            reader = new FileReader(filename);
            mappings.load(reader);
        } finally {
            if (null != reader) {
                reader.close();
            }
        }
    }
}
