package tommy.rubik.util;

public class Vector {
    public static float getInnerProduct(float[] a, float[] b){
        if (a.length != b.length) throw new IllegalArgumentException("ベクトルの次元が異なります");
        float ip = 0;
        for (int i=0; i<a.length; i++)
            ip += a[i]*b[i];
        return ip;
    }

    public static float[] getCrossProduct(float[] a, float[] b) {
        if (a.length != 3 || b.length != 3) throw new IllegalArgumentException("次元が3ではありません");
        return new float[] { a[1]*b[2]-a[2]*b[1], a[2]*b[0]-a[0]*b[2], a[0]*b[1]-a[1]*b[0] };
    }

    public static float getMagnitude(float[] a) {
        float m = 0;
        for (float e : a)
            m += e*e;
        return (float) Math.pow(m, 0.5);
    }

    public static float getArgument(float[] a, float[] b) {
        float ip = getInnerProduct(a, b);
        float ma = getMagnitude(a);
        float mb = getMagnitude(b);
        return (float) Math.acos(ip/(ma*mb));
    }

    public static float[] getUnitVector(float[] a) {
        float m = getMagnitude(a);
        return new float[] { a[0]/m, a[1]/m, a[2]/m };
    }

    public static float[] rotate(float[] a, float[] n, float theta) {
        float[][] rotateMatrix = Matrix.rodriguesRotation(n, theta);
        return (Matrix.getTransposedMatrix(Matrix.getProduct(rotateMatrix, Matrix.getTransposedMatrix(new float[][] { a }))))[0];
    }

    public static float[] getWeightedAverage(float a, float[] A, float b, float[] B) {
        if (A.length != B.length) throw new IllegalArgumentException();
        float[] C = new float[A.length];
        for (int i=0; i<A.length; i++)
            C[i] = (a*A[i] + b*B[i])/(a+b);
        return C;
    }

    public static float[] getSubtraction(float[] a, float[] b) {
        if (a.length != b.length) throw new IllegalArgumentException();
        float[] C = new float[a.length];
        for (int i=0; i<a.length; i++)
            C[i] = a[i] - b[i];
        return C;
    }

    public static float[] getAddition(float[] a, float[] b) {
        if (a.length != b.length) throw new IllegalArgumentException();
        float[] c = new float[a.length];
        for (int i=0; i<a.length; i++)
            c[i] = a[i] + b[i];
        return c;
    }

    public static float[] getScalarTimesVector(float k, float[] a) {
        float[] C = new float[a.length];
        for (int i=0; i<a.length; i++)
            C[i] = k * a[i];
        return C;
    }
}
