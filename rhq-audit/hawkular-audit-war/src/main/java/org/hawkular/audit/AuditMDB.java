package org.hawkular.audit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.MessageListener;
import javax.sql.DataSource;

import org.hawkular.audit.consumer.DataSourceConsumer;

@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "AuditQueue") })
public class AuditMDB extends DataSourceConsumer {
    @Resource(mappedName = "java:jboss/datasources/RHQAuditDS")
    private DataSource auditDataSource;

    @PostConstruct
    public void init() {
        initialize(this.auditDataSource, null);
    }
}
