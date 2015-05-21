package cn.hotdev.server.connectors.impls;

import cn.hotdev.server.connectors.protocols.ConfigStoreProtocol;
import cn.hotdev.server.constants.ConfigOption;
import cn.hotdev.server.tools.Log;
import cn.hotdev.server.tools.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * Created by andy on 5/12/15.
 */
public class PropertiesFileConnector implements ConfigStoreProtocol {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesFileConnector.class);

    private Properties properties;
    private String fileName;
    private boolean isLoaded;

    public PropertiesFileConnector(String fileName) {
        this.properties = new Properties();
        this.fileName = fileName;
        this.isLoaded = false;
        reload();
    }

    public void reload() {

        properties.clear();

        InputStream is = null;

        /* first: try to load the outside file */
        try {

            String cfgPath = "";
            String projectPath = Utils.getProjectPath(PropertiesFileConnector.class);
            if (projectPath != null)
                cfgPath = projectPath + System.getProperty("file.separator", "/");
            cfgPath += fileName;

            File f = new File(cfgPath);
            if (f.exists()) {
                is = new FileInputStream(f);

                if (is != null)
                    properties.load(is);

                isLoaded = true;
                Log.info(logger, "Loaded outside properties {}", fileName);
            }
        } catch (FileNotFoundException e) {
            Log.err(logger, "Can't open outside properties {}", fileName);
        } catch (IOException e) {
            Log.err(logger, "Can't read outside properties {}", fileName);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.err(logger, "Loaded outside properties got exception {}", e.getMessage());
                }
            }
        }

        /* then: load the inside jar file */
        if (!isLoaded) {
            try {
                is = ClassLoader.getSystemResourceAsStream(fileName);

                if (is != null)
                    properties.load(is);

                isLoaded = true;
                Log.info(logger, "Loaded inside properties {}", fileName);
            } catch (FileNotFoundException e) {
                Log.err(logger, "Can't open inside properties {}", fileName);
            } catch (IOException e) {
                Log.err(logger, "Can't read inside properties {}", fileName);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        Log.err(logger, "Loaded inside properties got exception {}", e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public String option(ConfigOption option) {
        if (option == null)
            return "";

        String property = properties.getProperty(option.name(), option.defaultValue.getValue());
        if (property == null)
            return "";

        return property;
    }

    @Override
    public int intOption(ConfigOption option) {
        if (option == null)
            return 0;

        String property = properties.getProperty(option.name(), option.defaultValue.getValue());
        if (property == null)
            return 0;

        int v = 0;
        try {
            v = Integer.parseInt(property);
        } catch (NumberFormatException e) {
        }

        return v;
    }

    @Override
    public long longOption(ConfigOption option) {
        if (option == null)
            return 0;

        String property = properties.getProperty(option.name(), option.defaultValue.getValue());
        if (property == null)
            return 0;

        long v = 0;
        try {
            v = Integer.parseInt(property);
        } catch (NumberFormatException e) {
        }

        return v;
    }

    @Override
    public boolean boolOption(ConfigOption option) {
        if (option == null)
            return false;

        String property = properties.getProperty(option.name(), option.defaultValue.getValue());
        if (property == null)
            return false;

        return "true".equalsIgnoreCase(property);
    }
}
