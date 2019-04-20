package com.movingimage.challenge.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Immutable
@JsonDeserialize
@NoArgsConstructor
@Setter
public class GetSensorMetricsResponse {
  public int maxLast30Days;
  public double avgLast30Days;
}
