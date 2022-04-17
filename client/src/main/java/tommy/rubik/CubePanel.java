package tommy.rubik;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import tommy.rubik.cube.Face;
import tommy.rubik.cube.RotateDirection;
import tommy.rubik.util.Matrix;
import tommy.rubik.util.Vector;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

import static tommy.rubik.ClientCube.CUBE_SIZE;

public class CubePanel extends GLJPanel implements GLEventListener, MouseMotionListener, MouseListener, MouseWheelListener {
    private float PANEL_WIDTH;
    private float PANEL_HEIGHT;

    public ClientCube cube;

    private final Client client;

    private final GLU glu;
    private float fovY = 30f;
    private float zNear = 1.0f;
    private float zFar = 40.0f;
    private float r = 15f;
    private float theta = (float) Math.PI/2;
    private float phi = (float) Math.PI;
    private float a = 0.007f;
    private float[] u = { 0, 1, 0 };

    private int lastX;
    private int lastY;
    private float[] lastRay;
    private float[] lastCam;

    private float[] collisionPoint;
    private Face collisionFace;
    private boolean grabbing = false;
    private float rotateSense = 0.5f;
    private boolean rotated = false;

    public boolean showAxis = false;
    public boolean showCollision = false;

    public CubePanel(Client client) {
        super(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
        this.client = client;
        glu = new GLU();
        addGLEventListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);
        FPSAnimator animator = new FPSAnimator(60);
        animator.add(this);
        animator.start();
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL2 gl2 = glAutoDrawable.getGL().getGL2();
        glAutoDrawable.setAutoSwapBufferMode(true);
        gl2.glEnable(GL2.GL_DEPTH_TEST);
        gl2.glEnable(GL2.GL_CULL_FACE);
        gl2.glCullFace(GL2.GL_BACK);
        gl2.glClearColor(0f, 0f, 0f, 1f);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL2 gl2 = glAutoDrawable.getGL().getGL2();
        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glLoadIdentity();
        glu.gluLookAt(r*Math.sin(theta)*Math.sin(phi), r*Math.cos(theta), r*Math.sin(theta)*Math.cos(phi), 0, 0, 0, u[0], u[1], u[2]);
        if (showAxis)
            drawAxis(gl2);
        if (showCollision)
            drawCollision(gl2);
        if (cube != null)
            cube.draw(gl2);
        drawChat(gl2);
        glAutoDrawable.swapBuffers();
        client.chats.removeIf(chat -> chat.animation() < -getSize().width);
    }

    protected void drawChat(GL2 gl2) {
        TextRenderer tr = new TextRenderer(new Font(Font.SANS_SERIF, Font.PLAIN, Client.Chat.size), true, true);
        tr.setColor(Color.WHITE);
        tr.beginRendering(getSize().width, getSize().height);
        for (Client.Chat chat : client.chats) {
            tr.draw(chat.text, chat.x, getSize().height-chat.y);
        }
        tr.endRendering();
    }

    protected void drawCollision(GL2 gl2) {
        float delta = 0.2f;
        if (collisionPoint != null) {
            gl2.glLineWidth(4f);
            gl2.glBegin(GL2.GL_LINES);
            gl2.glColor3f(1f, 0f, 1f);
            gl2.glVertex3f(collisionPoint[0]-delta, collisionPoint[1], collisionPoint[2]);
            gl2.glVertex3f(collisionPoint[0]+delta, collisionPoint[1], collisionPoint[2]);
            gl2.glVertex3f(collisionPoint[0], collisionPoint[1]-delta, collisionPoint[2]);
            gl2.glVertex3f(collisionPoint[0], collisionPoint[1]+delta, collisionPoint[2]);
            gl2.glVertex3f(collisionPoint[0], collisionPoint[1], collisionPoint[2]-delta);
            gl2.glVertex3f(collisionPoint[0], collisionPoint[1], collisionPoint[2]+delta);
            gl2.glEnd();
        }
    }

    protected void drawRay(GL2 gl2) {
        if (lastRay != null && lastCam != null) {
            gl2.glLineWidth(2f);
            gl2.glBegin(GL2.GL_LINES);
            gl2.glColor3f(1f, 0f, 1f);
            gl2.glVertex3f(lastCam[0], lastCam[1], lastCam[2]);
            gl2.glVertex3f(lastCam[0]+100*lastRay[0], lastCam[1]+100*lastRay[1], lastCam[2]+100*lastRay[2]);
            gl2.glEnd();
        }
    }

