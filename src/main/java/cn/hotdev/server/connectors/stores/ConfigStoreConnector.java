package cn.hotdev.server.connectors.stores;

import cn.hotdev.server.connectors.impls.PropertiesFileConnector;
import cn.hotdev.server.connectors.protocols.ConfigStoreProtocol;
import cn.hotdev.server.constants.ConfigOption;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by andy on 5/12/15.
 */
public class ConfigStoreConnector implements ConfigStoreProtocol {

    private static final AtomicReference<ConfigStoreConnector> instance = new AtomicReference<ConfigStoreConnector>();

    private ConfigStoreProtocol delegate;

    public static ConfigStoreConnector getInstance() {
        if (instance.get() == null) {
            instance.compareAndSet(null, new ConfigStoreConnector());
        }
        return instance.get();
    }

    private ConfigStoreConnector() {
        delegate = new PropertiesFileConnector("config.properties");
    }


    @Override
    public String option(ConfigOption option) {
        return delegate.option(option);
    }

    @Override
    public int intOption(ConfigOption option) {
        return delegate.intOption(option);
    }

    @Override
    public long longOption(ConfigOption option) {
        return delegate.longOption(option);
    }

    @Override
    public boolean boolOption(ConfigOption option) {
        return delegate.boolOption(option);
    }
}
