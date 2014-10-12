package uk.gov.nationalarchvies.droid;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class App extends Application {
    public synchronized Restlet createInboundRoot()
    {
        Router router = new Router(getContext());
        router.attach("/check/{filename}", FileCheckResource.class);
        return router;
    }
}
