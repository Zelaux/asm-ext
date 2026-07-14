package asmlib.transform;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExtraArguments implements Iterable<String> {
    private final ArrayList<String> list = new ArrayList<>();
    private Map<String, Integer> loopupTable;

    private boolean debug;
    @Getter
    private boolean release;

    public boolean isDebug() {
        return debug;
    }

    public void invalidateLoopupTable() {
        loopupTable = null;
    }

    public Map<String, Integer> getLoopupTable() {
        Map<String, Integer> map = loopupTable;
        if(map == null) {
            map = new HashMap<>(list.size());
            for(int i = 0; i < list.size(); i++) {
                map.put(list.get(i), i);
            }
        }
        return map;
    }


    public void add(String arg) {
        list.add(arg);
        if(arg.equals("-debug")) debug = true;
        if(arg.equals("-release")) release = true;
        invalidateLoopupTable();
    }

    public void remove(String arg) {
        if(list.remove(arg)) {
            removed(arg);
        }
    }
    public void removeAt(int index) {
        removed(list.remove(index));

    }

    private void removed(String remove) {
        if(remove.equals("-debug")) debug = false;
        if(remove.equals("-release")) release = false;
        invalidateLoopupTable();
    }

    public int size(){
        return list.size();
    }

    @Override
    public @NotNull Iterator<String> iterator() {
        return list.iterator();
    }

    public boolean contains(String s) {
        return getLoopupTable().containsKey(s);
    }
}
