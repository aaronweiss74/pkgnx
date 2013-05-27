# pkgnx #
pkgnx is a simple Java library for the NX file format. The format was designed by [Peter Atashian](http://github.com/retep998) and [angelsl](https://github.com/angelsl) and has a public specification available [here](http://nxformat.github.io/). The format intended on replacing the proprietary WZ data format designed by Wizet for the popular game, [MapleStory](http://www.maplestory.com).

## Using pkgnx ##
Using pkgnx is really quite simple! The first step, of course, is to include the library as a dependency either by adding it to your classpath or adding it as a maven dependency. Once that's done, you can start coding away. The code itself is quite simple.

```java
    NXFile file = new NXFile("path/to/file");
    file.parse();
    // Do stuff, like...
    System.out.println(file.getRoot().getName());
```
    
You can also parse the file immediately through the constructor like so: `NXFile file = new NXFile("path/to/file", true);`

## Acknowledgements ##

* [Peter Atashian](http://github.com/retep998) and [angelsl](https://github.com/angelsl) for designing the PKG specification.
* [Cedric Van Goethem](https://github.com/Zepheus) for creating [javanx](https://github.com/Zepheus/javanx), the PKG3 Java NX library.
* [angelsl](https://github.com/angelsl) for creating [libjinx](https://github.com/angelsl/ms-libjinx), the first Java NX library.