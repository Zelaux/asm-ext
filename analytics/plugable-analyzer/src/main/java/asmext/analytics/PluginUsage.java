package asmext.analytics;

/**
 * @author Zelaux
 * @since 2025-09
 */
public enum PluginUsage {
    initFrame,
    controlFlowEdge,
    frameMerge,
    frameExecute,
    frameInitJump, beforeAnalyze, afterAnalyze;

    public static final PluginUsage[] all = values();
    public static final int allAmount= all.length;

    public final int id=ordinal();
}
