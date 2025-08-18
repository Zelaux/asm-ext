package asmlib.util.testclasses;

import java.util.List;

@SuppressWarnings("ALL")
public class Test2 {

    public static int magicNumber = number();
    private String someString="haha";

    public String someString() {
        return someString;
    }

    public void setSomeString(String someString) {
        this.someString = someString;
    }

    {
        Test2.class.getInterfaces();
        List x=null;

        System.out.println(x.size());
        System.out.println(number());
        System.out.println(someString);
        System.out.println(magicNumber);
        System.out.println(System.out);
    }
@Deprecated
    public static int number() {
        return 0;
    }

}
