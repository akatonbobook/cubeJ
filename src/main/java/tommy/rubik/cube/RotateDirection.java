package tommy.rubik.cube;

public enum RotateDirection {
    FORWARD("+"),
    BACKWARD("-");

    private final String s;
    private RotateDirection(String s) {
        this.s = s;
    }

    public String getId() {
        return this.s;
    }
}
