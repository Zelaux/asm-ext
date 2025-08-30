package asmext.tools.graph;

public class TestClass {
    TestClass() {
        System.out.println("Hello world");
        if ((System.currentTimeMillis() & 1) == 0) {
            System.out.println("Hello world1");
        } else {
            System.out.println("Hello world2");
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