    public static void drawAxis(GL2 gl2) {
        gl2.glLineWidth(2f);
        gl2.glBegin(GL2.GL_LINES);
        gl2.glColor3f(1, 0 ,0 );
        gl2.glVertex3f(0, 0, 0);
        gl2.glVertex3f(10, 0, 0);
        gl2.glColor3f(0, 1 ,0 );
        gl2.glVertex3f(0, 0, 0);
        gl2.glVertex3f(0, 10, 0);
        gl2.glColor3f(0, 0 ,1 );
        gl2.glVertex3f(0, 0, 0);
        gl2.glVertex3f(0, 0, 10);
        gl2.glEnd();
        gl2.glFlush();
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
        GL2 gl2 = glAutoDrawable.getGL().getGL2();
        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glLoadIdentity();
        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glLoadIdentity();
        glu.gluPerspective(fovY, (double) width/(double) height, zNear, zFar);
        PANEL_WIDTH = width;
        PANEL_HEIGHT = height;
    }

    public float[] getCamera() {
        return new float[] {
                (float) (r*Math.sin(theta)*Math.sin(phi)),
                (float) (r*Math.cos(theta)),
                (float) (r*Math.sin(theta)*Math.cos(phi))
        };
    }

    private float[] yn;
    private float[] getRay(int x, int y) {
        float wu = PANEL_WIDTH /2f - x;
        float wv = PANEL_HEIGHT /2f - y;
        float[] c = Vector.getUnitVector(getCamera());
        float[] yAxis = { 0, 1, 0 };
        float[] n = Vector.getCrossProduct(yAxis, u);
        float yk = Vector.getInnerProduct(yAxis, c);
        yn = Vector.getSubtraction(yAxis, Vector.getScalarTimesVector(yk, c));
        System.out.println("yn = " + Arrays.toString(yn) + " , yk = " + yk);
        float t = Vector.getArgument(yn, u);
        if (Vector.getInnerProduct(c, n) < 0)
            t *= -1;
        System.out.println("arg = " + (t*180/Math.PI));
        float[][] wuv = Matrix.getTransposedMatrix(new float[][] { {
                (float) (zNear*Math.tan(Math.toRadians(fovY/2))*2*wu/ PANEL_HEIGHT),
                (float) (zNear*Math.tan(Math.toRadians(fovY/2))*2*wv/ PANEL_HEIGHT),
                zNear,
                1
        } });
        float[][] rod = Matrix.rodriguesRotation(c, t);
        float[][] za = {
                { rod[0][0], rod[0][1], rod[0][2], 0 },
                { rod[1][0], rod[1][1], rod[1][2], 0 },
                { rod[2][0], rod[2][1], rod[2][2], 0 },
                { 0, 0, 0, 1 },
        };
        float[][] xa = Matrix.getXRotate4((float) (Math.PI/2-theta));
        float[][] ya = Matrix.getYRotate4((float) -(Math.PI-phi));
        float[][] tr = Matrix.getTransration4(0f, 0f, -r);
        float[][]  a = Matrix.getProduct(za, Matrix.getProduct(ya, Matrix.getProduct(xa, Matrix.getProduct(tr, wuv))));
        lastCam = getCamera();
        return new float[] { a[0][0] - lastCam[0], a[1][0] - lastCam[1], a[2][0] - lastCam[2] };
    }

