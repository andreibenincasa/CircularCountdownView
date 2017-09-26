# CircularCountdownView 
[![](https://jitpack.io/v/andreibenincasa/CircularCountdownView.svg)](https://jitpack.io/#andreibenincasa/CircularCountdownView) [![API](https://img.shields.io/badge/API-15%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=15)
 
A basic and customizable circular countdown view for Android.
# New Features!

  - A listerner to tell when the progress reached the duration.

## Usage

Add the following to your project level `build.gradle`:
 
```gradle
allprojects {
	repositories {
		maven { url "https://jitpack.io" }
	}
}
```

Add this to your app `build.gradle`:
 
```gradle
dependencies {
	compile 'com.github.andreibenincasa:CircularCountdownView:1.0.1'
}
```

Add the CircularCountdonwView to your layout:
```xml
...
<com.abs.ccv.CircularCountdownView
        android:id="@+id/ccv"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_gravity="center"
        android:layout_weight="1"
        ccv:progressWidth="12dp"
        ccv:strokeWidth="12dp"
        ccv:backgroundColor="@color/colorPrimaryDark"
        ccv:progressColor="@color/colorAccent"
        ccv:strokeColor="@color/colorPrimary" />
```

And finally setup the view to your code setting the attributes:
```java
public class MainActivity extends AppCompatActivity implements CircularCountdownViewListener {

    CircularCountdownView ccv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ccv = (CircularCountdownView) findViewById(R.id.ccv);
        ccv.setDuration(30 * 1000);
        ccv.setInitialProgress(15 * 1000);
        ccv.setListener(this);
    }

    @Override
    public void onCountdownFinished() {
        Toast.makeText(this, "onCountdownFinished", Toast.LENGTH_LONG).show();
        ccv.setInitialProgress(0);
    }
}
```

## License
```
Copyright 2017 Andrei Benincasa

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
