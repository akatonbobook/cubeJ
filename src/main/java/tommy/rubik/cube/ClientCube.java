package tommy.rubik.cube;

public class ClientCube extends Cube3D {
    private final IClient client;

    public ClientCube(int n, String data, IClient client) {
        super(n, data);
        this.client = client;
        createCube();
    }

    private void createCube() {
        for (int x=0; x<N; x++) {
            for (int y=0; y<N; y++) {
                for (int z=0; z<N; z++) {
                    if (x == 0 || x == N-1 || y == 0 || y == N-1 || z == 0 || z == N-1)
                        parts[x][y][z] = new ClientParts(
                                z == N-1 ? new ClientPanel(0) : null,
                                z ==   0 ? new ClientPanel(1) : null,
                                x == N-1 ? new ClientPanel(2) : null,
                                x ==   0 ? new ClientPanel(3) : null,
                                y == N-1 ? new ClientPanel(4) : null,
                                y ==   0 ? new ClientPanel(5) : null
                        );
                    else
                        parts[x][y][z] = null;
                }
            }
        }
    }

    protected class ClientPanel extends Panel3D {
        protected ClientPanel(int c) {
            super(c);
        }
    }

    protected class ClientParts extends Parts3D {

        protected ClientParts(Panel f, Panel b, Panel r, Panel l, Panel u, Panel d) {
            super(f, b, r, l, u, d);
        }

        @Override
        public void rotateX(RotateDirection rd) { }

        @Override
        public void rotateY(RotateDirection rd) { }

        @Override
        public void rotateZ(RotateDirection rd) { }
    }

    @Override
    public void rotateXLayer(int x, RotateDirection rd) {
        client.send("ROTA X" + rd.getId() + x);
    }

    @Override
    public void rotateYLayer(int y, RotateDirection rd) {
        client.send("ROTA Y" + rd.getId() + y);
    }

    @Override
    public void rotateZLayer(int z, RotateDirection rd) {
        client.send("ROTA Z" + rd.getId() + z);
    }
}
