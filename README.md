UndoBar
=======
[![Build Status](https://travis-ci.org/soarcn/UndoBar.svg?branch=master)](https://travis-ci.org/soarcn/UndoBar)

This lib is deprecated in favor of Google's Design Support Library which includes a Snackbar and is no longer being developed.

Thanks for all your support!


Android Library that implements Snackbars (former known as Undobar) from Google's Material Design documentation.

![UndoBar Sample](https://github.com/soarcn/UndoBar/blob/master/art/kitkat.png?raw=true)
![UndoBar Sample](https://github.com/soarcn/UndoBar/blob/master/art/redo.png?raw=true)
![UndoBar Sample](https://github.com/soarcn/UndoBar/blob/master/art/refresh.png?raw=true)
![UndoBar Sample](https://github.com/soarcn/UndoBar/blob/master/art/customize.png?raw=true)
![UndoBar Sample](https://github.com/soarcn/UndoBar/blob/master/art/material.png?raw=true)


How to use this library
=======

- Download this library, import to your IDE (eclipse...) as a library project.
- Using Gradle(from 0.5)

    ```groovy
    compile 'com.cocosw:undobar:1.+@aar' 
    ```
- Using Maven

    ```xml
    <dependency>
        <groupId>com.cocosw</groupId>
        <artifactId>undobar</artifactId>
        <version>*</version>
        <type>apklib</type>
    </dependency>
    ```

API
=======

- You can also use UndoBar with builder style.

    ``` java
    new UndoBar(getActivity()).message("Undo-bar title").listener(listener).show();
    ```
- Or you can use UndoBar by just one line code (Deprecated from 1.4)
    
    ``` java
    UndoBarController.show(getActivity(), "Undo-bar title" , listener, undoToken);
    ```

- UndoBar support customize style with background/icon/duration/animation attribution.

- UndoBar will determine if tranlucent mode(4.4) is using and adjust its position.


Style
========

- UndoBar provides 3 default styles. You can use them for different purposes like undo,refresh, or just use it as replacement of toasts.
 For example, you can use retry style in this way.

    ``` java
    new UndoBarController.UndoBar(this).message(loader.getException().getMessage()).style(UndoBarController.RETRYSTYLE).listener(this).show();
    ```

- UndoBar will switch to KitKat L&F in API-19 target and Material design in API20+.
- You can set theme in your appliation to change the look and feel. For example, you can use following lines in your style.xml to always use material style UndoBar.

    ```xml
    <style name="MaterialTheme" parent="android:Theme.Light">
        <item name="undoBarStyle">@style/UndoBarMaterialStyle</item>
    </style>
    ```

- You can change undobar style completely by define your own style

    ```xml
        <style name="UndoBarClassicStyle">
            <item name="containerStyle">@style/UndoBarClassic</item>
            <item name="messageStyle">@style/UndoBarMessageClassic</item>
            <item name="buttonStyle">@style/UndoBarButtonClassic</item>
            <item name="dividerStyle">@style/UndoBarDividerClassic</item>
            <item name="inAnimation">@anim/undobar_classic_in_anim</item>
            <item name="outAnimation">@anim/undobar_classic_out_anim</item>
        </style>
    ```

Advanced usage
======

- Using UndoBarController.AdvancedUndoListener if you need to get notification when UndoBar was cleared or hidden.
- UndoBar is designed to dynamically add to activity viewgroup, so you need to handle screen-rotation by your self. Check [this](https://github.com/soarcn/UndoBar/blob/master/example/src/com/cocosw/undobar/example/SnackBar.java) example to see more.


Contribute
=======

- Feel free to fork it


License
=======

    Copyright 2011, 2015 Kai Liao

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
