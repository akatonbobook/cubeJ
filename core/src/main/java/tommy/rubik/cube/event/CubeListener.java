package tommy.rubik.cube.event;

import java.util.EventListener;

public interface CubeListener extends EventListener {
    void cubeClosed(CubeEvent e);
    void cubeCreated(CubeEvent e);
}
