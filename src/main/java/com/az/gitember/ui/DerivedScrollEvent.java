package com.az.gitember.ui;

import javafx.beans.NamedArg;
import javafx.event.Event;
import javafx.event.EventType;

/**
 * Created by Igor_Azarny on 17 dec 2016.
 */
public class DerivedScrollEvent extends Event {

    public DerivedScrollEvent(@NamedArg("eventType") EventType<? extends Event> eventType) {
        super(eventType);
    }

}
