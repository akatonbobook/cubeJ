package tommy.rubik.cube;

import com.jogamp.opengl.GL2;

public abstract class Cube3D extends Cube implements IDrawable {

    protected float CUBE_SIZE = 3f;

    private static final float[][] panelPos = {
            { 0, 0,  .5f,   0, 0, 0, 0 },
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

    public Cube3D(int n) {
        super(n);
    }

    public Cube3D(int n, String data) {
        super(n, data);
    }

    protected abstract class Panel3D extends Panel implements IDrawable {

        protected Panel3D(int c) {
            super(c);
        }

        @Override
        public void draw(GL2 gl2) {
            gl2.glColor3f(colors[c].getRed()/255f, colors[c].getGreen()/255f, colors[c].getBlue()/255f);
            gl2.glBegin(GL2.GL_QUADS);
            for(float[] v : vertex)
                gl2.glVertex3f(v[0], v[1], v[2]);
            gl2.glEnd();
        }
    }

    protected abstract class Parts3D extends Parts implements IDrawable {

        protected Parts3D(Panel f, Panel b, Panel r, Panel l, Panel u, Panel d) {
            super(f, b, r, l, u, d);
        }

        @Override
        public void draw(GL2 gl2) {
            for (int i=0; i<6; i++) {
                if (panels[i] == null) continue;
                gl2.glPushMatrix();
                gl2.glTranslatef(panelPos[i][0], panelPos[i][1], panelPos[i][2]);
                gl2.glRotatef(panelPos[i][3], panelPos[i][4], panelPos[i][5], panelPos[i][6]);
                ((Panel3D)panels[i]).draw(gl2);
                gl2.glPopMatrix();
            }
        }
    }

    @Override
    public void draw(GL2 gl2) {
        gl2.glTranslatef(-CUBE_SIZE/2f, -CUBE_SIZE/2f, -CUBE_SIZE/2f);
        gl2.glScalef(CUBE_SIZE/N, CUBE_SIZE/N, CUBE_SIZE/N);
        gl2.glTranslatef(.5f, .5f, .5f);
        gl2.glPushMatrix();
        for (int x=0; x<N; x++) {
            for (int y=0; y<N; y++) {
                for (int z=0; z<N; z++) {
                    if (parts[x][y][z] == null) continue;
                    gl2.glPushMatrix();
                    gl2.glTranslatef(x, y, z);
                    ((Parts3D)parts[x][y][z]).draw(gl2);
                    gl2.glPopMatrix();
                }
            }
        }
        gl2.glPopMatrix();
    }
}
