package asmlib.transform.context;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Dependencies implements Iterable<Dependency>{
    private final ArrayList<Dependency> dependencies = new ArrayList<>();
    private final List<Dependency> dependenciesView = Collections.unmodifiableList(dependencies);

    public void add(Dependency dependency) {
        dependencies.add(dependency);
    }

    @Override
    public @NotNull Iterator<Dependency> iterator() {
        return dependenciesView.iterator();
    }
}
