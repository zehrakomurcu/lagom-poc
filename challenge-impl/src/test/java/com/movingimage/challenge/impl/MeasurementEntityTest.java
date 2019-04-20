package com.movingimage.challenge.impl;

import akka.Done;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;

import com.movingimage.challenge.impl.MeasurementCommand.Hello;
import com.movingimage.challenge.impl.MeasurementCommand.UseGreetingMessage;

import static org.junit.Assert.assertEquals;

public class MeasurementEntityTest {
    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("MeasurementEntityTest");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testChallengeEntity() {
        PersistentEntityTestDriver<MeasurementCommand, MeasurementEvent, MeasurementState> driver = new PersistentEntityTestDriver<>(system,
                new MeasurementEntity(), "world-1");

        Outcome<MeasurementEvent, MeasurementState> outcome1 = driver.run(new Hello("Alice"));
        assertEquals("Hello, Alice!", outcome1.getReplies().get(0));
        assertEquals(Collections.emptyList(), outcome1.issues());

        Outcome<MeasurementEvent, MeasurementState> outcome2 = driver.run(new UseGreetingMessage("Hi"),
                new Hello("Bob"));
        assertEquals(1, outcome2.events().size());
        assertEquals(new MeasurementEvent.NewMeasurementAdded("world-1", "Hi"), outcome2.events().get(0));
        assertEquals("Hi", outcome2.state().message);
        assertEquals(Done.getInstance(), outcome2.getReplies().get(0));
        assertEquals("Hi, Bob!", outcome2.getReplies().get(1));
        assertEquals(2, outcome2.getReplies().size());
        assertEquals(Collections.emptyList(), outcome2.issues());
    }
}
