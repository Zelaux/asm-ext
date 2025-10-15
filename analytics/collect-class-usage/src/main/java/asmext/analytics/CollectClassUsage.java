package asmext.analytics;


import asmlib.util.ClassFileMetaData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class CollectClassUsage {
    public static HashSet<String> collectAll(ClassNameResolution resolution,boolean doRecursion, String internalStartClassName) {
        byte[] startBytes = resolution.resolveClass(internalStartClassName);
        if (startBytes == null) return null;
        var names = new HashSet<String>();
        if(!doRecursion){
            new ClassFileMetaData(startBytes)
                    .usedClasses()
                    .map(resolution::resolveClass)
                    .filter(Objects::nonNull)
                    .map(ClassFileMetaData::new)
                    .map(ClassFileMetaData::getClassName)
                    .forEach(names::add);
            ;
            return names;
        }
        ByteArrSeq arr1 = new ByteArrSeq(), arr2 = new ByteArrSeq(), tmp;
        arr1.add(startBytes);

        while (arr1.size > 0) {
            for (int i = 0; i < arr1.size; i++) {
                byte[] b = arr1.arr[i];
                ClassFileMetaData data = new ClassFileMetaData(b);
                if(!names.add(data.getClassName()))continue;
                data.usedClasses()
                    .map(resolution::resolveClass)
                    .filter(Objects::nonNull)
                    .forEach(arr2::add);
            }

            {
                tmp = arr1;
                arr1 = arr2;
                arr2 = tmp;
                arr2.size = 0;
            }
        }
        return names;
    }
}
