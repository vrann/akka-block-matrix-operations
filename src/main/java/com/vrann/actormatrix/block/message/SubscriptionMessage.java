package com.vrann.actormatrix.block.message;

import com.vrann.actormatrix.Message;

import java.util.Objects;

public class SubscriptionMessage implements Message {

    private String topic;

    public SubscriptionMessage(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionMessage that = (SubscriptionMessage) o;
        return Objects.equals(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic);
    }

    @Override
    public String toString() {
        return (new StringBuilder()).append("subscription to ").append(topic).toString();
    }
}
