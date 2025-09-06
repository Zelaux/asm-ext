package asmext.tree.analysis.dataflow.value.merge.meta;

import asmext.tree.analysis.dataflow.interpreter.MetaDataKey;
import asmext.tree.analysis.dataflow.value.DataFlowValue;

import java.util.ArrayList;

/**
 * @author Zelaux
 * @since 2025-09
 */
public class LocationListMeta {
    public static final MetaDataKey<LocationListMeta> meta = MetaDataKey.create();
    public final ArrayList<LocationEntry> locations = new ArrayList<>();

    public static void addLocation(DataFlowValue visited, LocationEntry e) {
        LocationListMeta locationListMeta = visited.getMeta(meta);
        if (locationListMeta == null)
            visited.putMeta(meta, locationListMeta = new LocationListMeta());
        locationListMeta.locations.add(e);
    }
}
