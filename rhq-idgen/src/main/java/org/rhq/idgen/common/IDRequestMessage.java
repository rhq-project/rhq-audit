package org.rhq.idgen.common;

import org.rhq.msg.common.BasicMessage;
import org.rhq.msg.common.SimpleBasicMessage;

public class IDRequestMessage extends SimpleBasicMessage {

    public static IDRequestMessage fromJSON(String json) {
        return BasicMessage.fromJSON(json, IDRequestMessage.class);
    }

    public IDRequestMessage(String message) {
        super(message);
    }
}
