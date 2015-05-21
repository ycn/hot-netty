package cn.hotdev.server;

import cn.hotdev.server.connectors.stores.ConfigStoreConnector;
import cn.hotdev.server.constants.ConfigOption;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;

/**
 * Created by andy on 5/12/15.
 */
class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final ConfigStoreConnector CONFIG = ConfigStoreConnector.getInstance();

    private final HttpServer server;
    private final SslContext sslCtx;

    public HttpServerInitializer(HttpServer server, SslContext sslCtx) {
        this.server = server;
        this.sslCtx = sslCtx;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }


        // p.addLast(new ReadTimeoutHandler(Config.NETTY_READ_TIMEOUT, TimeUnit.MILLISECONDS));
        p.addLast(new HttpRequestDecoder());
        // Uncomment the following line if you don't want to handle HttpChunks.
        p.addLast(new HttpObjectAggregator(CONFIG.intOption(ConfigOption.NETTY_MAX_REQUEST_CONTENT_LEN))); // 1mb
        p.addLast(new HttpResponseEncoder());
        // Remove the following line if you don't want automatic content compression.
        // p.addLast(new HttpContentCompressor());
        // p.addLast(new WriteTimeoutHandler(Config.NETTY_WRITE_TIMEOUT, TimeUnit.MILLISECONDS));
        p.addLast(new HttpServerHandler(server));
    }
}
