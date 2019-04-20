package com.movingimage.challenge.impl;

import com.lightbend.lagom.serialization.CompressedJsonable;
import com.movingimage.challenge.impl.value.Measurement;
import com.movingimage.challenge.impl.value.SensorStatus;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;

/**
 * The state for the {@link MeasurementEntity} entity.
 */
@Value
@Builder(toBuilder = true)
public class MeasurementState implements CompressedJsonable {

    private static final long serialVersionUID = 1L;
    private final Optional<Measurement> measurement;
    private SensorStatus currentStatus;
    private int warningCount;

    public static MeasurementState initial() {
        return MeasurementState.builder()
            .measurement(Optional.empty())
            .warningCount(0)
            .build();
    }

    public static MeasurementState createMeasurement(Measurement measurement, SensorStatus status, int warningCount) {
        return MeasurementState.builder()
            .measurement(Optional.of(measurement))
            .currentStatus(status)
            .warningCount(warningCount)
            .build();
    }

    public static MeasurementState addNewMeasurement(Measurement measurement, SensorStatus currentStatus, int warningCount) {
        return MeasurementState.builder()
            .measurement(Optional.of(measurement))
            .currentStatus(currentStatus)
            .warningCount(warningCount)
            .build();
    }


}
