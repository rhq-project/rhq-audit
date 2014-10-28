package org.rhq.msg.broker.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceNotFoundException;

//TODO: this is just a stub to illustrate how to add a module extension operation that takes a parameter
class BrokerSubsystemOperationWithParameter implements OperationStepHandler {
    static final BrokerSubsystemOperationWithParameter INSTANCE = new BrokerSubsystemOperationWithParameter();

    private BrokerSubsystemOperationWithParameter() {
    }

    @Override
    public void execute(OperationContext opContext, ModelNode model) throws OperationFailedException {
        try {
            ServiceName name = BrokerService.SERVICE_NAME;
            BrokerService service = (BrokerService) opContext.getServiceRegistry(true).getRequiredService(name).getValue();

            String parameter = model.get(BrokerSubsystemDefinition.OP_WITH_PARAM_PARAMETER.getName()).asString();

            String results = parameter; // TODO: pass parameter to service. e.g. service.doIt(parameter);
            opContext.getResult().set(results);
        } catch (ServiceNotFoundException snfe) {
            throw new OperationFailedException("Cannot execute operation because the broker is not enabled");
        } catch (Exception e) {
            throw new OperationFailedException("Failed to execute operation [" + model + "]", e);
        }
        opContext.stepCompleted();
    }
}
