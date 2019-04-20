package com.movingimage.challenge.impl;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

import com.movingimage.challenge.api.MeasurementService;

/**
 * The module that binds the MeasurementService so that it can be served.
 */
public class MeasurementModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(MeasurementService.class, MeasurementServiceImpl.class);
    }
}
