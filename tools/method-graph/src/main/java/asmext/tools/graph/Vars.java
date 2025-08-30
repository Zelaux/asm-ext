package asmext.tools.graph;

import asmext.tools.graph.ui.elem.Group;
import lombok.SneakyThrows;

import java.io.File;

public interface Vars {
    Group mainGroup = new Group(0, 0, 100, 100, Group.LayoutDirection.NoLayout);
    File myPath = getMyPath();
    File openQueueFile = new File(myPath,"lastopen.txt");

    @SneakyThrows
    static File getMyPath() {
        return new File(Vars.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
    }
}
