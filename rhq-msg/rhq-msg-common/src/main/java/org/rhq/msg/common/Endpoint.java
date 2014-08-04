package org.rhq.msg.common;

/**
 * POJO that indicates the type of endpoint (queue or topic) and that queue or
 * topic's name.
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

    @Override
    public String toString() {
        return "{" + type.name() + "}" + name;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Endpoint)) {
            return false;
        }

        Endpoint other = (Endpoint) obj;

        if (type != other.type) {
            return false;
        }

        if (!name.equals(other.name)) {
            return false;
        }

        return true;
    }

}
