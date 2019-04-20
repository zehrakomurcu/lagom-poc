package com.movingimage.challenge.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.Date;


/**
 * This interface defines all the events that the MeasurementEntity supports.
 * <p>
 * By convention, the events should be inner classes of the interface, which
 * makes it simple to get a complete picture of what events an entity has.
 */
public interface MeasurementEvent extends Jsonable, AggregateEvent<MeasurementEvent> {
    /**
     * Tags are used for getting and publishing streams of events. Each event
     * will have this tag, and in this case, we are partitioning the tags into
     * 4 shards, which means we can have 4 concurrent processors/publishers of
     * events.
     */
    AggregateEventShards<MeasurementEvent> TAG = AggregateEventTag.sharded(MeasurementEvent.class, 4);

    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class NewMeasurementAdded implements MeasurementEvent {
        public final String id;
        public final Integer value;
        public final Date time;

        @JsonCreator
        NewMeasurementAdded(String id, Integer value, Date time) {
            this.id = id;
            this.value = value;
            this.time = time;
        }
    }

    @Override
    default AggregateEventTagger<MeasurementEvent> aggregateTag() {
        return TAG;
    }
}
