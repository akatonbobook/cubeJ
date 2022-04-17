package tommy.rubik;

import com.jogamp.opengl.GL2;
import tommy.rubik.cube.Cube;
import tommy.rubik.cube.IDrawable;
import tommy.rubik.cube.RotateDirection;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientCube  extends Cube implements IDrawable {
    private final Client client;
//    private final ClientThread thread;
    public static final float CUBE_SIZE = 3f;

    static Color[] WORLD_COLORS = new Color[] {
            new Color(0x00A800),
            new Color(0x0065FF),
            new Color(0xFF2A00),
            new Color(0xF7931E),
            new Color(0xFFFFFF),
            new Color(0xFFF619)
    };

    private static final float[][] panelPos = {
            { 0, 0,  .5f,   0, 1, 0, 0 },
            { 0, 0, -.5f, 180, 1, 0, 0 },
            {  .5f, 0, 0,  90, 0, 1, 0 },
            { -.5f, 0, 0, -90, 0, 1, 0 },
            { 0,  .5f, 0, -90, 1, 0, 0 },
            { 0, -.5f, 0,  90, 1, 0, 0 }
    };

    private static final float[][] vertex = {
            { -0.48f, -0.48f },
            {  0.48f, -0.48f },
            {  0.48f,  0.48f },
            { -0.48f,  0.48f }
    };

    public ClientCube(Client client) throws IOException {
        super(3);
        this.client = client;
        createCube();
    }

    @Override
    protected void changeN(int n) {
        N = n;
        createCube();
    }

//    private class ClientThread extends Thread {
//        private BufferedReader in;
//        private PrintWriter out;
//
//        public void send(String msg) {
//            out.println(msg);
//        }
//
//        @Override
//        public void run() {
//            try {
//                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                out = new PrintWriter(socket.getOutputStream(), true);
//
//                send("HELLO");
//                String s;
//                while ((s = in.readLine()) != null) {
//                    System.out.println("Received: " + s);
//                    if (s.startsWith("CUBE ")) {
//                        String[] split = s.substring(5).split(" ");
//                        System.out.println(Integer.parseInt(split[0]));
//                        if (N != Integer.parseInt(split[0])) {
//                            changeN(Integer.parseInt(split[0]));
//                        }
//                        applyDataString(split[1]);
//                    } else if (s.equals("BYE")) {
//                        break;
//                    }
//                }
//            } catch (IOException e) {
//                System.out.println("IOException");
//            } finally {
//                try {
//                    in.close();
//                    out.close();
//                    socket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            fireCubeClosed();
//        }
//    }

    private void createCube() {
        parts = new ClientParts[N][N][N];
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
        fireCubeCreated();
    }

    protected class ClientPanel extends Panel implements IDrawable {
        protected ClientPanel(int c) {
            super(c);
        }

        @Override
        public void draw(GL2 gl2) {
            gl2.glBegin(GL2.GL_QUADS);
            gl2.glColor3f(colors[c].getRed()/255f, colors[c].getGreen()/255f, colors[c].getBlue()/255f);
            for(float[] v : vertex)
                gl2.glVertex3f(v[0], v[1], 0);
            gl2.glEnd();
        }
    }

    protected class ClientParts extends Parts implements IDrawable {

        protected ClientParts(Panel f, Panel b, Panel r, Panel l, Panel u, Panel d) {
            super(f, b, r, l, u, d);
        }

        public void rotateX(RotateDirection rd) { }

        public void rotateY(RotateDirection rd) { }

        public void rotateZ(RotateDirection rd) { }

        @Override
        public void draw(GL2 gl2) {
            for (int i=0; i<6; i++) {
                if (panels[i] == null) continue;
                gl2.glPushMatrix();
                gl2.glTranslatef(panelPos[i][0], panelPos[i][1], panelPos[i][2]);
                gl2.glRotatef(panelPos[i][3], panelPos[i][4], panelPos[i][5], panelPos[i][6]);
                ((ClientCube.ClientPanel)panels[i]).draw(gl2);
                gl2.glPopMatrix();
            }
        }
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

    @Override
    public void close() {
        try {
            if (client.threadIsAlive()) {
                client.send("QUIT");
                client.threadJoin();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void draw(GL2 gl2) {
        gl2.glPushMatrix();
        gl2.glTranslatef(-CUBE_SIZE/2, -CUBE_SIZE/2, -CUBE_SIZE/2);
        gl2.glScalef(CUBE_SIZE/N, CUBE_SIZE/N, CUBE_SIZE/N);
        gl2.glTranslatef(0.5f, 0.5f, 0.5f);
        for (int x=0; x<N; x++) {
            for (int y=0; y<N; y++) {
                for (int z=0; z<N; z++) {
                    if (parts[x][y][z] != null) {
                        gl2.glPushMatrix();
                        gl2.glTranslatef(x, y, z);
                        ((ClientCube.ClientParts)parts[x][y][z]).draw(gl2);
                        gl2.glPopMatrix();
                    }
                }
            }
        }
        gl2.glPopMatrix();
    }
}
