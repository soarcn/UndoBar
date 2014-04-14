UndoBar
=======

This is a implementation for Android advanced UI pattern undo-bar, used in Gmail app, create by roman nurik

UndoBar was modified from Roman(@romannurik)'s undobar concept (https://code.google.com/p/romannurik-code/), and backport it to Android 2.x and make it easier to use

![UndoBar Sample](https://github.com/soarcn/UndoBar/blob/master/art/kitkat.png?raw=true)
![UndoBar Sample](https://github.com/soarcn/UndoBar/blob/master/art/redo.png?raw=true)
![UndoBar Sample](https://github.com/soarcn/UndoBar/blob/master/art/refresh.png?raw=true)
![UndoBar Sample](https://github.com/soarcn/UndoBar/blob/master/art/customize.png?raw=true)


How to use this library
=======

- Download this library, import to your IDE (eclipse...) as a library project.
- Using Gradle(from 0.5):

```xml
compile 'com.cocosw:undobar:1.+@aar'
```

- Using Maven(<0.4)

```xml
<dependency>
    <groupId>com.cocosw</groupId>
    <artifactId>undobar</artifactId>
    <version>0.4</version>
    <type>apklib</type>
</dependency>
```



API
=======

- You can use UndoBar by just one line code

``` java
    UndoBarController.show(getActivity(), "Undo-bar title" , listener, undoToken);
```
- You can also use UndoBar in builder style.

``` java
    new UndoBar(getActivity()).message("Undo-bar title").listener(listener).show;
```

- UndoBar support customize style with background/icon/duration/animation attribution.
- UndoBar provides 3 default styles. You can use them for different purpose like undo,refresh, or just use it as replacement of Toasts.
- UndoBar will switch to KitKat look and feel in API-19 target.
- UndoBar will determine if tranlucent mode(4.4) is using and adjust its position.
- You can overwrite style in your project to change the look and feel. For example, you can use following lines in your style.xml to always use KitKat style UndoBar.

```xml
    <style name="UndoBar" parent="UndoBarKitKat"/>
    <style name="UndoBarMessage" parent="UndoBarMessageKitKat"/>
    <style name="UndoBarButton" parent="UndoBarButtonKitKat"/>
```

Contribute
=======

- Feel free to fork it


Other
=======
- Works perfectly with Crouton/Menudrawer/ABS


License
=======

    Copyright 2011, 2014 Liao Kai

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
