UndoBar
=======

This is a implementation for Android advanced UI pattern undo-bar, used in Gmail app, create by roman nurik

UndoBar was modify from Roman(@romannurik)'s undobar concept (https://code.google.com/p/romannurik-code/), and backport it to Android 2.x and make it easier to use

![UndoBar Sample](https://github.com/soarcn/UndoBar/blob/master/art/redo.png?raw=true)
![UndoBar Sample](https://github.com/soarcn/UndoBar/blob/master/art/refresh.png?raw=true)
![UndoBar Sample](https://github.com/soarcn/UndoBar/blob/master/art/customize.png?raw=true)

How to use this libary
=======

- Download this libary, import to your IDE (eclipse...) as libary project.
- You can also depend on the .jar through Maven:

```xml
<dependency>
    <groupId>com.cocosw</groupId>
    <artifactId>undobar</artifactId>
    <version>0.3</version>
    <type>apklib</type>
</dependency>
```

API
=======

- You can use UndoBar by just one line code

``` java
    UndoBarController.show(getActivity(), "Undo-bar title" , listener, undoToken);
```

- UndoBar support customize style with backgroud/icon/duration attribution. libary provide 3 Style as default, you can use in different purpose like Undo,Refresh, or just use it as replacement of Toster


Contribute
=======

- Feel free to fork it


Other
=======
- Works perfect with Crouton/Menudrawer/ABS


License
=======

    Copyright 2011, 2012 Liao Kai

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
