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

package com.jaredrummler.materialspinner.example;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.jaredrummler.materialspinner.MaterialSpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String[] ANDROID_VERSIONS = {
            "Cupcake",
            "Donut",
            "Eclair",
            "Froyo",
            "Gingerbread",
            "Honeycomb",
            "Ice Cream Sandwich",
            "Jelly Bean",
            "KitKat",
            "Lollipop",
            "Marshmallow",
            "Nougat",
            "Oreo"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/jaredrummler/MaterialSpinner")));
                } catch (ActivityNotFoundException ignored) {
                }
            }
        });

        setupSpinner1();
        setupSpinner2();
    }

    private void setupSpinner1() {
        MaterialSpinner spinner = (MaterialSpinner) findViewById(R.id.spinner);
        spinner.setItems(ANDROID_VERSIONS);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                Snackbar.make(view, "Clicked " + item, Snackbar.LENGTH_SHORT).show();
            }
        });
        spinner.setOnNothingSelectedListener(new MaterialSpinner.OnNothingSelectedListener() {

            @Override
            public void onNothingSelected(MaterialSpinner spinner) {
                Snackbar.make(spinner, "Nothing selected", Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    private void setupSpinner2() {
        MaterialSpinner spinner2 = (MaterialSpinner) findViewById(R.id.spinner2);

        List cheeses = new ArrayList();
        cheeses.add(new Cheese("Edam", "Ne"));
        cheeses.add(new Cheese("Cheddar", "Uk"));
        cheeses.add(new Cheese("Brie", "Fr"));

        MyCustomCheeseAdapter adapter = new MyCustomCheeseAdapter(this, cheeses);
        spinner2.setAdapter(adapter);
        spinner2.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<Cheese>() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Cheese item) {
                Snackbar.make(view, "Cheese clicked " + item, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    class Cheese {
        String name;
        String origin;

        public Cheese(String name, String origin) {
            this.name = name;
            this.origin = origin;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Cheese{");
            sb.append("name='").append(name).append('\'');
            sb.append(", origin='").append(origin).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    class MyCustomCheeseAdapter extends MaterialSpinnerAdapter<Cheese> {

        public MyCustomCheeseAdapter(Context context, List<Cheese> items) {
            super(context, items);
        }

        @Override
        public String getItemText(int position) {
            return super.getItem(position).name;
        }
    }

}
