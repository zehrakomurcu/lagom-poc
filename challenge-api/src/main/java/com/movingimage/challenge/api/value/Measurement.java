package com.movingimage.challenge.api.value;

import lombok.Value;

import java.util.Date;

@Value
public class Measurement {
  public Date time;
  public int value;
}
