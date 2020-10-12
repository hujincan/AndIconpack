package org.andcreator.iconpack.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.andcreator.iconpack.R;
import org.andcreator.iconpack.util.DisplayUtil;
import org.andcreator.iconpack.util.Utils;

public class SplitButtonsLayout extends LinearLayout {

    private int mButtonCount;

    public SplitButtonsLayout(Context context) {
        super(context);
        init(context);
    }

    public SplitButtonsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SplitButtonsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
    }

    /**
     * Sets how many buttons the layout will have.
     */
    public void setButtonCount(int buttonCount) {
        this.mButtonCount = buttonCount;
        setWeightSum(buttonCount);
    }

    public void addButton(int iconId, String link) {
        if (getChildCount() == mButtonCount)
            throw new IllegalStateException(mButtonCount + " buttons have already been added.");
        final ImageView newButton = (ImageView) LayoutInflater.from(getContext())
                .inflate(R.layout.item_credits_button, this, false);
        // width can be 0 since weight is used
        this.setGravity(Gravity.CENTER_HORIZONTAL);
//        lp.setMargins(DisplayUtil.dip2px(getContext(), 8f),0,DisplayUtil.dip2px(getContext(), 8f),0);
        Glide.with(newButton).load(iconId).into(newButton);
        newButton.setTag(link);
        addView(newButton);
    }

    public boolean hasAllButtons() {
        return getChildCount() == mButtonCount;
    }
}