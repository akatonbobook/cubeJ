package tommy.rubik.cube;

import tommy.rubik.cube.event.CubeEvent;
import tommy.rubik.cube.event.CubeListener;

import javax.swing.event.EventListenerList;
import java.awt.*;

public abstract class Cube implements ICube {

    protected final Color[] colors;
    protected int N;
    protected Parts[][][] parts;

    private final EventListenerList cubeListenerList;

    public Cube() {
        this(3);
    }

    public Cube(int n) {
        colors = WORLD_COLORS;
        N = n;
        cubeListenerList = new EventListenerList();
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

    protected abstract class Parts {
        protected final Panel[] panels;
        protected Parts(Panel f, Panel b, Panel r, Panel l, Panel u, Panel d) {
            panels = new Panel[] { f, b, r, l, u, d };
        }
        public abstract void rotateX(RotateDirection rd);
        public abstract void rotateY(RotateDirection rd);
        public abstract void rotateZ(RotateDirection rd);
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

    public void addCubeListener(CubeListener listener) {
        cubeListenerList.add(CubeListener.class, listener);
    }

    public void removeCubeListener(CubeListener listener) {
        cubeListenerList.remove(CubeListener.class, listener);
    }

    public void fireCubeClosed() {
        CubeEvent e = new CubeEvent(this);
        for (CubeListener l : cubeListenerList.getListeners(CubeListener.class)) {
            l.cubeClosed(e);
        }
    }

    public void fireCubeCreated() {
        CubeEvent e = new CubeEvent(this);
        for (CubeListener l : cubeListenerList.getListeners(CubeListener.class)) {
            l.cubeCreated(e);
        }
    }

    public String toDataString() {
        StringBuilder sb = new StringBuilder();
        for (int x=0; x<N; x++) {
            for (int y=0; y<N; y++) {
                for (int z=0; z<N; z++) {
                    if (z == N-1) sb.append(parts[x][y][z].panels[0].c);
                    if (z ==   0) sb.append(parts[x][y][z].panels[1].c);
                    if (x == N-1) sb.append(parts[x][y][z].panels[2].c);
                    if (x ==   0) sb.append(parts[x][y][z].panels[3].c);
                    if (y == N-1) sb.append(parts[x][y][z].panels[4].c);
                    if (y ==   0) sb.append(parts[x][y][z].panels[5].c);
                }
            }
        }
        return sb.toString();
    }

    abstract protected void changeN(int n);

    public int getN() {
        return N;
    }

    public abstract void rotateXLayer(int x, RotateDirection rd);
    public abstract void rotateYLayer(int y, RotateDirection rd);
    public abstract void rotateZLayer(int z, RotateDirection rd);

    public void changeColor(Face face, Color color) {
        colors[face.getId()] = color;
    }
}
