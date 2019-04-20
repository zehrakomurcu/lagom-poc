package com.movingimage.challenge.impl.value;

import lombok.Value;
import org.joda.time.DateTime;

import java.util.Date;

@Value
public class Measurement {
  public String id;
  public Integer value;
  public Date time;
}
