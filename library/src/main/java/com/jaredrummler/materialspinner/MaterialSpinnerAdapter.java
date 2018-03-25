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
import java.util.List;

public class MaterialSpinnerAdapter<T> extends MaterialSpinnerBaseAdapter {

  private final List<T> items;

  public MaterialSpinnerAdapter(Context context, List<T> items) {
    super(context);
    this.items = items;
  }

  @Override public int getCount() {
    int size = items.size();
    if (size == 1 || isHintEnabled()) return size;
    return size - 1;
  }

  @Override public T getItem(int position) {
    if (isHintEnabled()) {
      return items.get(position);
    } else if (position >= getSelectedIndex() && items.size() != 1) {
      return items.get(position + 1);
    } else {
      return items.get(position);
    }
  }

  @Override public T get(int position) {
    return items.get(position);
  }

  @Override public List<T> getItems() {
    return items;
  }
}