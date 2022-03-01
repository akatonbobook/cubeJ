package tommy.rubik.cube;

import java.awt.*;

public interface ICube {

    static Color[] WORLD_COLORS = new Color[] {
            new Color(0x00A800),
            new Color(0x0065FF),
            new Color(0xFF2A00),
            new Color(0xF7931E),
            new Color(0xFFFFFF),
            new Color(0xFFF619)
    };

    void rotateXLayer(int x, RotateDirection rd);
    void rotateYLayer(int y, RotateDirection rd);
    void rotateZLayer(int z, RotateDirection rd);
}
