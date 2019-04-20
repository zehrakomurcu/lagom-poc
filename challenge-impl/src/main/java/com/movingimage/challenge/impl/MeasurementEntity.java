package com.movingimage.challenge.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.movingimage.challenge.impl.value.Measurement;
import com.movingimage.challenge.impl.value.SensorStatus;

import java.util.Optional;

public class MeasurementEntity extends PersistentEntity<MeasurementCommand, MeasurementEvent, MeasurementState> {

    @Override
    public Behavior initialBehavior(Optional<MeasurementState> snapshotState) {
        /*
         * Set behavioral mechanism
         * If there is no snapshot before then initialize with an empty state
         * If there is then get the current status of the state and process to the related state
         */
        if (!snapshotState.isPresent()) {
            return initial();
        } else {
            SensorStatus status = snapshotState.get().getCurrentStatus();
            if (status.equals(SensorStatus.OK)) {
                return stable(snapshotState.get());
            } else if(status.equals(SensorStatus.WARN)) {
                return warning(snapshotState.get());
            } else if(status.equals(SensorStatus.ALERT)) {
                return alert(snapshotState.get());
            } else {
                return null;
                //todo throw unexpected state error
            }
        }
    }

    /*
     * In this state the entity can get new measurement
     * AND can be processed as stable or warning since its the first command of the entity
     */
    public Behavior initial() {
        BehaviorBuilder b = newBehaviorBuilder(MeasurementState.initial());

        // Command handler for the CreateNewMeasurement command.
        b.setCommandHandler(MeasurementCommand.CreateNewMeasurement.class, (cmd, ctx) ->
                // In response to this command, we want to first persist it as a NewMeasurementAdded event
                ctx.thenPersist(new MeasurementEvent.NewMeasurementAdded(entityId(), cmd.sensorValue, cmd.time),
                        // Then once the event is successfully persisted, we respond with done.
                        evt -> ctx.reply(Done.getInstance())
                )
        );

        // Event handler for the NewMeasurementAdded event.
        // This event changes behavior in terms of the value of sensor
        b.setEventHandlerChangingBehavior(MeasurementEvent.NewMeasurementAdded.class,
            evt -> {
                if(evt.value < 2000) {
                    return stable(MeasurementState.createMeasurement(new Measurement(evt.id, evt.value, evt.time), SensorStatus.OK, 0));
                } else {
                    return warning(MeasurementState.createMeasurement(new Measurement(evt.id, evt.value, evt.time), SensorStatus.WARN, 1));
                }
            });

        return b.build();
    }

    /*
     * In this state entity can handle new measurement and get status commands
     * It is possible to go warning state or stay in the same state
     */
    public Behavior stable(MeasurementState state) {
        BehaviorBuilder b = newBehaviorBuilder(state);

        b.setCommandHandler(MeasurementCommand.CreateNewMeasurement.class, (cmd, ctx) ->
            // In response to this command, we want to first persist it as a NewMeasurementAdded event
            ctx.thenPersist(new MeasurementEvent.NewMeasurementAdded(entityId(), cmd.sensorValue, cmd.time),
                // Then once the event is successfully persisted, we respond with done.
                evt -> ctx.reply(Done.getInstance())
            )
        );

        b.setReadOnlyCommandHandler(MeasurementCommand.GetStatus.class, (cmd, ctx) ->
            ctx.reply(state().getCurrentStatus()));

        b.setEventHandlerChangingBehavior(MeasurementEvent.NewMeasurementAdded.class,
            evt -> {
                if(evt.value < 2000) {
                    return stable(MeasurementState.addNewMeasurement(new Measurement(evt.id, evt.value, evt.time), SensorStatus.OK, state.getWarningCount()));
                } else {
                    return warning(MeasurementState.addNewMeasurement(new Measurement(evt.id, evt.value, evt.time), SensorStatus.WARN, state.getWarningCount()+1));
                }
            });

        return b.build();
    }


    /*
     * In this state entity can handle new measurement and get status commands
     * It is possible to return stable state, remain in the same warning state or go to alert state
     * in terms of previous warning count of the entity
     */
    public Behavior warning(MeasurementState state) {
        BehaviorBuilder b = newBehaviorBuilder(state);

        b.setCommandHandler(MeasurementCommand.CreateNewMeasurement.class, (cmd, ctx) ->
            ctx.thenPersist(new MeasurementEvent.NewMeasurementAdded(entityId(), cmd.sensorValue, cmd.time),
                // Then once the event is successfully persisted, we respond with done.
                evt -> ctx.reply(Done.getInstance())
            )

        );

        b.setReadOnlyCommandHandler(MeasurementCommand.GetStatus.class, (cmd, ctx) ->
            ctx.reply(state().getCurrentStatus()));

        b.setEventHandlerChangingBehavior(MeasurementEvent.NewMeasurementAdded.class,
            evt -> {
                if(evt.value < 2000) {
                    if(state().getWarningCount() == 0)
                        return stable(MeasurementState.addNewMeasurement(new Measurement(evt.id, evt.value, evt.time), SensorStatus.OK, state.getWarningCount()));
                    else
                        return warning(MeasurementState.addNewMeasurement(new Measurement(evt.id, evt.value, evt.time), SensorStatus.WARN, state.getWarningCount() - 1));

                } else {
                    if(state().getWarningCount() == 2) {
                        return alert(MeasurementState.addNewMeasurement(new Measurement(evt.id, evt.value, evt.time), SensorStatus.ALERT, state.getWarningCount() + 1));
                    }
                    else
                        return warning(MeasurementState.addNewMeasurement(new Measurement(evt.id, evt.value, evt.time), SensorStatus.WARN, state.getWarningCount() + 1));
                }
            });

        return b.build();
    }

    /*
     * In this state entity can handle new measurement and get status commands
     * It is possible to go back to stable state or stay in the same state
     */
    public Behavior alert(MeasurementState state) {
        BehaviorBuilder b = newBehaviorBuilder(state);

        b.setCommandHandler(MeasurementCommand.CreateNewMeasurement.class, (cmd, ctx) ->
            ctx.thenPersist(new MeasurementEvent.NewMeasurementAdded(entityId(), cmd.sensorValue, cmd.time),
                // Then once the event is successfully persisted, we respond with done.
                evt -> ctx.reply(Done.getInstance())
            )
        );

        b.setReadOnlyCommandHandler(MeasurementCommand.GetStatus.class, (cmd, ctx) ->
            ctx.reply(state().getCurrentStatus()));

        b.setEventHandlerChangingBehavior(MeasurementEvent.NewMeasurementAdded.class,
            evt -> {
                if(evt.value < 2000) {
                    if(state().getWarningCount() == 1)
                        return stable(MeasurementState.addNewMeasurement(new Measurement(evt.id, evt.value, evt.time), SensorStatus.OK, state.getWarningCount()));
                    else
                        return alert(MeasurementState.addNewMeasurement(new Measurement(evt.id, evt.value, evt.time), SensorStatus.ALERT, state.getWarningCount() - 1));

                } else {
                    return alert(MeasurementState.addNewMeasurement(new Measurement(evt.id, evt.value, evt.time), SensorStatus.ALERT, state.getWarningCount()));
                }
            });

        return b.build();
    }

}
