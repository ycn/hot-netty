package cn.hotdev.server.connectors.protocols;

import cn.hotdev.server.constants.ConfigOption;

/**
 * Created by andy on 5/13/15.
 */
public interface ConfigStoreProtocol {

    public String option(ConfigOption option);
    public int intOption(ConfigOption option);
    public long longOption(ConfigOption option);
    public boolean boolOption(ConfigOption option);
}
