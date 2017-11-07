package com.picup.calling.typefaced;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.widget.Button;

import com.picup.calling.R;

/**
 * Created by frank.truong on 12/1/2016.
 */

public class RobotoButton extends Button implements RobotoConstants {

    private static final SparseArray<Typeface> typefaces = new SparseArray<>(18);

    public RobotoButton(Context context) {
        super(context);

    }

    public RobotoButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(context, attrs);
    }

    public RobotoButton(Context context, AttributeSet attrs, int defStype) {
        super(context, attrs, defStype);
        parseAttributes(context, attrs);
    }

    private void parseAttributes(Context context, AttributeSet attrs) {
        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.RobotoButton);
        int typefaceValue = values.getInt(R.styleable.RobotoButton_typeface, 0);
        values.recycle();

        setTypeface(obtainTypeface(context, typefaceValue));
    }

    private Typeface obtainTypeface(Context context, int typefaceValue) throws IllegalArgumentException {
        Typeface typeface = typefaces.get(typefaceValue);
        if (typeface == null) {
            typeface = createTypeface(context, typefaceValue);
            typefaces.put(typefaceValue, typeface);
        }
        return typeface;
    }

    private Typeface createTypeface(Context context, int typefaceValue) throws IllegalArgumentException {
        Typeface typeface = null;

        switch (typefaceValue) {
            case ROBOTO_BLACK:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Black.ttf.ttf");
                break;
            case ROBOTO_BLACK_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-BlackItalic.ttf");
                break;
            case ROBOTO_BOLD:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Bold.ttf");
                break;
            case ROBOTO_BOLD_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-BlackItalic.ttf");
                break;
            case ROBOTO_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Italic.ttf");
                break;
            case ROBOTO_LIGHT:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
                break;
            case ROBOTO_LIGHT_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-LightItalic.ttf");
                break;
            case ROBOTO_MEDIUM:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
                break;
            case ROBOTO_MEDIUM_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-MediumItalic.ttf");
                break;
            case ROBOTO_REGULAR:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
                break;
            case ROBOTO_THIN:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
                break;
            case ROBOTO_THIN_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-ThinItalic.ttf");
                break;
            case ROBOTO_CONDENSED_BOLD:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-Bold.ttf");
                break;
            case ROBOTO_CONDENSED_BOLD_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-BoldItalic.ttf");
                break;
            case ROBOTO_CONDENSED_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-Italic.ttf");
                break;
            case ROBOTO_CONDENSED_LIGHT:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-Light.ttf");
                break;
            case ROBOTO_CONDENSED_LIGHT_ITALIC:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-LightItalic.ttf");
                break;
            case ROBOTO_CONDENSED_REGULAR:
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-Regular.ttf");
                break;
        }
        return typeface;
    }
}
