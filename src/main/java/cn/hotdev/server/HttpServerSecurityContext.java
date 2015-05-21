package cn.hotdev.server;

import com.sun.security.auth.UserPrincipal;
import io.netty.handler.codec.http.HttpRequest;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

/**
 * Created by andy on 5/17/15.
 */
public class HttpServerSecurityContext implements SecurityContext {

    private HttpRequest request;
    private boolean isSecure;

    public HttpServerSecurityContext(HttpRequest request, boolean isSecure) {
        this.request = request;
        this.isSecure = isSecure;
    }

    @Override
    public Principal getUserPrincipal() {
        return new UserPrincipal("user");
    }

    @Override
    public boolean isUserInRole(String role) {
        return true;
    }

    @Override
    public boolean isSecure() {
        return isSecure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "basic";
    }
}
