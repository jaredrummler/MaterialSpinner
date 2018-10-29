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

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import java.util.List;

public abstract class MaterialSpinnerBaseAdapter<T> extends BaseAdapter {

  private final Context context;
  private int selectedIndex;
  private int textColor;
  private int backgroundSelector;
  private int popupPaddingTop;
  private int popupPaddingLeft;
  private int popupPaddingBottom;
  private int popupPaddingRight;
  private boolean isHintEnabled;

  public MaterialSpinnerBaseAdapter(Context context) {
    this.context = context;
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    final TextView textView;
    if (convertView == null) {
      LayoutInflater inflater = LayoutInflater.from(context);
      convertView = inflater.inflate(R.layout.ms__list_item, parent, false);
      textView = (TextView) convertView.findViewById(R.id.tv_tinted_spinner);
      textView.setTextColor(textColor);

      textView.setPadding(popupPaddingLeft, popupPaddingTop, popupPaddingRight, popupPaddingBottom);
//
//      int left, right, bottom, top;
//      if (popupPaddingTop != -1) {
//        left = textView.getPaddingLeft();
//        right = textView.getPaddingRight();
//        bottom = textView.getPaddingBottom();
//
//        textView.setPadding(left, popupPaddingTop, right, bottom);
//      }
//
//      if (popupPaddingLeft != -1) {
//        top = textView.getPaddingTop();
//        right = textView.getPaddingRight();
//        bottom = textView.getPaddingBottom();
//
//        textView.setPadding(popupPaddingLeft, top, right, bottom);
//      }
//
//      if (popupPaddingBottom != -1) {
//        left = textView.getPaddingLeft();
//        top = textView.getPaddingTop();
//        right = textView.getPaddingRight();
//
//        textView.setPadding(left, top, right, popupPaddingBottom);
//      }
//
//      if (popupPaddingRight != -1) {
//        left = textView.getPaddingLeft();
//        top = textView.getPaddingTop();
//        bottom = textView.getPaddingBottom();
//
//        textView.setPadding(left, top, popupPaddingRight, bottom);
//      }

      if (backgroundSelector != 0) {
        textView.setBackgroundResource(backgroundSelector);
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        Configuration config = context.getResources().getConfiguration();
        if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
          textView.setTextDirection(View.TEXT_DIRECTION_RTL);
        }
      }
      convertView.setTag(new ViewHolder(textView));
    } else {
      textView = ((ViewHolder) convertView.getTag()).textView;
    }
    textView.setText(getItemText(position));
    return convertView;
  }

  public String getItemText(int position) {
    return getItem(position).toString();
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  public void notifyItemSelected(int index) {
    selectedIndex = index;
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override public abstract T getItem(int position);

  @Override public abstract int getCount();

  public abstract T get(int position);

  public abstract List<T> getItems();

  public void setHintEnabled(boolean isHintEnabled) {
    this.isHintEnabled = isHintEnabled;
  }

  public boolean isHintEnabled() {
    return this.isHintEnabled;
  }

  public MaterialSpinnerBaseAdapter<T> setTextColor(@ColorInt int textColor) {
    this.textColor = textColor;
    return this;
  }

  public MaterialSpinnerBaseAdapter<T> setBackgroundSelector(@DrawableRes int backgroundSelector) {
    this.backgroundSelector = backgroundSelector;
    return this;
  }

  public MaterialSpinnerBaseAdapter<T> setPopupPadding(int left, int top, int right, int bottom) {
    this.popupPaddingLeft = left;
    this.popupPaddingTop = top;
    this.popupPaddingRight = right;
    this.popupPaddingBottom = bottom;
    return this;
  }

  private static class ViewHolder {

    private TextView textView;

    private ViewHolder(TextView textView) {
      this.textView = textView;
    }
  }
}