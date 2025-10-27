package asmlib.transform.context;

import java.util.ArrayList;

public class Dependencies {
    private final ArrayList<Dependency> dependencies = new ArrayList<>();

    public void add(Dependency dependency) {
        dependencies.add(dependency);
    }
}
