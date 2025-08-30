package asmext.tools.graph;

import lombok.SneakyThrows;

import java.awt.*;
import java.awt.font.FontRenderContext;

public interface Fonts {
    Font jetbrainsLigature=loadFont("JetBrainsMono-Medium.ttf");
    Font jetbrainsNoLigature=loadFont("JetBrainsMonoNL-Medium.ttf");
    FontRenderContext[] defaultContext= {new FontRenderContext(null, true, false)};
    @SneakyThrows
    static Font loadFont(String name){
        Font font = Font.createFont(Font.TRUETYPE_FONT, Fonts.class.getClassLoader().getResourceAsStream(name));

        return font.deriveFont(24f);
    }
}
