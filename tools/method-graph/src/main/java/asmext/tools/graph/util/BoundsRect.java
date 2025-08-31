package asmext.tools.graph.util;

import lombok.NoArgsConstructor;


@NoArgsConstructor
public class BoundsRect {
    public int x, y, width, height;

    public BoundsRect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static BoundsRect fromRect(int width, int height) {
        return new BoundsRect(0, 0, width, height);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public void reset() {
        x = y = width = height = 0;
    }

    public BoundsRect copy() {
        return new BoundsRect(x,y,width,height);
    }
}
