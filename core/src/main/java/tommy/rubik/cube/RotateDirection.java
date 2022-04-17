package tommy.rubik.cube;

public enum RotateDirection {
    FORWARD('+'),
    BACKWARD('-');

    private final char s;
    RotateDirection(char s) {
        this.s = s;
    }

    public char getId() {
        return this.s;
    }

    public static RotateDirection fromId(char id) {
        for (RotateDirection rd: RotateDirection.values()) {
            if (rd.getId() == id) return rd;
        }
        return null;
    }
}
