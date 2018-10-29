/*
 * Copyright (C) 2016 Jared Rummler
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
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * A spinner that shows a {@link PopupWindow} under the view when clicked.
 */
public class MaterialSpinner extends TextView {

  private OnNothingSelectedListener onNothingSelectedListener;
  private OnItemSelectedListener onItemSelectedListener;
  private MaterialSpinnerBaseAdapter adapter;
  private PopupWindow popupWindow;
  private ListView listView;
  private Drawable arrowDrawable;
  private boolean hideArrow;
  private boolean nothingSelected;
  private int popupWindowMaxHeight;
  private int popupWindowHeight;
  private int selectedIndex;
  private int backgroundColor;
  private int backgroundSelector;
  private int arrowColor;
  private int arrowColorDisabled;
  private int textColor;
  private int hintColor;
  private int paddingTop;
  private int paddingLeft;
  private int paddingBottom;
  private int paddingRight;
  private int popupPaddingTop;
  private int popupPaddingLeft;
  private int popupPaddingBottom;
  private int popupPaddingRight;
  private String hintText;

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

  private void init(Context context, AttributeSet attrs) {
    TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MaterialSpinner);
    int defaultColor = getTextColors().getDefaultColor();
    boolean rtl = Utils.isRtl(context);

    try {
      backgroundColor = ta.getColor(R.styleable.MaterialSpinner_ms_background_color, Color.WHITE);
      backgroundSelector = ta.getResourceId(R.styleable.MaterialSpinner_ms_background_selector, 0);
      textColor = ta.getColor(R.styleable.MaterialSpinner_ms_text_color, defaultColor);
      hintColor = ta.getColor(R.styleable.MaterialSpinner_ms_hint_color, defaultColor);
      arrowColor = ta.getColor(R.styleable.MaterialSpinner_ms_arrow_tint, textColor);
      hideArrow = ta.getBoolean(R.styleable.MaterialSpinner_ms_hide_arrow, false);
      hintText = ta.getString(R.styleable.MaterialSpinner_ms_hint) == null ? ""
          : ta.getString(R.styleable.MaterialSpinner_ms_hint);
      popupWindowMaxHeight = ta.getDimensionPixelSize(R.styleable.MaterialSpinner_ms_dropdown_max_height, 0);
      popupWindowHeight = ta.getLayoutDimension(R.styleable.MaterialSpinner_ms_dropdown_height,
          WindowManager.LayoutParams.WRAP_CONTENT);
      paddingTop = ta.getDimensionPixelSize(R.styleable.MaterialSpinner_ms_padding_top, -1);
      paddingLeft = ta.getDimensionPixelSize(R.styleable.MaterialSpinner_ms_padding_left, -1);
      paddingBottom = ta.getDimensionPixelSize(R.styleable.MaterialSpinner_ms_padding_bottom, -1);
      paddingRight = ta.getDimensionPixelSize(R.styleable.MaterialSpinner_ms_padding_right, -1);
      popupPaddingTop = ta.getDimensionPixelSize(R.styleable.MaterialSpinner_ms_popup_padding_top, -1);
      popupPaddingLeft = ta.getDimensionPixelSize(R.styleable.MaterialSpinner_ms_popup_padding_left, -1);
      popupPaddingBottom = ta.getDimensionPixelSize(R.styleable.MaterialSpinner_ms_popup_padding_bottom, -1);
      popupPaddingRight = ta.getDimensionPixelSize(R.styleable.MaterialSpinner_ms_popup_padding_right, -1);
      arrowColorDisabled = Utils.lighter(arrowColor, 0.8f);
    } finally {
      ta.recycle();
    }

    nothingSelected = true;

    Resources resources = getResources();
    int left, right, bottom, top;
    left = right = bottom = top = resources.getDimensionPixelSize(R.dimen.ms__padding_top);
    if (rtl) {
      right = resources.getDimensionPixelSize(R.dimen.ms__padding_left);
    } else {
      left = resources.getDimensionPixelSize(R.dimen.ms__padding_left);
    }

    setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
    setClickable(true);
    setPadding(left, top, right, bottom);

    if(paddingTop != -1)
    {
      left = getPaddingLeft();
      right = getPaddingRight();
      bottom = getPaddingBottom();

      setPadding(left, paddingTop, right, bottom);
    }

    if (paddingLeft != -1) {
      top = getPaddingTop();
      right = getPaddingRight();
      bottom = getPaddingBottom();

      setPadding(paddingLeft, top, right, bottom);
    }

    if (paddingBottom != -1) {
      left = getPaddingLeft();
      top = getPaddingTop();
      right = getPaddingRight();

      setPadding(left, top, right, paddingBottom);
    }

