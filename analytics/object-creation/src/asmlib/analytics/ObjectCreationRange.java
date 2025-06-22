package asmlib.analytics;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC)

public class ObjectCreationRange {
    @Nullable
    final ObjectCreationRange parent;
    @NotNull
    final String type;
    final int startIndex;
    @NotNull
    final List<ObjectCreationRange> innerObjects = new ArrayList<>();
    @SuppressWarnings("unused")
    int endIndex = -1;

    @Override
    public String toString() {
        return type + "[" + startIndex + "; " + endIndex + "]";
    }
}