    private boolean checkCollision(int x, int y) {
        float[] R = getRay(x, y);
        float[] V = getCamera();

        float A = Vector.getInnerProduct(R, R);
        float B = 2*Vector.getInnerProduct(V, R);
        float C = Vector.getInnerProduct(V, V) - (CUBE_SIZE /2)*(CUBE_SIZE /2)*3;

        if (B*B - 4*A*C > 0) {
            float[] p;
            if (R[0] > 0) {
                p = getCollisionPoint(new float[]{ 1f, 0f, 0f }, new float[]{ -CUBE_SIZE/2, 0f, 0f }, V, R);
                if (p!=null && -CUBE_SIZE/2 < p[1] && p[1] < CUBE_SIZE/2 &&
                        -CUBE_SIZE/2 < p[2] && p[2] < CUBE_SIZE/2) {
                    collisionPoint = new float[] { -CUBE_SIZE/2, p[1], p[2] };
                    collisionFace = Face.LEFT;
                    return true;
                }
            } else {
                p = getCollisionPoint(new float[]{ -1f, 0f, 0f }, new float[]{ CUBE_SIZE/2, 0f, 0f}, V, R);
                if (p!=null && -CUBE_SIZE/2 < p[1] && p[1] < CUBE_SIZE/2 &&
                        -CUBE_SIZE/2 < p[2] && p[2] < CUBE_SIZE/2) {
                    collisionPoint = new float[] { CUBE_SIZE/2, p[1], p[2] };
                    collisionFace = Face.RIGHT;
                    return true;
                }
            }

            if (R[1] > 0) {
                p = getCollisionPoint(new float[]{0f, 1f, 0f}, new float[]{0f, -CUBE_SIZE / 2, 0f}, V, R);
                if (p!=null && -CUBE_SIZE/2 < p[2] && p[2] < CUBE_SIZE/2 &&
                        -CUBE_SIZE/2 < p[0] && p[0] < CUBE_SIZE/2) {
                    collisionPoint = new float[] { p[0], -CUBE_SIZE/2, p[2] };
                    collisionFace = Face.DOWN;
                    return true;
                }
            } else {
                p = getCollisionPoint(new float[]{ 0f, -1f, 0f }, new float[]{ 0f, CUBE_SIZE/2, 0f }, V, R);
                if (p!=null && -CUBE_SIZE/2 < p[2] && p[2] < CUBE_SIZE/2 &&
                        -CUBE_SIZE/2 < p[0] && p[0] < CUBE_SIZE/2) {
                    collisionPoint = new float[] { p[0], CUBE_SIZE/2, p[2] };
                    collisionFace = Face.UP;
                    return true;
                }
            }

            if (R[2] > 0) {
                p = getCollisionPoint(new float[]{ 0f, 0f, 1f }, new float[]{ 0f, 0f, -CUBE_SIZE/2 }, V, R);
                if (p!=null && -CUBE_SIZE/2 < p[0] && p[0] < CUBE_SIZE/2 &&
                        -CUBE_SIZE/2 < p[1] && p[1] < CUBE_SIZE/2) {
                    collisionPoint = new float[] { p[0], p[1], -CUBE_SIZE/2 };
                    collisionFace = Face.BACK;
                    return true;
                }
            } else {
                p = getCollisionPoint(new float[]{0f, 0f, -1f}, new float[]{0f, 0f, CUBE_SIZE / 2}, V, R);
                if (p!=null && -CUBE_SIZE/2 < p[0] && p[0] < CUBE_SIZE/2 &&
                        -CUBE_SIZE/2 < p[1] && p[1] < CUBE_SIZE/2) {
                    collisionPoint = new float[] { p[0], p[1], CUBE_SIZE/2 };
                    collisionFace = Face.FRONT;
                    return true;
                }
            }
        }
        return false;
    }

