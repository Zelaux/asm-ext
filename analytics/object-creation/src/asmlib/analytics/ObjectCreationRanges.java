package asmlib.analytics;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class ObjectCreationRanges {
    List<ObjectCreationRange> rootRanges;
    List<ObjectCreationRange> allRanges;
}
