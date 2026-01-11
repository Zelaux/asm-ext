package asmext.postprocessors.inlinefunc;

class TestClass {
    static float[] arr = new float[1 << 8];

    static float x(int i) {return arr[i << 1];}

    static float y(int i) {return arr[(i << 1) + 1];}

    static float dot(int i, float x, float y) {
        return x(i) * x + y(i) * y;
    }


    static float dotx(int i) {return x(i++)*x(i);}
    static float doty(int i) {return y(i++)*y(i);}

    static float dot_i(int i) {
        return dotx(i)+doty(i);
    }
}
