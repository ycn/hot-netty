package cn.hotdev.server;

import org.glassfish.jersey.server.spi.ContainerProvider;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

/**
 * Created by andy on 5/17/15.
 */
@Provider
public class HttpServerProvider implements ContainerProvider {
    @Override
    public <T> T createContainer(Class<T> aClass, Application application) throws ProcessingException {
        if (aClass != HttpServerHandler.class) {
            return null;
        }
        return aClass.cast(new HttpServerHandler(application));
    }
}
