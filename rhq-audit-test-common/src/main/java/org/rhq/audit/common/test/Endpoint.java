package org.rhq.audit.common.test;

/**
 * POJO that allows tests to indicate the type of endpoint to use (queue or
 * topic) and that endpoint's name.
 */
public class Endpoint {
    public enum Type {
        QUEUE, TOPIC
    }

    private final Type type;
    private final String name;

    public Endpoint(Type type, String name) {
        if (type == null) {
            throw new NullPointerException("type must not be null");
        }
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }
        this.type = type;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

}
