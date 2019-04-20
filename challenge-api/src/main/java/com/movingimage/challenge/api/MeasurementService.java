package com.movingimage.challenge.api;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.broker.kafka.KafkaProperties;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import com.lightbend.lagom.javadsl.api.transport.Method;
import com.movingimage.challenge.api.model.GetAlertsResponse;
import com.movingimage.challenge.api.model.GetSensorMetricsResponse;
import com.movingimage.challenge.api.model.GetSensorStatusResponse;
import com.movingimage.challenge.api.model.NewMeasurementRequest;
import org.pcollections.PSequence;

import java.util.UUID;

import static com.lightbend.lagom.javadsl.api.Service.*;

/**
 * The challenge service interface.
 * <p>
 * This describes everything that Lagom needs to know about how to serve and
 * consume the MeasurementService.
 */
public interface MeasurementService extends Service {
    /**
     * Service call is used to collect the data
     * @param id
     * @return done
     */
    ServiceCall<NewMeasurementRequest, Done> collectData(UUID id);

    /**
     * Service call is used to get specified sensor's last status
     * @param id
     * @return status which might be one of the followings: OK, WARN, ALERT
     */
    ServiceCall<NotUsed, GetSensorStatusResponse> getSensorStatus(UUID id);

    /**
     * Service call is used to get specified sensor's metrics
     */
    ServiceCall<NotUsed, GetSensorMetricsResponse> getSensorMetrics(UUID id);

    /**
     * Service call is used to get specified sensor's alert history
     * @param id
     * @return
     */
    ServiceCall<NotUsed, GetAlertsResponse> listAlerts(UUID id);


    @Override
    default Descriptor descriptor() {
        return named("challenge")
                .withCalls(
                        Service.restCall(Method.POST,"/api/v1/sensors/:id/measurements", this::collectData),
                        Service.restCall(Method.GET,"/api/v1/sensors/:id", this::getSensorStatus),
                        Service.restCall(Method.GET,"/api/v1/sensors/:id/metrics", this::getSensorMetrics),
                        Service.restCall(Method.GET,"/api/v1/sensors/:id/alerts", this::listAlerts)
                )
                .withPathParamSerializer(UUID.class, PathParamSerializers.required("UUID", UUID::fromString, UUID::toString))
                .withAutoAcl(true);
    }
}
