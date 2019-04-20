package com.movingimage.challenge.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize
public class GetAlertsResponse {
  public Date startTime;
  public Date endTime;
  public int measurement1;
  public int measurement2;
  public int measurement3;
}
