package uk.gov.nationalarchvies.droid;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import uk.gov.nationalarchives.droid.core.interfaces.config.RuntimeConfig;

public class ClassPathXmlWebContext implements WebContext {
    private ClassPathXmlApplicationContext context;

    public ClassPathXmlWebContext() {
        RuntimeConfig.configureRuntimeEnvironment();
        this.context = new ClassPathXmlApplicationContext("/web-spring.xml");
    }

    public WebSubmissionGateway getSubmissionGateway()
            throws Exception {
        try {
            return (WebSubmissionGateway) this.context.getBean("submissionGateway");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
