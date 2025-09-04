package main.java.asmext;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * @author Zelaux
 * @since 2025-09
 */
public class InsnPrint {
    public static final String[] OPCODES = Printer.OPCODES;
    public static final ThreadLocal<SyncEntry> SYNC_ENTRY_THREAD_LOCAL = ThreadLocal.withInitial(SyncEntry::make);
    public static final PrintInsnTextifier STATIC_PRINT_INSN_TEXTIFIER=new PrintInsnTextifier();
    public static final TraceMethodVisitor STATIC_TRACE_METHOD_VISITOR=new TraceMethodVisitor(STATIC_PRINT_INSN_TEXTIFIER);

    public static String toStringSync(AbstractInsnNode node) {
        SyncEntry entry = SYNC_ENTRY_THREAD_LOCAL.get();
        node.accept(entry.visitor);
        return entry.textifier.getString();
    }

    public static String toString(AbstractInsnNode node) {
        node.accept(STATIC_TRACE_METHOD_VISITOR);
        return STATIC_PRINT_INSN_TEXTIFIER.getString();
    }

    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    static class SyncEntry {
        TraceMethodVisitor visitor;
        PrintInsnTextifier textifier;

        public static SyncEntry make() {
            PrintInsnTextifier printer = new PrintInsnTextifier();
            return new SyncEntry(new TraceMethodVisitor(printer), printer);
        }
    }
}
