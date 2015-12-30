package uk.gov.nationalarchvies.droid;

import com.sun.org.apache.xml.internal.security.Init;
import org.restlet.Component;
import org.restlet.data.Protocol;

public class Entry {
    public static void main(String[] args) throws Exception {

        Init.init();

        Component component = new Component();

        component.getServers().add(Protocol.HTTP, 8182);

        component.getDefaultHost().attach("/api", new App());

        component.start();
    }
}
