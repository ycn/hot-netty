package cn.hotdev.server;

import cn.hotdev.server.connectors.stores.ConfigStoreConnector;
import cn.hotdev.server.constants.ConfigOption;
import cn.hotdev.server.internals.factory.CookieFactory;
import cn.hotdev.server.tools.Log;
import cn.hotdev.server.tools.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.*;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.is100ContinueExpected;
import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by andy on 5/12/15.
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<Object> implements Container, ContainerResponseWriter {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);
    private static final ConfigStoreConnector CONFIG = ConfigStoreConnector.getInstance();
    private static final boolean SSL = CONFIG.boolOption(ConfigOption.SERVER_USE_SSL);
    private static final URI BASE_URI = URI.create(HttpServer.getBaseUri());
    private static final int WRITE_BUF_SIZE = CONFIG.intOption(ConfigOption.NETTY_WRITE_BUF_SIZE);

    private final ApplicationHandler appHandler;
    private ChannelHandlerContext ctx;

    private DefaultFullHttpRequest httpRequest;
    private DefaultFullHttpResponse httpResponse;

    private String clientIp;
    private String serverIp;
    private boolean keepAlive;
    private HttpMethod httpMethod;
    private HttpVersion httpVersion;

    public HttpServerHandler(Application application) {

        appHandler = new ApplicationHandler(application);

        clientIp = "";
        serverIp = "";
        keepAlive = false;
    }

    private void initWithRequest(ChannelHandlerContext ctx, HttpRequest request) {

        // channel context
        this.ctx = ctx;

        final HttpHeaders headers = request.headers();

        // client ip
        if (headers.contains("X-Forwarded-For")) {
            clientIp = headers.get("X-Forwarded-For");
        } else if (headers.contains("X-Real-IP")) {
            clientIp = headers.get("X-Real-IP");
        } else {
            InetSocketAddress remote = (InetSocketAddress) ctx.channel().remoteAddress();
            clientIp = remote.getAddress().getHostAddress();
        }

        // server ip
        InetSocketAddress local = (InetSocketAddress) ctx.channel().localAddress();
        serverIp = local.getAddress().getHostAddress();

        // keepAlive
        keepAlive = isKeepAlive(request);

        // request httpMethod
        httpMethod = request.getMethod();

        // http protocol version
        httpVersion = request.getProtocolVersion();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof DefaultFullHttpRequest) {

            httpRequest = (DefaultFullHttpRequest) msg;

            if (is100ContinueExpected(httpRequest)) {
                send100Continue(ctx);
            }

            URI requestUri;
            try {
                requestUri = BASE_URI.resolve(httpRequest.getUri());
            } catch (IllegalArgumentException throwable) {
                errorOnFlush(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }

            initWithRequest(ctx, httpRequest);

            ContainerRequest containerRequest = new ContainerRequest(
                    BASE_URI,
                    requestUri,
                    httpMethod.name(),
                    new HttpServerSecurityContext(httpRequest, SSL),
                    new MapPropertiesDelegate());

            // request content
            containerRequest.setEntityStream(new ByteBufInputStream(httpRequest.content()));

            // jersey response writer
            containerRequest.setWriter(this);

            // add headers
            final MultivaluedMap<String, String> headers = containerRequest.getHeaders();
            for (Map.Entry<String, String> header : httpRequest.headers()) {
                headers.add(header.getKey(), header.getValue());
            }

            // add cookies
            final Map<String, javax.ws.rs.core.Cookie> cookies = containerRequest.getCookies();
            final String cookieString = httpRequest.headers().get(COOKIE);
            if (cookieString != null) {
                Set<Cookie> nettyCookies = CookieDecoder.decode(cookieString);
                if (!nettyCookies.isEmpty()) {
                    for (Cookie cookie : nettyCookies) {
                        cookies.put(cookie.getName(), CookieFactory.parseFromNettyCookie(cookie));
                    }
                }
            }

            // handle request
            appHandler.handle(containerRequest);
            
        } else {

            errorOnFlush(ctx, HttpResponseStatus.BAD_REQUEST);
        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        StackTraceElement[] stackTrace = cause.getStackTrace();
        Log.err(logger, "server_exception cause={} trace={}", cause.getMessage(), Utils.makeString(stackTrace, 0));
        ctx.close();
    }

    /**
     * Closes the specified channel after sends an error status & message.
     */
    private void errorOnFlush(ChannelHandlerContext ctx, HttpResponseStatus status) {
        if (ctx != null) {
            ctx.write(new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.EMPTY_BUFFER));
            Log.err(logger, "server_error code={}", status.code());
            closeOnFlush(ctx);
        }
    }

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    private void closeOnFlush(ChannelHandlerContext ctx) {
        if (ctx != null) {
            Channel channel = ctx.channel();
            if (channel != null && channel.isActive() && channel.isWritable()) {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            } else {
                ctx.close();
            }
        }
    }

    private void send100Continue(ChannelHandlerContext ctx) {
        if (ctx != null) {
            ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
        }
    }

    @Override
    public ResourceConfig getConfiguration() {
        return appHandler.getConfiguration();
    }

    @Override
    public ApplicationHandler getApplicationHandler() {
        return appHandler;
    }

    @Override
    public void reload() {
        // TODO: Jersey container reload
        Log.debug(logger, "TODO: Jersey container reload");
    }

    @Override
    public void reload(ResourceConfig resourceConfig) {
        // TODO: Jersey container reload with config
        Log.debug(logger, "TODO: Jersey container reload with config");
    }

    @Override
    public OutputStream writeResponseStatusAndHeaders(long contentLength, ContainerResponse responseContext) throws ContainerException {

        Channel channel = ctx.channel();

        final String entity = (String) responseContext.getEntity();
        final byte[] encodedBytes = entity.getBytes(Charset.forName("UTF-8"));

        final ByteBuf b = ctx.alloc().buffer(WRITE_BUF_SIZE);
        b.writeBytes(encodedBytes);
        long length = b.readableBytes(); // contentLength


        httpResponse = new DefaultFullHttpResponse(httpVersion,
                HttpResponseStatus.valueOf(responseContext.getStatus()),
                b);


        if (!responseContext.getHeaders().containsKey(HttpHeaders.Names.CONTENT_LENGTH)) {
            HttpHeaders.setContentLength(httpResponse, length);
        }

        if (keepAlive) {
            HttpHeaders.addHeader(httpResponse,
                    HttpHeaders.Names.CONNECTION,
                    HttpHeaders.Values.KEEP_ALIVE);
        }

        // add headers
        for (Map.Entry<String, List<String>> headerEntry : responseContext.getStringHeaders().entrySet()) {
            HttpHeaders.addHeader(httpResponse,
                    headerEntry.getKey(),
                    Utils.join(", ", headerEntry.getValue()));
        }

        // add cookies
        for (Map.Entry<String, NewCookie> cookieEntry : responseContext.getCookies().entrySet()) {
            HttpHeaders.addHeader(httpResponse,
                    HttpHeaders.Names.SET_COOKIE,
                    ServerCookieEncoder.encode(CookieFactory.parseFromCommonCookie(cookieEntry.getValue())));
        }

        return new ByteBufOutputStream(httpResponse.content());
    }

    @Override
    public boolean suspend(long timeOut, TimeUnit timeUnit, TimeoutHandler timeoutHandler) {
        // TODO: Trying to suspend
        Log.debug(logger, "TODO: Trying to suspend for {} ms, handler {}", timeUnit.toMillis(timeOut), timeoutHandler);
        return false;
    }

    @Override
    public void setSuspendTimeout(long timeOut, TimeUnit timeUnit) throws IllegalStateException {
        // TODO: Setting suspend timeout
        Log.debug(logger, "TODO: Setting suspend timeout to {} ms", timeUnit.toMillis(timeOut));
    }

    @Override
    public void commit() {
        if (ctx != null) {

            Channel channel = ctx.channel();

            if (channel.isActive()) {

                // Write the response.
                final ChannelFuture channelFuture = ctx.writeAndFlush(httpResponse);

                if (!keepAlive) {
                    // If keep-alive is off, close the connection once the content is fully written.
                    closeOnFlush(ctx);
                } else {
                    channelFuture.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                }
            }
        }
    }

    @Override
    public void failure(Throwable error) {
        Log.err(logger, "server_error exception={}", error.getMessage());
        errorOnFlush(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public boolean enableResponseBuffering() {
        return false;
    }
}
