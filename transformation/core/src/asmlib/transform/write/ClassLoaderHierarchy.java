package asmlib.transform.write;

public class ClassLoaderHierarchy extends Hierarchy {
    public ClassLoader loader;

    public ClassLoaderHierarchy(ClassLoader loader, Hierarchy fallback) {
        super(fallback);
        this.loader = loader;
    }

    @Override
    public RawClassMeta findRawMeta(String classCanonicalName) {
        try {
            Class<?> type = Class.forName(classCanonicalName, false, loader);
            Class<?> superclass = type.getSuperclass();
            return RawClassMeta.of(superclass == null ? rawJavaLangObject.superClass() : superclass.getName(), type.isInterface());
        } catch(ClassNotFoundException e) {
            return null;
        } catch(NullPointerException e){
            throw e;
        }
    }
}
