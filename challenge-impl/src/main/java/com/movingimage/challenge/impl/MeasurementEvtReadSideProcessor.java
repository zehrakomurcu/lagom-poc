package com.movingimage.challenge.impl;

import akka.Done;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import org.pcollections.PSequence;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide.completedStatement;

public class MeasurementEvtReadSideProcessor extends ReadSideProcessor<MeasurementEvent> {

  private final CassandraSession db;
  private final CassandraReadSide readSide;

  private PreparedStatement insertMeasurement = null; // initialized in prepare

  @Inject
  public MeasurementEvtReadSideProcessor(CassandraSession db, CassandraReadSide readSide) {
    this.db = db;
    this.readSide = readSide;
  }

  @Override
  public ReadSideHandler<MeasurementEvent> buildHandler() {
    return readSide.<MeasurementEvent>builder("measurements-offset")
        .setGlobalPrepare(this::createTable)
        .setPrepare((tag) -> prepareInsertMeasurement())
        .setEventHandler(MeasurementEvent.NewMeasurementAdded.class, this::processNewMeasurement)
        .build();
  }

  @Override
  public PSequence<AggregateEventTag<MeasurementEvent>> aggregateTags() {
    return MeasurementEvent.TAG.allTags();
  }

  private CompletionStage<Done> createTable() {
    return db.executeCreateTable(
        "CREATE TABLE IF NOT EXISTS measurement ("
            + "sensorId text, ts timestamp, value int, "
            + "PRIMARY KEY(sensorId, ts)) WITH CLUSTERING ORDER BY (ts DESC)");
  }

  private CompletionStage<Done> prepareInsertMeasurement() {
    return db.prepare("UPDATE measurement SET value = ? WHERE sensorId = ? and ts = ?").thenApply(ps -> {
      this.insertMeasurement = ps;
      return Done.getInstance();
    });
  }

  private CompletionStage<List<BoundStatement>> processNewMeasurement(MeasurementEvent.NewMeasurementAdded event) {
    LocalDateTime localDate = event.time.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime();
    Timestamp ts = Timestamp.valueOf(localDate);

    BoundStatement bindInsertMeasurement = insertMeasurement.bind();
    bindInsertMeasurement.setString("sensorId", event.id);
    bindInsertMeasurement.setTimestamp("ts", ts);
    bindInsertMeasurement.setInt("value", event.value);

    return completedStatement(bindInsertMeasurement);
  }

}
