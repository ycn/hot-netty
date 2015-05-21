package cn.hotdev.server.constants;

import cn.hotdev.server.internals.objects.DefaultValue;

/**
 * Created by andy on 5/13/15.
 */
public enum ConfigOption {

    SERVER_NAME(new DefaultValue("hot-netty")),
    SERVER_VERSION(new DefaultValue("1.0")),
    SERVER_HOST(new DefaultValue("localhost")),
    SERVER_BASE_PATH(new DefaultValue("/hot-netty/")),
    SERVER_PORT(new DefaultValue(9080)),
    SERVER_USE_SSL(new DefaultValue(false)),
    SERVER_SSL_PORT(new DefaultValue(9443)),
    SERVER_TCP_NODELAY(new DefaultValue(true)),
    SERVER_SO_KEEPALIVE(new DefaultValue(true)),
    SERVER_SO_BACKLOG(new DefaultValue(100)),
    SERVER_CONNECT_TIMEOUT_MILLIS(new DefaultValue(500000)),

    NETTY_LOG_LEVEL(new DefaultValue("DEBUG")),
    NETTY_MAX_REQUEST_CONTENT_LEN(new DefaultValue(1048576)), // 1mb
    NETTY_WRITE_BUF_SIZE(new DefaultValue(65535)), // 64kb

    ENV(new DefaultValue("dev"));

    public DefaultValue defaultValue;

    ConfigOption(DefaultValue defaultValue) {
        this.defaultValue = defaultValue;
    }
}
