package asmext.analytics.controlflow;

import org.intellij.lang.annotations.MagicConstant;
/**
 * @author Zelaux
 * */
@MagicConstant(flagsFromClass = NodeKindProperties.class)
public @interface NodeKindProperties {

     int MULTI_GOTO = 1;
     int GOTO_LABEL = 0b10;
     int GO_NEXT = 0b100;
     int LOOP = 0b1000;
}
