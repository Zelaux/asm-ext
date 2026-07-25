package asmlib.transform.context;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.util.Map;

@AllArgsConstructor
@Getter
public class Dependency {
    public final File file;

    public static Dependency fromString(String arg, Map<String, Dependency> cache) {
        return cache.computeIfAbsent(arg,x->new Dependency(new File(x))) ;
    }
}
