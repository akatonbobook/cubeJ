package tommy.rubik.cube;

import java.awt.*;

public abstract class Cube implements ICube {

    protected final Color[] colors;
    protected final int N;
    protected Parts[][][] parts;

    public Cube(int n) {
        colors = WORLD_COLORS;
        N = n;
        parts = new Parts[N][N][N];
    }

    public Cube(int n, String data) {
        this(n);
        applyDataString(data);
    }

    protected abstract class Panel {
        protected int c;
        protected Panel(int c) {
            this.c = c;
        }
    }

    protected abstract class Parts implements IParts {
        protected final Panel[] panels;
        protected Parts(Panel f, Panel b, Panel r, Panel l, Panel u, Panel d) {
            panels = new Panel[] { f, b, r, l, u, d };
        }
    }

    public void applyDataString(final String data) {
        int idx = 0;
        for (int x=0; x<N; x++) {
            for (int y=0; y<N; y++) {
                for (int z=0; z<N; z++) {
                    if (z == N-1) parts[x][y][z].panels[0].c = Character.getNumericValue(data.charAt(idx++));
                    if (z ==   0) parts[x][y][z].panels[1].c = Character.getNumericValue(data.charAt(idx++));
                    if (x == N-1) parts[x][y][z].panels[2].c = Character.getNumericValue(data.charAt(idx++));
                    if (x ==   0) parts[x][y][z].panels[3].c = Character.getNumericValue(data.charAt(idx++));
                    if (y == N-1) parts[x][y][z].panels[4].c = Character.getNumericValue(data.charAt(idx++));
                    if (y ==   0) parts[x][y][z].panels[5].c = Character.getNumericValue(data.charAt(idx++));
                }
            }
        }
    }

    public String toDataString() {
        StringBuilder sb = new StringBuilder(" ");
        for (int x=0; x<N; x++) {
            for (int y=0; y<N; y++) {
                for (int z=0; z<N; z++) {
                    if (z == N-1) sb.append(parts[x][y][z].panels[0].c);
                    if (z ==   0) sb.append(parts[x][y][z].panels[0].c);
                    if (x == N-1) sb.append(parts[x][y][z].panels[0].c);
                    if (x ==   0) sb.append(parts[x][y][z].panels[0].c);
                    if (y == N-1) sb.append(parts[x][y][z].panels[0].c);
                    if (y ==   0) sb.append(parts[x][y][z].panels[0].c);
                }
            }
        }
        return sb.toString();
    }

    public void changeColor(Face face, Color color) {
        colors[face.getId()] = color;
    }
}
