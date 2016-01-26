/*
 * Copyright (C) 2016 Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.jaredrummler.materialspinner;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

public class MaterialSpinner extends TextView {

  private static final int MAX_LEVEL = 10000;
  private static final int DEFAULT_ELEVATION = 16;
  private static final String INSTANCE_STATE = "instance_state";
  private static final String SELECTED_INDEX = "selected_index";
  private static final String IS_POPUP_SHOWING = "is_popup_showing";

  private int selectedIndex;
  private Drawable arrowDrawable;
  private PopupWindow popupWindow;
  private ListView listView;
  private MaterialSpinnerBaseAdapter adapter;
  private AdapterView.OnItemClickListener onItemClickListener;
  private AdapterView.OnItemSelectedListener onItemSelectedListener;
  private boolean hideArrow;

  public MaterialSpinner(Context context) {
    super(context);
    init(context, null);
  }

  public MaterialSpinner(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public MaterialSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  @Override public Parcelable onSaveInstanceState() {
    Bundle bundle = new Bundle();
    bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
    bundle.putInt(SELECTED_INDEX, selectedIndex);
    if (popupWindow != null) {
      bundle.putBoolean(IS_POPUP_SHOWING, popupWindow.isShowing());
      dismissDropDown();
    }
    return bundle;
  }

  @Override public void onRestoreInstanceState(Parcelable savedState) {
    if (savedState instanceof Bundle) {
      Bundle bundle = (Bundle) savedState;

      selectedIndex = bundle.getInt(SELECTED_INDEX);

      if (adapter != null) {
        setText(adapter.getItemInDataset(selectedIndex).toString());
        adapter.notifyItemSelected(selectedIndex);
      }

      if (bundle.getBoolean(IS_POPUP_SHOWING)) {
        if (popupWindow != null) {
          // Post the show request into the looper to avoid bad token exception
          post(new Runnable() {

            @Override
            public void run() {
              showDropDown();
            }
          });
        }
      }

      savedState = bundle.getParcelable(INSTANCE_STATE);
    }

    super.onRestoreInstanceState(savedState);
  }

  private void init(Context context, AttributeSet attrs) {
    Resources resources = getResources();
    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaterialSpinner);
    setGravity(Gravity.CENTER_VERTICAL | Gravity.START);

    boolean rtl = false;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      Configuration config = getResources().getConfiguration();
      rtl = config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
      if (rtl) {
        setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        setTextDirection(View.TEXT_DIRECTION_RTL);
      }
    }

    int left, right, bottom, top;
    left = right = bottom = top = resources.getDimensionPixelSize(R.dimen.one_and_a_half_grid_unit);
    if (rtl) {
      right = resources.getDimensionPixelSize(R.dimen.three_grid_unit);
    } else {
      left = resources.getDimensionPixelSize(R.dimen.three_grid_unit);
    }
    setPadding(left, top, right, bottom);

    setClickable(true);
    setBackgroundResource(R.drawable.ms__selector);

    hideArrow = typedArray.getBoolean(R.styleable.MaterialSpinner_ms_hide_arrow, false);
    if (!hideArrow) {
      Drawable basicDrawable = ContextCompat.getDrawable(context, R.drawable.ms__arrow);
      int resId = typedArray.getColor(R.styleable.MaterialSpinner_ms_arrow_tint, -1);
      if (basicDrawable != null) {
        arrowDrawable = DrawableCompat.wrap(basicDrawable);
        if (resId != -1) {
          DrawableCompat.setTint(arrowDrawable, resId);
        }
      }
      if (rtl) {
        setCompoundDrawablesWithIntrinsicBounds(arrowDrawable, null, null, null);
      } else {
        setCompoundDrawablesWithIntrinsicBounds(null, null, arrowDrawable, null);
      }
    }

    typedArray.recycle();

    listView = new ListView(context);
    // Set the spinner's id into the listview to make it pretend to be the right parent in
    // onItemClick
    listView.setId(getId());
    listView.setDivider(null);
    listView.setItemsCanFocus(true);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position >= selectedIndex && position < adapter.getCount()) {
          position++;
        }

        // Need to set selected index before calling listeners or getSelectedIndex()
        selectedIndex = position;

        if (onItemClickListener != null) {
          onItemClickListener.onItemClick(parent, view, position, id);
        }

        if (onItemSelectedListener != null) {
          onItemSelectedListener.onItemSelected(parent, view, position, id);
        }

        adapter.notifyItemSelected(position);
        setText(adapter.getItemInDataset(position).toString());
        dismissDropDown();
      }
    });

    popupWindow = new PopupWindow(context);
    popupWindow.setContentView(listView);
    popupWindow.setOutsideTouchable(true);
    popupWindow.setFocusable(true);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      popupWindow.setElevation(DEFAULT_ELEVATION);
      popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ms__drawable));
    } else {
      popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ms__drop_down_shadow));
    }

    popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

      @Override public void onDismiss() {
        if (!hideArrow) {
          animateArrow(false);
        }
      }
    });
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  /**
   * Set the default spinner item using its index
   *
   * @param position
   *     the item's position
   */
  public void setSelectedIndex(int position) {
    if (adapter != null) {
      if (position >= 0 && position <= adapter.getCount()) {
        adapter.notifyItemSelected(position);
        selectedIndex = position;
        setText(adapter.getItemInDataset(position).toString());
      } else {
        throw new IllegalArgumentException("Position must be lower than adapter count!");
      }
    }
  }

  public void addOnItemClickListener(@NonNull AdapterView.OnItemClickListener onItemClickListener) {
    this.onItemClickListener = onItemClickListener;
  }

  public void setOnItemSelectedListener(@NonNull AdapterView.OnItemSelectedListener onItemSelectedListener) {
    this.onItemSelectedListener = onItemSelectedListener;
  }

  public <T> void attachDataSource(@NonNull List<T> dataset) {
    adapter = new MaterialSpinnerAdapter<>(getContext(), dataset);
    setAdapterInternal(adapter);
  }

  public void setAdapter(@NonNull ListAdapter adapter) {
    this.adapter = new MaterialSpinnerAdapterWrapper(getContext(), adapter);
    setAdapterInternal(this.adapter);
  }

  private void setAdapterInternal(@NonNull MaterialSpinnerBaseAdapter adapter) {
    listView.setAdapter(adapter);
    setText(adapter.getItemInDataset(selectedIndex).toString());
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    popupWindow.setWidth(MeasureSpec.getSize(widthMeasureSpec));
    popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  @Override public boolean onTouchEvent(@NonNull MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_UP) {
      if (!popupWindow.isShowing()) {
        showDropDown();
      } else {
        dismissDropDown();
      }
    }

    return super.onTouchEvent(event);
  }

  private void animateArrow(boolean shouldRotateUp) {
    int start = shouldRotateUp ? 0 : MAX_LEVEL;
    int end = shouldRotateUp ? MAX_LEVEL : 0;
    ObjectAnimator animator = ObjectAnimator.ofInt(arrowDrawable, "level", start, end);
    animator.setInterpolator(new LinearOutSlowInInterpolator());
    animator.start();
  }

  public void dismissDropDown() {
    if (!hideArrow) {
      animateArrow(false);
    }
    popupWindow.dismiss();
  }

  public void showDropDown() {
    if (!hideArrow) {
      animateArrow(true);
    }
    popupWindow.showAsDropDown(this);
  }

  public void setTintColor(@ColorRes int resId) {
    if (arrowDrawable != null && !hideArrow) {
      DrawableCompat.setTint(arrowDrawable, getResources().getColor(resId));
    }
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  private boolean isRTL() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
      return false;
    }
    Configuration config = getResources().getConfiguration();
    return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
  }

}