    private float[] getCollisionPoint(float[] N, float[] O, float[] V, float[] R) {
        float t = (Vector.getInnerProduct(O, N)-Vector.getInnerProduct(V, N))/Vector.getInnerProduct(R,N);
        if (t > 0) {
            float[] p = new float[]{V[0] + t * R[0], V[1] + t * R[1], V[2] + t * R[2]};
            System.out.println(Arrays.toString(p));
            return p;
        }
        return null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!grabbing){
            int x = e.getX();
            int y = e.getY();
            int dx = x - lastX;
            int dy = y - lastY;
            float[] camera = getCamera();
            float[] n = Vector.getUnitVector(Vector.getCrossProduct(camera, u));
            float[] p1 = Vector.rotate(camera, n, dy * a);
            float[] p2 = Vector.rotate(camera, u, -dx * a);
            float[] p = Vector.getAddition(p1, p2);
            theta = (float) Math.atan2(Math.pow(p[0] * p[0] + p[2] * p[2], 0.5), p[1]);
            phi = (float) Math.atan2(p[0], p[2]);
            u = Vector.getUnitVector(Vector.getCrossProduct(n, p));
            lastX = x;
            lastY = y;
        } else if (!rotated) {
            float[] p;
            float[] V = getCamera();
            float[] R = getRay(e.getX(), e.getY());
            if (collisionFace == Face.LEFT) {
                p = getCollisionPoint(new float[]{1f, 0f, 0f}, new float[]{-CUBE_SIZE/2, 0f, 0f}, V, R);
            } else if (collisionFace == Face.RIGHT) {
                p = getCollisionPoint(new float[]{-1f, 0f, 0f}, new float[]{CUBE_SIZE/2, 0f, 0f}, V, R);
            } else if (collisionFace == Face.DOWN) {
                p = getCollisionPoint(new float[]{0f, 1f, 0f}, new float[]{0f, -CUBE_SIZE/2, 0f}, V, R);
            } else if (collisionFace == Face.UP) {
                p = getCollisionPoint(new float[]{0f, -1f, 0f}, new float[]{0f, CUBE_SIZE/2, 0f}, V, R);
            } else if (collisionFace == Face.BACK) {
                p = getCollisionPoint(new float[]{0f, 0f, 1f}, new float[]{0f, 0f, -CUBE_SIZE/2}, V, R);
            } else if (collisionFace == Face.FRONT) {
                p = getCollisionPoint(new float[]{0f, 0f, -1f}, new float[]{0f, 0f, CUBE_SIZE / 2}, V, R);
            } else {
                return;
            }
            if (p == null) return;
            float[] d = { p[0]-collisionPoint[0], p[1]-collisionPoint[1], p[2]-collisionPoint[2] };

            if (d[0] < -rotateSense) {
                if (collisionFace == Face.UP)
                    cube.rotateZLayer((int) ((collisionPoint[2]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.FORWARD);
                else if (collisionFace == Face.DOWN)
                    cube.rotateZLayer((int) ((collisionPoint[2]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.BACKWARD);
                else if (collisionFace == Face.FRONT)
                    cube.rotateYLayer((int) ((collisionPoint[1]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.BACKWARD);
                else if (collisionFace == Face.BACK)
                    cube.rotateYLayer((int) ((collisionPoint[1]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.FORWARD);
                rotated = true;
            } else if (rotateSense < d[0]) {
                if (collisionFace == Face.UP)
                    cube.rotateZLayer((int) ((collisionPoint[2]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.BACKWARD);
                else if (collisionFace == Face.DOWN)
                    cube.rotateZLayer((int) ((collisionPoint[2]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.FORWARD);
                else if (collisionFace == Face.FRONT)
                    cube.rotateYLayer((int) ((collisionPoint[1]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.FORWARD);
                else if (collisionFace == Face.BACK)
                    cube.rotateYLayer((int) ((collisionPoint[1]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.BACKWARD);
                rotated = true;
            } else if (d[1] < -rotateSense) {
                if (collisionFace == Face.RIGHT)
                    cube.rotateZLayer((int) ((collisionPoint[2]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.BACKWARD);
                else if (collisionFace == Face.LEFT)
                    cube.rotateZLayer((int) ((collisionPoint[2]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.FORWARD);
                else if (collisionFace == Face.FRONT)
                    cube.rotateXLayer((int) ((collisionPoint[0]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.FORWARD);
                else if (collisionFace == Face.BACK)
                    cube.rotateXLayer((int) ((collisionPoint[0]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.BACKWARD);
                rotated = true;
            } else if (rotateSense < d[1]) {
                if (collisionFace == Face.RIGHT)
                    cube.rotateZLayer((int) ((collisionPoint[2]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.FORWARD);
                else if (collisionFace == Face.LEFT)
                    cube.rotateZLayer((int) ((collisionPoint[2]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.BACKWARD);
                else if (collisionFace == Face.FRONT)
                    cube.rotateXLayer((int) ((collisionPoint[0]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.BACKWARD);
                else if (collisionFace == Face.BACK)
                    cube.rotateXLayer((int) ((collisionPoint[0]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.FORWARD);
                rotated = true;
            } else if (d[2] < -rotateSense) {
                if (collisionFace == Face.RIGHT)
                    cube.rotateYLayer((int) ((collisionPoint[1]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.FORWARD);
                else if (collisionFace == Face.LEFT)
                    cube.rotateYLayer((int) ((collisionPoint[1]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.BACKWARD);
                else if (collisionFace == Face.UP)
                    cube.rotateXLayer((int) ((collisionPoint[0]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.BACKWARD);
                else if (collisionFace == Face.DOWN)
                    cube.rotateXLayer((int) ((collisionPoint[0]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.FORWARD);
                rotated = true;
            } else if (rotateSense < d[2]) {
                if (collisionFace == Face.RIGHT)
                    cube.rotateYLayer((int) ((collisionPoint[1]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.BACKWARD);
                else if (collisionFace == Face.LEFT)
                    cube.rotateYLayer((int) ((collisionPoint[1]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.FORWARD);
                else if (collisionFace == Face.UP)
                    cube.rotateXLayer((int) ((collisionPoint[0]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.FORWARD);
                else if (collisionFace == Face.DOWN)
                    cube.rotateXLayer((int) ((collisionPoint[0]+CUBE_SIZE/2)/(CUBE_SIZE/cube.getN())), RotateDirection.BACKWARD);
                rotated = true;
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (cube != null) {
            lastX = e.getX();
            lastY = e.getY();
            if (e.getButton() == MouseEvent.BUTTON1) {
                grabbing = checkCollision(e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        grabbing = false;
        rotated = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() > 0)
            r += 0.5f;
        else if (e.getWheelRotation() < 0)
            r -= 0.5f;
        if (r < 4.5f)
            r = 4.5f;
        else if (r > 20f)
            r = 20f;
    }
}
