package asmlib.transform.write;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public abstract class Hierarchy {
    public static final String JAVA_LANG_OBJECT = "java.lang.Object";
    public final RawClassMeta rawJavaLangObject = new RawClassMeta(JAVA_LANG_OBJECT, false);
    public final ClassMeta javaLangObject = new ClassMeta(JAVA_LANG_OBJECT, false, null);

    public record RawClassMeta(String superClass, boolean isInterface) {

        public static RawClassMeta of(String superName, boolean isInterface) {
            return new RawClassMeta(
                superName == null ? JAVA_LANG_OBJECT : superName.replace('/', '.'),
                isInterface
            );
        }
    }

    public record ClassMeta(String myName, boolean isInterface, String superType) {}

    public abstract RawClassMeta findRawMeta(String classCanonicalName);

    private HashMap<String, ClassMeta> cache = new HashMap<>();
    public Hierarchy fallback;

    public Hierarchy(Hierarchy fallback) {
        this.fallback = fallback;
        clearCache();
    }

    public void erase(String canonicalName) {
        canonicalName = canonicalName.replace('/', '.');
        cache.remove(canonicalName);
        cache.put(javaLangObject.myName, javaLangObject);
    }

    public void clearCache() {
        cache.clear();
        cache.put(javaLangObject.myName, javaLangObject);
    }

    @Nullable
    public ClassMeta cachedMeta(String type) {
        @Nullable ClassMeta meta = cache.get(type);
        if(meta == null) {
            RawClassMeta raw = findRawMeta(type);
            if(raw != null) {
                cache.put(type, meta = new ClassMeta(type, raw.isInterface, raw.isInterface ? JAVA_LANG_OBJECT : raw.superClass));
            }
        }
        if(meta == null && fallback != null) return fallback.cachedMeta(type);
        return meta;
    }

}
