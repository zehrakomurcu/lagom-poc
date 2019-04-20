package com.movingimage.challenge.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jdk.nashorn.internal.ir.annotations.Immutable;

import java.util.Date;

@Immutable
@JsonDeserialize
public class NewMeasurementRequest {
  public Integer co2;

  @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=("yyyy-MM-dd HH:mm:ss"))
  public Date time;
}

