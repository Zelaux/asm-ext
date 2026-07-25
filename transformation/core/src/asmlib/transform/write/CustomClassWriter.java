package asmlib.transform.write;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class CustomClassWriter extends ClassWriter {
    private final Hierarchy hierarchy;

    public CustomClassWriter(int flags, Hierarchy hierarchy) {
        super(flags);
        this.hierarchy = hierarchy;
    }

    public CustomClassWriter(ClassReader classReader, int flags, Hierarchy hierarchy) {
        super(classReader, flags);
        this.hierarchy = hierarchy;
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        // Превращаем внутренние имена ASM (a/b/C) в канонические (a.b.C)
        var meta1 = hierarchy.cachedMeta(type1.replace('/', '.'));
        var meta2 = hierarchy.cachedMeta(type2.replace('/', '.'));

        if (meta1 == null || meta2 == null || meta1.isInterface() || meta2.isInterface()) {
            return "java/lang/Object";
        }

        // Алгоритм поиска общего предка
        var s1 = new java.util.HashSet<String>();
        var curr1 = meta1;
        while (curr1 != null) {
            s1.add(curr1.myName());
            if(curr1==hierarchy.javaLangObject)break;
            curr1 = hierarchy.cachedMeta(curr1.superType());
        }

        var curr2 = meta2;
        while (curr2 != null) {
            if (s1.contains(curr2.myName())) {
                return curr2.myName().replace('.', '/');
            }
            if(curr2==hierarchy.javaLangObject)break;
            curr2 = hierarchy.cachedMeta(curr2.superType());
        }

        return "java/lang/Object";
    }
}