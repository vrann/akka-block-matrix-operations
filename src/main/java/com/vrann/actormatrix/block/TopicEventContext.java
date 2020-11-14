package com.vrann.actormatrix.block;

import java.util.Objects;

public class TopicEventContext implements EventContext {

    private String topic;

    public TopicEventContext(String topic) {
        this.topic = topic;
    }

    public static EventContext from(String topic) {
        return new TopicEventContext(topic);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicEventContext that = (TopicEventContext) o;
        return Objects.equals(topic, that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic);
    }

    @Override
    public String toString() {
        return topic;
    }
}
