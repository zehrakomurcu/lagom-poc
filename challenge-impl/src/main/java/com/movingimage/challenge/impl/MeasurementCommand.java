package com.movingimage.challenge.impl;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import com.movingimage.challenge.api.model.GetAlertsResponse;
import com.movingimage.challenge.api.model.GetSensorMetricsResponse;
import com.movingimage.challenge.impl.value.SensorStatus;
import lombok.Value;
import org.joda.time.DateTime;

import java.util.Date;

/**
 * This interface defines all the commands that the MeasurementEntity supports.
 * <p>
 * By convention, the commands should be inner classes of the interface, which
 * makes it simple to get a complete picture of what commands an entity
 * supports.
 */
public interface MeasurementCommand extends Jsonable {
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class CreateNewMeasurement  implements MeasurementCommand, PersistentEntity.ReplyType<Done> {
        public final String sensorId;
        public final Integer sensorValue;
        public final Date time;

        @JsonCreator
        CreateNewMeasurement(String sensorId, Integer sensorValue, Date time) {
            this.sensorId = Preconditions.checkNotNull(sensorId, "sensorId");
            this.sensorValue = Preconditions.checkNotNull(sensorValue, "sensorValue");
            this.time = Preconditions.checkNotNull(time, "time");
        }
    }


    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class GetStatus implements MeasurementCommand, PersistentEntity.ReplyType<SensorStatus> {
        public final String sensorId;

        @JsonCreator
        GetStatus(String sensorId) {
            this.sensorId = Preconditions.checkNotNull(sensorId, "sensorId");
        }
    }

    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class GetAlerts implements MeasurementCommand, PersistentEntity.ReplyType<GetAlertsResponse> {
        public final String sensorId;

        @JsonCreator
        GetAlerts(String sensorId) {
            this.sensorId = Preconditions.checkNotNull(sensorId, "sensorId");
        }
    }
}
