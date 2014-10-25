package org.rhq.msg.broker.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceNotFoundException;
import org.jboss.msc.service.StartException;

class BrokerSubsystemRestart implements OperationStepHandler {

    static final BrokerSubsystemRestart INSTANCE = new BrokerSubsystemRestart();

    private final Logger log = Logger.getLogger(BrokerSubsystemRestart.class);

    private BrokerSubsystemRestart() {
    }

    @Override
    public void execute(OperationContext opContext, ModelNode model) throws OperationFailedException {
        try {
            ServiceName name = BrokerService.SERVICE_NAME;
            BrokerService service = (BrokerService) opContext.getServiceRegistry(true).getRequiredService(name).getValue();
            log.info("Asked to restart the broker");
            service.stopBroker();
            service.startBroker();
        } catch (ServiceNotFoundException snfe) {
            throw new OperationFailedException("Cannot restart broker - the broker is disabled", snfe);
        } catch (StartException se) {
            throw new OperationFailedException("Cannot restart broker", se);
        }

        opContext.completeStep();
        return;
    }
}
