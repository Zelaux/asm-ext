package asmext.tools.graph.util;

import lombok.NoArgsConstructor;


@NoArgsConstructor
public class BoundingBox {
    public int left, top, right, bottom;

    public BoundingBox(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public static BoundingBox fromRect(int width, int height) {
        return new BoundingBox(0, 0, width, height);
    }
    public static BoundingBox centerXRect(int width, int height) {
        int hw = width / 2;
        return new BoundingBox(-hw, 0, width- hw, height);
    }

    public void stickRight(int margin, BoundingBox other) {
        top = Math.max(other.top, top);
        bottom = Math.min(other.bottom, bottom);
        right += margin + other.width();
    }

    public void stickLeft(int margin, BoundingBox other) {
        top = Math.max(other.top, top);
        bottom = Math.min(other.bottom, bottom);
        left -= margin + other.width();
    }

    public void stickDown(int margin, BoundingBox other) {
        right = Math.max(other.right, right);
        left = Math.min(other.left, left);
        bottom -= margin + other.height();
    }

    public void stickUp(int margin, BoundingBox other) {
        right = Math.max(other.right, right);
        left = Math.min(other.left, left);
        top += margin + other.height();
    }

    public int width() {
        return right - left;
    }

    public int height() {
        return top - bottom;
    }

    public void reset() {
        left = right = top = bottom = 0;
    }

    public BoundingBox copy() {
        return new BoundingBox(left, top, right, bottom);
    }
}
