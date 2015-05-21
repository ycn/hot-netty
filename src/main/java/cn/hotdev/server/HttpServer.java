package cn.hotdev.server;

import ch.qos.logback.classic.LoggerContext;
import cn.hotdev.server.connectors.stores.ConfigStoreConnector;
import cn.hotdev.server.constants.ConfigOption;
import cn.hotdev.server.tools.Log;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import javax.ws.rs.core.Application;
import java.security.cert.CertificateException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Main Entry of this application.
 * <p>
 * Created by andy on 5/12/15.
 */
public class HttpServer extends Application {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private static final ConfigStoreConnector CONFIG = ConfigStoreConnector.getInstance();
    private static final AtomicReference<HttpServer> instance = new AtomicReference<HttpServer>();

    public static HttpServer getInstance() {
        if (instance.get() == null) {
            instance.compareAndSet(null, new HttpServer());
        }
        return instance.get();
    }

    private HttpServer() {

    }

    private void destroy() {
        // assume SLF4J is bound to logback-classic in the current environment
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.stop();
    }

    public static void main(String[] args) {

        final String ENV = CONFIG.option(ConfigOption.ENV);
        final boolean SSL = CONFIG.boolOption(ConfigOption.SERVER_USE_SSL);
        final int PORT = SSL
                ? CONFIG.intOption(ConfigOption.SERVER_SSL_PORT)
                : CONFIG.intOption(ConfigOption.SERVER_PORT);

        printEnvironment();

        SslContext sslCtx = null;

        if (SSL) {
            try {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
            } catch (CertificateException | SSLException e) {
                Log.err(logger, "server_ssl_exception exp={}", e.getMessage());
            }
        } else {
            sslCtx = null;
        }

        HttpServer server = HttpServer.getInstance();

        EventLoopGroup bossGroup;
        EventLoopGroup workerGroup;

        if ("dev".equals(ENV)) {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
        } else {
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup();
        }

        try {

            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup)
                    .channel("dev".equals(ENV)
                            ? NioServerSocketChannel.class
                            : EpollServerSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,
                            CONFIG.boolOption(ConfigOption.SERVER_TCP_NODELAY))
                    .option(ChannelOption.SO_KEEPALIVE,
                            CONFIG.boolOption(ConfigOption.SERVER_SO_KEEPALIVE))
                    .option(ChannelOption.SO_BACKLOG,
                            CONFIG.intOption(ConfigOption.SERVER_SO_BACKLOG))
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                            CONFIG.intOption(ConfigOption.SERVER_CONNECT_TIMEOUT_MILLIS))
                    .option(ChannelOption.ALLOCATOR,
                            PooledByteBufAllocator.DEFAULT)
                    .handler(new LoggingHandler(LogLevel.valueOf(CONFIG.option(ConfigOption.NETTY_LOG_LEVEL))))
                    .childHandler(new HttpServerInitializer(server, sslCtx));

            Channel ch = b.bind(PORT).sync().channel();

            Log.debug(logger, "Open your web browser and navigate to {}", getBaseUri());

            ch.closeFuture().sync();

        } catch (InterruptedException e) {

            Log.err(logger, "server_interrupted");

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            server.destroy();
        }
    }

    public static String getBaseUri() {
        final boolean SSL = CONFIG.boolOption(ConfigOption.SERVER_USE_SSL);
        final int PORT = SSL
                ? CONFIG.intOption(ConfigOption.SERVER_SSL_PORT)
                : CONFIG.intOption(ConfigOption.SERVER_PORT);

        final String HOST = CONFIG.option(ConfigOption.SERVER_HOST);
        final String BASE_PATH = CONFIG.option(ConfigOption.SERVER_BASE_PATH);

        return (SSL ? "https://" : "http://") + HOST + ":" + PORT + BASE_PATH;
    }

    private static void printEnvironment() {
        Log.debug(logger, "app_start env={}", CONFIG.option(ConfigOption.ENV));
    }
}
