package uk.gov.nationalarchvies.droid;

import com.sun.org.apache.xml.internal.security.Init;

public class SubmissionGatewayProvider {
    private static WebSubmissionGateway submissionGateway;

    public static WebSubmissionGateway getInstance()
            throws Exception {
        if (submissionGateway == null) {
            WebContext context = new ClassPathXmlWebContext();
            submissionGateway = context.getSubmissionGateway();
            Init.init();
        }
        return submissionGateway;
    }
}

