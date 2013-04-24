UndoBar
=======

The UI component for Android advanced UI pattern undo-bar, used in Gmail app, create by roman nurik

UndoBar was modify from Roman(@romannurik)'s undobar concept (https://code.google.com/p/romannurik-code/), I backport it to Android 2.x and make it easier to use

![UndoBar Sample](https://github.com/soarcn/UndoBar/blob/master/art/redo.png?raw=true)
![UndoBar Sample](https://github.com/soarcn/UndoBar/blob/master/art/refresh.png?raw=true)
![UndoBar Sample](https://github.com/soarcn/UndoBar/blob/master/art/customize.png?raw=true)

How to use this libary
=======

- Download this libary, import to your IDE (eclipse...) as libary project


API
=======

- You can use UndoBar by just one line code

``` java
    UndoBarController.show(getActivity(), "Undo-bar title" , listener, undoToken);
```

- UndoBar support customize style with backgroud/icon/duration attribution. libary provide 3 Style as default, you can use in different purpose like Undo ,Refresh, or just use it as replacement of Toster


Contribute
=======

- Feel free to fork it


Other
=======
- Works perfect with Crouton/Menudrawer/ABS
