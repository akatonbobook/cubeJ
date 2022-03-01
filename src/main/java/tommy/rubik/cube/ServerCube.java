package tommy.rubik.cube;

public class ServerCube extends Cube {

    ServerCube(int n) {
        super(n);
        createCube();
    }

    private void createCube() {
        for (int x=0; x<N; x++) {
            for (int y=0; y<N; y++) {
                for (int z=0; z<N; z++) {
                    if (x == 0 || x == N-1 || y == 0 || y == N-1 || z == 0 || z == N-1)
                        parts[x][y][z] = new ServerParts(
                                z == N-1 ? new ServerPanel(0) : null,
                                z ==   0 ? new ServerPanel(1) : null,
                                x == N-1 ? new ServerPanel(2) : null,
                                x ==   0 ? new ServerPanel(3) : null,
                                y == N-1 ? new ServerPanel(4) : null,
                                y ==   0 ? new ServerPanel(5) : null
                        );
                    else
                        parts[x][y][z] = null;
                }
            }
        }
    }

    protected class ServerPanel extends Panel {
        protected ServerPanel(int c) {
            super(c);
        }
    }

    protected class ServerParts extends Parts {
        protected ServerParts(Panel f, Panel b, Panel r, Panel l, Panel u, Panel d) {
            super(f, b, r, l, u, d);
        }

        @Override
        public void rotateX(RotateDirection rd) {
            Panel p = panels[0];
            if (rd == RotateDirection.FORWARD) {
                panels[0] = panels[4];
                panels[4] = panels[1];
                panels[1] = panels[5];
                panels[5] = p;
            } else {
                panels[0] = panels[5];
                panels[5] = panels[1];
                panels[1] = panels[4];
                panels[4] = p;
            }
        }

        @Override
        public void rotateY(RotateDirection rd) {
            Panel p = panels[0];
            if (rd == RotateDirection.FORWARD) {
                panels[0] = panels[3];
                panels[3] = panels[1];
                panels[1] = panels[2];
                panels[2] = p;
            } else {
                panels[0] = panels[2];
                panels[2] = panels[1];
                panels[1] = panels[3];
                panels[3] = p;
            }
        }

        @Override
        public void rotateZ(RotateDirection rd) {
            Panel p = panels[2];
            if (rd == RotateDirection.FORWARD) {
                panels[2] = panels[5];
                panels[5] = panels[3];
                panels[3] = panels[4];
                panels[4] = p;
            } else {
                panels[2] = panels[4];
                panels[4] = panels[3];
                panels[3] = panels[5];
                panels[5] = p;
            }
        }
    }

    @Override
    public void rotateXLayer(int x, RotateDirection rd) {
        Parts p;
        for (int r=0; r<N/2; r++) {
            for (int i=r; i<N-r-1; i++) {
                p = parts[x][i][r];
                if (rd == RotateDirection.FORWARD) {
                    parts[x][i][r] = parts[x][r][N-i-1];
                    parts[x][r][N-i-1] = parts[x][N-i-1][N-r-1];
                    parts[x][N-i-1][N-r-1] = parts[x][N-r-1][i];
                    parts[x][N-r-1][i] = p;
                } else {
                    parts[x][i][r] = parts[x][N-r-1][i];
                    parts[x][N-r-1][i] = parts[x][N-i-1][N-r-1];
                    parts[x][N-i-1][N-r-1] = parts[x][r][N-i-1];
                    parts[x][r][N-i-1] = p;
                }
                parts[x][i][r].rotateX(rd);
                parts[x][r][N-i-1].rotateX(rd);
                parts[x][N-i-1][N-r-1].rotateX(rd);
                parts[x][r][N-i-1].rotateX(rd);
            }
            if (x != 0 && x != N-1) break;
        }
    }

    @Override
    public void rotateYLayer(int y, RotateDirection rd) {
        Parts p;
        for (int r=0; r<N/2; r++) {
            for (int i=r; i<N-r-1; i++) {
                p = parts[r][y][i];
                if (rd == RotateDirection.FORWARD) {
                    parts[r][y][i] = parts[N-i-1][y][r];
                    parts[N-i-1][y][r] = parts[N-r-1][y][N-i-1];
                    parts[N-r-1][y][N-i-1] = parts[i][y][N-r-1];
                    parts[i][y][N-r-1] = p;
                } else {
                    parts[r][y][i] = parts[i][y][N-r-1];
                    parts[i][y][N-r-1] = parts[N-r-1][y][N-i-1];
                    parts[N-r-1][y][N-i-1] = parts[N-i-1][y][r];
                    parts[N-i-1][y][r] = p;
                }
                parts[r][y][i].rotateY(rd);
                parts[N-i-1][y][r].rotateY(rd);
                parts[N-r-1][y][N-i-1].rotateY(rd);
                parts[i][y][N-r-1].rotateY(rd);
            }
            if (y != 0 && y != N-1) break;
        }
    }

    @Override
    public void rotateZLayer(int z, RotateDirection rd) {
        Parts p;
        for (int r=0; r<N/2; r++) {
            for (int i=0; i<N-r-1; i++) {
                p = parts[i][r][z];
                if (rd == RotateDirection.FORWARD) {
                    parts[i][r][z] = parts[r][N-i-1][z];
                    parts[r][N-i-1][z] = parts[N-i-1][N-r-1][z];
                    parts[N-i-1][N-r-1][z] = parts[N-r-1][i][z];
                    parts[N-r-1][i][z] = p;
                } else {
                    parts[i][r][z] = parts[N-r-1][i][z];
                    parts[N-r-1][i][z] = parts[N-i-1][N-r-1][z];
                    parts[N-i-1][N-r-1][z] = parts[r][N-i-1][z];
                    parts[r][N-i-1][z] = p;
                }
                parts[i][r][z].rotateZ(rd);
                parts[r][N-i-1][z].rotateZ(rd);
                parts[N-i-1][N-r-1][z].rotateZ(rd);
                parts[N-r-1][i][z].rotateZ(rd);
            }
            if (z != 0 && z != N-1) break;
        }
    }
}
