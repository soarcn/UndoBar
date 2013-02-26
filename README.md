UndoBar
=======

The UI component for Android advanced UI pattern undo-bar, used in Gmail app, create by roman nurik

UndoBar was modify from Roman(@romannurik)'s undobar concept (https://code.google.com/p/romannurik-code/), I backport it to Android 2.x and make it more easy to use in your andorid application

![UndoBar Sample](https://github.com/soarcn/UndoBar/blob/master/art/screen.png?raw=true)

How to use this libary
=======

- download this libary, unzip to folder.
- import to your eclipse IDE ( eclipse...)
- use it as libary project.

API
=======

- you can use UndoBar by just one line code

``` java
    UndoBarController.show(getActivity(), "Undo-bar title" , listener, undoToken, false);
```

- UndoBar support customize style with backgroud/icon/duration attribution. libary provide 2 Style as default, you can use in different purpose like Undo or Refresh.


Contribute
=======

- feel free to Fork it


Other
=======
- Works perfect with Crouton/Menudrawer/ABS
- Will be release to Maven responsitory soon!
