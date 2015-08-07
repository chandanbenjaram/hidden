package co.samepinch.android.app.helpers.pubsubs;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by cbenjaram on 7/15/15.
 */
public enum BusProvider {
    INSTANCE;
    private final Bus bus;

    private BusProvider(){
        bus = new Bus(ThreadEnforcer.ANY);
    }

    public Bus getBus() {
        return bus;
    }
}
