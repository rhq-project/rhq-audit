package org.rhq.audit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@Singleton
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class AuditStartupBean {
    private final Logger log = LoggerFactory.getLogger(AuditStartupBean.class);

    @Resource(mappedName = "java:jboss/datasources/RHQAuditDS")
    private DataSource auditDataSource;

    @PostConstruct
    public void init() {
        log.info("Audit subsystem initializing");
        return;
    }
}
