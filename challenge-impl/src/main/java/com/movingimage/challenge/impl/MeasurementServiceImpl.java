package com.movingimage.challenge.impl;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import com.movingimage.challenge.api.MeasurementService;
import com.movingimage.challenge.api.model.*;
import com.movingimage.challenge.api.value.Measurement;
import com.movingimage.challenge.impl.MeasurementCommand.CreateNewMeasurement;
import com.movingimage.challenge.impl.MeasurementCommand.GetStatus;
import com.movingimage.challenge.impl.value.SensorStatus;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Implementation of the MeasurementService.
 */
public class MeasurementServiceImpl implements MeasurementService {
    private final PersistentEntityRegistry persistentEntityRegistry;
    private final CassandraSession session;

    @Inject
    public MeasurementServiceImpl(PersistentEntityRegistry persistentEntityRegistry, CassandraSession session, ReadSide readSide) {
        this.persistentEntityRegistry = persistentEntityRegistry;
        this.session = session;

        persistentEntityRegistry.register(MeasurementEntity.class);
        readSide.register(MeasurementEvtReadSideProcessor.class);
    }


    @Override
    public ServiceCall<NewMeasurementRequest, Done> collectData(UUID id) {
        return request -> {
            PersistentEntityRef<MeasurementCommand> ref = persistentEntityRegistry.refFor(MeasurementEntity.class, id.toString());
            return ref.ask(new CreateNewMeasurement(id.toString(), request.co2, request.time));
        };
    }

    @Override
    public ServiceCall<NotUsed, GetSensorStatusResponse> getSensorStatus(UUID id) {
        return request -> {
            PersistentEntityRef<MeasurementCommand> ref = persistentEntityRegistry.refFor(MeasurementEntity.class, id.toString());
            return ref.ask(new GetStatus(id.toString()))
                .thenApply(sensorStatus -> new GetSensorStatusResponse(sensorStatus.name()));

        };
    }

    @Override
    public ServiceCall<NotUsed, GetSensorMetricsResponse > getSensorMetrics(UUID id) {
        return req -> {
            GetSensorMetricsResponse resp = new GetSensorMetricsResponse();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);

            return session.selectAll("SELECT avg(value), max(value) FROM measurement WHERE sensorId = ? AND ts < ?",
                id.toString(), cal.getTime())
                .thenApply(row -> {
                    resp.setAvgLast30Days(row.get(0).getInt(0));
                    resp.setMaxLast30Days(row.get(0).getInt(1));
                    return resp;
                });
        };
    }

    @Override
    public ServiceCall<NotUsed, GetAlertsResponse> listAlerts(UUID id) {
        return request -> {
            /*
             * First ask the current state of queried sensor
             * If the current state is ALERT, then the last 3 value will be the measurements caused to alert
             * If the current state is NOT ALERT, then there will be an empty result
             * Respond with an empty result in case of errors while getting the current status
             */
            PersistentEntityRef<MeasurementCommand> ref = persistentEntityRegistry.refFor(MeasurementEntity.class, id.toString());
            try {
                SensorStatus sensorStatus = ref.ask(new GetStatus(id.toString())).toCompletableFuture().get();
                if (!sensorStatus.equals(SensorStatus.ALERT))
                    return CompletableFuture.completedFuture(new GetAlertsResponse());
                else {
                    return session.selectAll("SELECT sensorId, value, ts FROM measurement WHERE sensorId = ? ORDER BY ts DESC LIMIT 3", id.toString())
                        .thenApply(rows -> rows.stream()
                            .map(row -> new Measurement(row.getTimestamp("ts"), row.getInt("value")))
                            .collect(Collectors.toList()))
                        .thenApply(measurements -> {
                            Date startTime = measurements.get(0).time;
                            Date endTime = measurements.get(2).time;
                            int measurement1 = measurements.get(2).value;
                            int measurement2 = measurements.get(1).value;
                            int measurement3 = measurements.get(0).value;

                            return new GetAlertsResponse(startTime, endTime, measurement1, measurement2, measurement3);
                        });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return CompletableFuture.completedFuture(new GetAlertsResponse());
            } catch (ExecutionException e) {
                e.printStackTrace();
                return CompletableFuture.completedFuture(new GetAlertsResponse());
            }
        };
    }

}
