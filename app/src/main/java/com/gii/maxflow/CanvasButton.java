package com.gii.maxflow;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

/**
 * Created by Timur on 15-Nov-15.
 * Any button that is drawn on canvas
 */
public class CanvasButton {
    public enum ButtonType {delete, edit,plus,branch,color, rename, operations}
    public ButtonType type;
    public Drawable bitmap; //bottom menu icons
    public Rect rectangle; //bottom menu icons positions
    public String description; //bottom menu icons descriptions
    public float halfMeasuredDescription = 0;
    public Paint paint;
    public ArrayList<CanvasButton> subButton;
    public boolean selected;
    public CanvasButton(ButtonType type) {
        this.type = type;
        subButton = new ArrayList<>();
        halfMeasuredDescription = 0;
        selected = true;
        paint = new Paint();
        rectangle = new Rect(0,0,0,0);
        description = "";
    }
    public CanvasButton(Drawable bitmap, String description, ButtonType type, Paint paint) {
        this.bitmap = bitmap;
        this.description = description;
        this.rectangle = new Rect(0,0,0,0);
        this.type = type;
        this.paint = paint;
        selected = false;
        halfMeasuredDescription = paint.measureText(description) / 2;
        subButton = new ArrayList<>();
    }
    public void openSubMenu(boolean open) {
        selected = open;
        if (selected) {
            for (int i = 0; i < subButton.size(); i++)
                subButton.get(i).setRectangle(new Rect(this.rectangle.left,this.rectangle.top,
                        this.rectangle.right, this.rectangle.bottom));
        }
    }
    public void addSubButton(CanvasButton newSubButton) {
        subButton.add(newSubButton);
    }
    public void setRectangle(Rect rectangle) {
        this.rectangle = rectangle;
    }
    public Rect toRectangle = new Rect(0,0,0,0);
    public boolean needToTransform = false;
    public int speed = 10;
    public void setToRectangle(int left, int top, int right, int bottom) {
        this.toRectangle.set(left,top,right,bottom);
        needToTransform = true;
    }
    public void transformAndMove() {
        if (!needToTransform)
            return;
        int left = toRectangle.left,top = toRectangle.top,bottom = toRectangle.bottom,right = toRectangle.right;

        if (Math.abs(rectangle.left - toRectangle.left) > speed)
            left = rectangle.left + (toRectangle.left - rectangle.left) / 3 ;

        if (Math.abs(rectangle.right - toRectangle.right) > speed)
            right = rectangle.right + (toRectangle.right - rectangle.right) / 3;

        if (Math.abs(rectangle.top - toRectangle.top) > speed)
            top =  rectangle.top + (toRectangle.top - rectangle.top) / 3;

        if (Math.abs(rectangle.bottom - toRectangle.bottom) > speed)
            bottom = rectangle.bottom + (toRectangle.bottom - rectangle.bottom) / 3;

        rectangle.set(left,top,right,bottom);
        if (rectangle.equals(toRectangle))
            needToTransform = false;
    }
}
