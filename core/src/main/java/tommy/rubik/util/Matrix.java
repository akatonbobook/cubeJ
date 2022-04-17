package tommy.rubik.util;

public class Matrix {

    public static float[][] getProduct(float[][] A, float[][] B) {
        if (A[0].length != B.length) throw new IllegalArgumentException();
        float[][] C = new float[A.length][B[0].length];
        for (int i=0; i<A.length; i++) {
            for (int j=0; j<B[0].length; j++) {
                C[i][j] = 0;
                for (int k=0; k<A[0].length; k++) {
                    C[i][j] += A[i][k]*B[k][j];
                }
            }
        }
        return C;
    }

    public static float[][] rodriguesRotation(float[] n, float theta) {
        return new float[][] {
                { (float) (Math.cos(theta)+n[0]*n[0]*(1-Math.cos(theta))), (float) (n[0]*n[1]*(1-Math.cos(theta))-n[2]*Math.sin(theta)), (float) (n[2]*n[0]*(1-Math.cos(theta))+n[1]*Math.sin(theta))},
                { (float) (n[0]*n[1]*(1-Math.cos(theta))+n[2]*Math.sin(theta)), (float) (Math.cos(theta)+n[1]*n[1]*(1-Math.cos(theta))), (float) (n[1]*n[2]*(1-Math.cos(theta))-n[0]*Math.sin(theta))},
                { (float) (n[2]*n[0]*(1-Math.cos(theta))-n[1]*Math.sin(theta)), (float) (n[1]*n[2]*(1-Math.cos(theta))+n[0]*Math.sin(theta)), (float) (Math.cos(theta)+n[2]*n[2]*(1-Math.cos(theta)))}
        };
    }

    public static float[][] getTransposedMatrix(float[][] A) {
        float[][] B = new float[A[0].length][A.length];
        for (int i=0; i<A.length; i++) {
            for (int j=0; j<A[0].length; j++) {
                B[j][i] = A[i][j];
            }
        }
        return B;
    }

    public static float getCofactor(float[][] matrix, int i, int j) {
        float[][] CM = getSubMatrix(matrix, i, j);
        if ((i+j)%2 == 0) return getDeterminant(CM);
        else return -getDeterminant(CM);
    }

    public static float[][] getCofactorMatrix(float[][] matrix) {
        float[][] CM = new float[matrix.length][matrix[0].length];
        for (int i=0; i<matrix.length; i++) {
            for (int j=0; j<matrix[0].length; j++) {
                CM[i][j] = getCofactor(matrix, i, j);
            }
        }
        return getTransposedMatrix(CM);
    }

    public static float[][] getSubMatrix(float[][] matrix, int i, int j) {
        float[][] Sub = new float[matrix.length-1][matrix[0].length-1];
        for (int y=0; y<matrix.length; y++) {
            for (int x=0; x<matrix[0].length; x++) {
                if (x == j || y == i) continue;
                int idx = x;
                int idy = y;
                if (x > j) idx--;
                if (y > i) idy--;
                Sub[idy][idx] = matrix[y][x];
            }
        }
        return Sub;
    }

    public static float getDeterminant(float[][] matrix) {
        int n = matrix.length;
        int m = matrix[0].length;
        if (n != m) throw new IllegalArgumentException("行列式を計算できません");
        if (n == 1) return matrix[0][0];
        else if (n == 2) return matrix[0][0]*matrix[1][1] - matrix[0][1]*matrix[1][0];
        else {
            int z = 0;
            for (int j=0; j<matrix[0].length; j++) {
                if (j % 2 == 0) z += matrix[0][j] * getDeterminant(getSubMatrix(matrix, 0, j));
                else z -= matrix[0][j] * getDeterminant(getSubMatrix(matrix, 0, j));
            }
            return z;
        }
    }

    public static float[][] getInverseMatrix(float[][] A) {
        float[][] I = new float[A.length][A[0].length];
        float detA = getDeterminant(A);
        for (int i=0; i<A.length; i++) {
            for (int j=0; j<A[0].length; j++) {
                I[j][i] = getCofactor(A, i, j) / detA;
            }
        }
        return I;
    }

    public static float[][] getXRotate4(float theta) {
        return new float[][]{
                { 1, 0, 0, 0 },
                { 0, (float)Math.cos(theta), (float)-Math.sin(theta), 0 },
                { 0, (float)Math.sin(theta), (float)Math.cos(theta), 0 },
                { 0, 0, 0, 1},
        };
    }

    public static float[][] getYRotate4(float theta) {
        return new float[][]{
                { (float)Math.cos(theta), 0, (float)Math.sin(theta), 0 },
                { 0, 1, 0, 0 },
                { (float)-Math.sin(theta), 0, (float)Math.cos(theta), 0 },
                { 0, 0, 0, 1},
        };
    }

    public static float[][] getZRotate4(float theta) {
        return new float[][]{
                { (float)Math.cos(theta), (float)-Math.sin(theta), 0, 0 },
                { (float)Math.sin(theta), (float)Math.cos(theta), 0, 0 },
                { 0, 0, 1, 0 },
                { 0, 0, 0, 1},
        };
    }

    public static float[][] getAdd(float[][] A, float[][] B) {
        if (A.length != B.length || A[0].length != B[0].length) throw new IllegalArgumentException("差を計算できません");
        float[][] C = new float[A.length][A[0].length];
        for (int i=0; i<A.length; i++) {
            for (int j=0; j<A[0].length; j++) {
                C[i][j] = A[i][j] + B[i][j];
            }
        }
        return C;
    }


    public static float[][] getSubtract(float[][] A, float[][] B) {
        if (A.length != B.length || A[0].length != B[0].length) throw new IllegalArgumentException("差を計算できません");
        float[][] C = new float[A.length][A[0].length];
        for (int i=0; i<A.length; i++) {
            for (int j=0; j<A[0].length; j++) {
                C[i][j] = A[i][j] - B[i][j];
            }
        }
        return C;
    }

    public static float[][] getTransration4(float x, float y, float z) {
        return new float[][] {
                { 1f, 0f, 0f,  x },
                { 0f, 1f, 0f,  y },
                { 0f, 0f, 1f,  z },
                { 0f, 0f, 0f, 1f }
        };
    }

    public static String getString(float[][] A) {
        StringBuilder s = new StringBuilder();
        for (float[] Ai : A) {
            for (float Aij : Ai) {
                s.append(Aij).append(" ");
            }
            s.append('\n');
        }
        return s.toString();
    }
}
