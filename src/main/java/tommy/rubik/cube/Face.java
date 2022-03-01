package tommy.rubik.cube;

public enum Face {
    FRONT(0),
    BACK(1),
    RIGHT(2),
    LEFT(3),
    UP(4),
    DOWN(5);

    private final int id;
    private Face(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
