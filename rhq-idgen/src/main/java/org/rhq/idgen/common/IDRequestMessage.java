package org.rhq.idgen.common;

import org.rhq.msg.common.BasicMessage;

public class IDRequestMessage extends BasicMessage {

    public static IDRequestMessage fromJSON(String json) {
        return BasicMessage.fromJSON(json, IDRequestMessage.class);
    }

    public IDRequestMessage(String message) {
        super(message);
    }
}