    if (paddingRight != -1) {
      left = getPaddingLeft();
      top = getPaddingTop();
      bottom = getPaddingBottom();

      setPadding(left, top, paddingRight, bottom);
    }

    setBackgroundResource(R.drawable.ms__selector);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && rtl) {
      setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
      setTextDirection(View.TEXT_DIRECTION_RTL);
    }

    if (!hideArrow) {
      arrowDrawable = Utils.getDrawable(context, R.drawable.ms__arrow).mutate();
      arrowDrawable.setColorFilter(arrowColor, PorterDuff.Mode.SRC_IN);
      if (rtl) {
        setCompoundDrawablesWithIntrinsicBounds(arrowDrawable, null, null, null);
      } else {
        setCompoundDrawablesWithIntrinsicBounds(null, null, arrowDrawable, null);
      }
    }

    listView = new ListView(context);
    listView.setId(getId());
    listView.setDivider(null);
    listView.setItemsCanFocus(true);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position >= selectedIndex
            && position < adapter.getCount()
            && adapter.getItems().size() != 1
            && TextUtils.isEmpty(hintText)) {
          position++;
        }
        selectedIndex = position;
        nothingSelected = false;
        Object item = adapter.get(position);
        adapter.notifyItemSelected(position);
        setTextColor(textColor);
        setText(item.toString());
        collapse();
        if (onItemSelectedListener != null) {
          //noinspection unchecked
          onItemSelectedListener.onItemSelected(MaterialSpinner.this, position, id, item);
        }
      }
    });

    popupWindow = new PopupWindow(context);
    popupWindow.setContentView(listView);
    popupWindow.setOutsideTouchable(true);
    popupWindow.setFocusable(true);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      popupWindow.setElevation(16);
      popupWindow.setBackgroundDrawable(Utils.getDrawable(context, R.drawable.ms__drawable));
    } else {
      popupWindow.setBackgroundDrawable(Utils.getDrawable(context, R.drawable.ms__drop_down_shadow));
    }

    if (backgroundColor != Color.WHITE) { // default color is white
      setBackgroundColor(backgroundColor);
    } else if (backgroundSelector != 0) {
      setBackgroundResource(backgroundSelector);
    }
    if (textColor != defaultColor) {
      setTextColor(textColor);
    }

    popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

      @Override public void onDismiss() {
        if (nothingSelected && onNothingSelectedListener != null) {
          onNothingSelectedListener.onNothingSelected(MaterialSpinner.this);
        }
        if (!hideArrow) {
          animateArrow(false);
        }
      }
    });
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    popupWindow.setWidth(MeasureSpec.getSize(widthMeasureSpec));
    popupWindow.setHeight(calculatePopupWindowHeight());
    if (adapter != null) {
      CharSequence currentText = getText();
      String longestItem = currentText.toString();
      for (int i = 0; i < adapter.getCount(); i++) {
        String itemText = adapter.getItemText(i);
        if (itemText.length() > longestItem.length()) {
          longestItem = itemText;
        }
      }
      setText(longestItem);
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
      setText(currentText);
    } else {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
  }

  @Override public boolean onTouchEvent(@NonNull MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_UP) {
      if (isEnabled() && isClickable()) {
        if (!popupWindow.isShowing()) {
          expand();
        } else {
          collapse();
        }
      }
    }
    return super.onTouchEvent(event);
  }

  @Override public void setBackgroundColor(int color) {
    backgroundColor = color;
    Drawable background = getBackground();
    if (background instanceof StateListDrawable) { // pre-L
      try {
        Method getStateDrawable = StateListDrawable.class.getDeclaredMethod("getStateDrawable", int.class);
        if (!getStateDrawable.isAccessible()) getStateDrawable.setAccessible(true);
        int[] colors = { Utils.darker(color, 0.85f), color };
        for (int i = 0; i < colors.length; i++) {
          ColorDrawable drawable = (ColorDrawable) getStateDrawable.invoke(background, i);
          drawable.setColor(colors[i]);
        }
      } catch (Exception e) {
        Log.e("MaterialSpinner", "Error setting background color", e);
      }
    } else if (background != null) { // 21+ (RippleDrawable)
      background.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
    popupWindow.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
  }

  @Override public void setTextColor(int color) {
    textColor = color;
    super.setTextColor(color);
  }

  public void setHintColor(int color) {
    hintColor = color;
    super.setTextColor(color);
  }

  @Override public Parcelable onSaveInstanceState() {
    Bundle bundle = new Bundle();
    bundle.putParcelable("state", super.onSaveInstanceState());
    bundle.putInt("selected_index", selectedIndex);
    bundle.putBoolean("nothing_selected", nothingSelected);
    if (popupWindow != null) {
      bundle.putBoolean("is_popup_showing", popupWindow.isShowing());
      collapse();
    } else {
      bundle.putBoolean("is_popup_showing", false);
    }
    return bundle;
  }

  @Override public void onRestoreInstanceState(Parcelable savedState) {
    if (savedState instanceof Bundle) {
      Bundle bundle = (Bundle) savedState;
      selectedIndex = bundle.getInt("selected_index");
      nothingSelected = bundle.getBoolean("nothing_selected");
      if (adapter != null) {
        if (nothingSelected && !TextUtils.isEmpty(hintText)) {
          setHintColor(hintColor);
          setText(hintText);
        } else {
          setTextColor(textColor);
          setText(adapter.get(selectedIndex).toString());
        }
        adapter.notifyItemSelected(selectedIndex);
      }
      if (bundle.getBoolean("is_popup_showing")) {
        if (popupWindow != null) {
          // Post the show request into the looper to avoid bad token exception
          post(new Runnable() {

            @Override public void run() {
              expand();
            }
          });
        }
      }
      savedState = bundle.getParcelable("state");
    }
    super.onRestoreInstanceState(savedState);
  }

  @Override public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if (arrowDrawable != null) {
      arrowDrawable.setColorFilter(enabled ? arrowColor : arrowColorDisabled, PorterDuff.Mode.SRC_IN);
    }
  }

  /**
   * @return the selected item position
   */
  public int getSelectedIndex() {
    return selectedIndex;
  }

  /**
   * Set the default spinner item using its index
   *
   * @param position the item's position
   */
  public void setSelectedIndex(int position) {
    if (adapter != null) {
      if (position >= 0 && position <= adapter.getCount()) {
        adapter.notifyItemSelected(position);
        selectedIndex = position;
        setText(adapter.get(position).toString());
      } else {
        throw new IllegalArgumentException("Position must be lower than adapter count!");
      }
    }
  }

  /**
   * Register a callback to be invoked when an item in the dropdown is selected.
   *
   * @param onItemSelectedListener The callback that will run
   */
  public void setOnItemSelectedListener(@Nullable OnItemSelectedListener onItemSelectedListener) {
    this.onItemSelectedListener = onItemSelectedListener;
  }

  /**
   * Register a callback to be invoked when the {@link PopupWindow} is shown but the user didn't select an item.
   *
   * @param onNothingSelectedListener the callback that will run
   */
  public void setOnNothingSelectedListener(@Nullable OnNothingSelectedListener onNothingSelectedListener) {
    this.onNothingSelectedListener = onNothingSelectedListener;
  }

  /**
   * Set the dropdown items
   *
   * @param items A list of items
   * @param <T> The item type
   */
  public <T> void setItems(@NonNull T... items) {
    setItems(Arrays.asList(items));
  }

  /**
   * Set the dropdown items
   *
   * @param items A list of items
   * @param <T> The item type
   */
  public <T> void setItems(@NonNull List<T> items) {
    adapter = new MaterialSpinnerAdapter<>(getContext(), items)
            .setBackgroundSelector(backgroundSelector)
            .setTextColor(textColor)
            .setPopupPaddingTop(popupPaddingTop)
            .setPopupPaddingLeft(popupPaddingLeft)
            .setPopupPaddingBottom(popupPaddingBottom)
            .setPopupPaddingRight(popupPaddingRight);
    setAdapterInternal(adapter);
  }

  /**
   * Get the list of items in the adapter
   *
   * @param <T> The item type
   * @return A list of items or {@code null} if no items are set.
   */
  public <T> List<T> getItems() {
    if (adapter == null) {
      return null;
    }
    //noinspection unchecked
    return adapter.getItems();
  }

  /**
   * Set a custom adapter for the dropdown items
   *
   * @param adapter The list adapter
   */
  public void setAdapter(@NonNull ListAdapter adapter) {
    this.adapter = new MaterialSpinnerAdapterWrapper(getContext(), adapter)
            .setBackgroundSelector(backgroundSelector)
            .setTextColor(textColor)
            .setPopupPaddingTop(popupPaddingTop)
            .setPopupPaddingLeft(popupPaddingLeft)
            .setPopupPaddingBottom(popupPaddingBottom)
            .setPopupPaddingRight(popupPaddingRight);
    setAdapterInternal(this.adapter);
  }

  /**
   * Set the custom adapter for the dropdown items
   *
   * @param adapter The adapter
   * @param <T> The type
   */
  public <T> void setAdapter(MaterialSpinnerAdapter<T> adapter) {
    this.adapter = adapter;
    this.adapter.setTextColor(textColor);
    this.adapter.setBackgroundSelector(backgroundSelector);
    this.adapter.setPopupPaddingTop(popupPaddingTop);
    this.adapter.setPopupPaddingLeft(popupPaddingLeft);
    this.adapter.setPopupPaddingBottom(popupPaddingBottom);
    this.adapter.setPopupPaddingRight(popupPaddingRight);
    setAdapterInternal(adapter);
  }

  private void setAdapterInternal(@NonNull MaterialSpinnerBaseAdapter adapter) {
    adapter.setHintEnabled(!TextUtils.isEmpty(hintText));
    listView.setAdapter(adapter);
    if (selectedIndex >= adapter.getCount()) {
      selectedIndex = 0;
    }
    if (adapter.getItems().size() > 0) {
      if (nothingSelected && !TextUtils.isEmpty(hintText)) {
        setText(hintText);
        setHintColor(hintColor);
      } else {
        setTextColor(textColor);
        setText(adapter.get(selectedIndex).toString());
      }
    } else {
      setText("");
    }
  }

  /**
   * Show the dropdown menu
   */
  public void expand() {
    if (!hideArrow) {
      animateArrow(true);
    }
    nothingSelected = true;
    popupWindow.showAsDropDown(this);
  }

  /**
   * Closes the dropdown menu
   */
  public void collapse() {
    if (!hideArrow) {
      animateArrow(false);
    }
    popupWindow.dismiss();
  }

  /**
   * Set the tint color for the dropdown arrow
   *
   * @param color the color value
   */
  public void setArrowColor(@ColorInt int color) {
    arrowColor = color;
    arrowColorDisabled = Utils.lighter(arrowColor, 0.8f);
    if (arrowDrawable != null) {
      arrowDrawable.setColorFilter(arrowColor, PorterDuff.Mode.SRC_IN);
    }
  }

  private void animateArrow(boolean shouldRotateUp) {
    int start = shouldRotateUp ? 0 : 10000;
    int end = shouldRotateUp ? 10000 : 0;
    ObjectAnimator animator = ObjectAnimator.ofInt(arrowDrawable, "level", start, end);
    animator.start();
  }

  /**
   * Set the maximum height of the dropdown menu.
   *
   * @param height the height in pixels
   */
  public void setDropdownMaxHeight(int height) {
    popupWindowMaxHeight = height;
    popupWindow.setHeight(calculatePopupWindowHeight());
  }

  /**
   * Set the height of the dropdown menu
   *
   * @param height the height in pixels
   */
  public void setDropdownHeight(int height) {
    popupWindowHeight = height;
    popupWindow.setHeight(calculatePopupWindowHeight());
  }

  private int calculatePopupWindowHeight() {
    if (adapter == null) {
      return WindowManager.LayoutParams.WRAP_CONTENT;
    }
    float itemHeight = getResources().getDimension(R.dimen.ms__item_height);
    float listViewHeight = adapter.getCount() * itemHeight;
    if (popupWindowMaxHeight > 0 && listViewHeight > popupWindowMaxHeight) {
      return popupWindowMaxHeight;
    } else if (popupWindowHeight != WindowManager.LayoutParams.MATCH_PARENT
        && popupWindowHeight != WindowManager.LayoutParams.WRAP_CONTENT
        && popupWindowHeight <= listViewHeight) {
      return popupWindowHeight;
    } else if (listViewHeight == 0 && adapter.getItems().size() == 1) {
      return (int) itemHeight;
    }
    return (int) listViewHeight;
  }

  /**
   * Get the {@link PopupWindow}.
   *
   * @return The {@link PopupWindow} that is displayed when the view has been clicked.
   */
  public PopupWindow getPopupWindow() {
    return popupWindow;
  }

  /**
   * Get the {@link ListView} that is used in the dropdown menu
   *
   * @return the ListView shown in the PopupWindow.
   */
  public ListView getListView() {
    return listView;
  }

  /**
   * Interface definition for a callback to be invoked when an item in this view has been selected.
   *
   * @param <T> Adapter item type
   */
  public interface OnItemSelectedListener<T> {

    /**
     * <p>Callback method to be invoked when an item in this view has been selected. This callback is invoked only when
     * the newly selected position is different from the previously selected position or if there was no selected
     * item.</p>
     *
     * @param view The {@link MaterialSpinner} view
     * @param position The position of the view in the adapter
     * @param id The row id of the item that is selected
     * @param item The selected item
     */
    void onItemSelected(MaterialSpinner view, int position, long id, T item);
  }

  /**
   * Interface definition for a callback to be invoked when the dropdown is dismissed and no item was selected.
   */
  public interface OnNothingSelectedListener {

    /**
     * Callback method to be invoked when the {@link PopupWindow} is dismissed and no item was selected.
     *
     * @param spinner the {@link MaterialSpinner}
     */
    void onNothingSelected(MaterialSpinner spinner);
  }
}