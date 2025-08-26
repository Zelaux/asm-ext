package asmlib.transform;

import org.jetbrains.annotations.*;

@SuppressWarnings("unused")
public abstract class AbstractClassFileTransformer implements TransformationProvider {
    public int roundLeft;
    public int roundIndex;

    public AbstractClassFileTransformer(int maxRound){
        this.roundLeft = maxRound;
    }

    @Override
    public void finishRound() {
        roundLeft--;
        roundIndex++;
    }

    @Override
    public boolean needNextRound(){
        return roundLeft > 0;
    }

}
