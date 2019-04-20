package com.movingimage.challenge.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.AllArgsConstructor;

@Immutable
@JsonDeserialize
@AllArgsConstructor
public class GetSensorStatusResponse {
  public String status;
}
