package asmlib.transform;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception{
        TransformationProvider[] providers=new TransformationProvider[args.length-1];
        for(int i = 0; i < providers.length; i++) {
            Class<?> name = Class.forName(args[i + 1]);
            providers[i]= (TransformationProvider) name.getConstructors()[0].newInstance();
        }

        Transformations.run(new File(args[0]),providers);
    }
}
