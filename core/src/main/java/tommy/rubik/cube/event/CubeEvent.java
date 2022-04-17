package tommy.rubik.cube.event;

import java.util.EventObject;

public class CubeEvent extends EventObject {

    /**
     * Constructs a prototypical Event.
     *
     * @param source the object on which the Event initially occurred
     * @throws IllegalArgumentException if source is null
     */
    public CubeEvent(Object source) {
        super(source);
    }
}
