package asmext.tools.graph;

public class TestClass {
    TestClass() {
        System.out.println("Hello world");
        while ((System.currentTimeMillis() & 31) < 16){
            System.out.println("HelloTMP");
        }
        String x;
        if ((System.currentTimeMillis() & 1) == 0) {
            x = "Hello world1";
        } else {
            x = ("Hello world2");
        }
        System.out.println(x);
    }
}
