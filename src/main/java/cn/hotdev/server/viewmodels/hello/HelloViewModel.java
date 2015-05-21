package cn.hotdev.server.viewmodels.hello;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by andy on 5/12/15.
 */
@Path("hello")
public class HelloViewModel {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Hello Jersey!";
    }
}
