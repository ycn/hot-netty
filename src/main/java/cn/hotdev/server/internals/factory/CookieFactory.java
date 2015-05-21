package cn.hotdev.server.internals.factory;

import io.netty.handler.codec.http.DefaultCookie;

import javax.ws.rs.core.NewCookie;

/**
 * Created by andy on 5/21/15.
 */
public class CookieFactory {

    public static io.netty.handler.codec.http.Cookie parseFromCommonCookie(javax.ws.rs.core.NewCookie cookie) {
        if (cookie == null || cookie.getName() == null || cookie.getValue() == null) {
            return null;
        }

        DefaultCookie nettyCookie = new DefaultCookie(cookie.getName(), cookie.getValue());
        nettyCookie.setComment(cookie.getComment());
        nettyCookie.setDomain(cookie.getDomain());
        nettyCookie.setMaxAge(cookie.getMaxAge());
        nettyCookie.setPath(cookie.getPath());
        nettyCookie.setHttpOnly(cookie.isHttpOnly());
        nettyCookie.setSecure(cookie.isSecure());
        nettyCookie.setVersion(cookie.getVersion());
        return nettyCookie;
    }

    public static javax.ws.rs.core.Cookie parseFromNettyCookie(io.netty.handler.codec.http.Cookie cookie) {
        if (cookie == null || cookie.getName() == null || cookie.getValue() == null) {
            return null;
        }

        NewCookie newCookie = new NewCookie(cookie.getName(),
                cookie.getValue(),
                cookie.getPath(),
                cookie.getDomain(),
                cookie.getVersion(),
                cookie.getComment(),
                (int) cookie.getMaxAge(),
                null,
                cookie.isSecure(),
                cookie.isHttpOnly());

        return newCookie;
    }
}
