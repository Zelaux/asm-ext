package asmlib.transform;

import asmlib.transform.context.TransformationContext;

@SuppressWarnings("unused")
public abstract class AbstractClassFileTransformer implements TransformationProvider {
    public int roundLeft;
    /**Starts with 0*/
    public int roundIndex;

    /**
     * {@inheritDoc}
     */
    public AbstractClassFileTransformer(int maxRound) {
        this.roundLeft = maxRound;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishRound(TransformationContext context) {
        roundLeft--;
        roundIndex++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needNextRound(TransformationContext context) {
        return roundLeft > 0;
    }

}
