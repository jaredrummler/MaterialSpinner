Material Spinner
================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jaredrummler/material-spinner/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jaredrummler/material-spinner) [![License](http://img.shields.io/:license-apache-blue.svg)](LICENSE) [![API](https://img.shields.io/badge/API-14%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=14) [![Twitter Follow](https://img.shields.io/twitter/follow/jrummy16.svg?style=social)](https://twitter.com/jrummy16)

![DEMO GIF](demo.gif "DEMO")

Usage
-----

Add the spinner to your layout XML:

```xml
<com.jaredrummler.materialspinner.MaterialSpinner
    android:id="@+id/spinner"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"/>
```

Add items to the spinner and listen for clicks:

```java
MaterialSpinner spinner = (MaterialSpinner) findViewById(R.id.spinner);
spinner.setItems("Ice Cream Sandwich", "Jelly Bean", "KitKat", "Lollipop", "Marshmallow");
spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

  @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
    Snackbar.make(view, "Clicked " + item, Snackbar.LENGTH_LONG).show();
  }
});
```

You can add attributes to customize the view. Available attributes:

| name                | type    | info                                                   |
|---------------------|---------|--------------------------------------------------------|
| ms_arrow_tint       | color   | sets the color on the drop-down arrow                  |
| ms_hide_arrow       | boolean | set to true to hide the arrow drawable                 |
| ms_background_color | color   | set the background color for the spinner and drop-down |
| ms_text_color       | color   | set the text color                                     |

Download
--------

Download [the latest AAR](https://repo1.maven.org/maven2/com/jaredrummler/material-spinner/1.0.5/material-spinner-1.0.5.aar) or grab via Gradle:

```groovy
compile 'com.jaredrummler:material-spinner:1.0.5'
```
or Maven:
```xml
<dependency>
  <groupId>com.jaredrummler</groupId>
  <artifactId>material-spinner</artifactId>
  <version>1.0.5</version>
  <type>aar</type>
</dependency>
```

Credits
-------

This library is based on [Nice Spinner](https://github.com/arcadefire/nice-spinner) by Angelo Marchesin

License
--------

    Copyright (C) 2016 Jared Rummler

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
