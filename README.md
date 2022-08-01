# prism
A minimal window manager written in [Kotlin](https://kotlinlang.org/)

## Building
### Ubuntu/Debian with APT
Tested working on Ubuntu 21.10<br>
<br>
Install dependencies with APT: `sudo apt install libc6-dev libx11-dev libxft-dev libxpm-dev libfreetype-dev build-essential`<br>
Add `-I/usr/include/x86_64-linux-gnu` to `compilerOpts` in `src/nativeInterop/cinterop/xlib.def`.<br>
Your `compilerOpts` in `xlib.def` should look like this:<br>
`compilerOpts = -I/usr/include -I/usr/include/freetype2 -I/usr/include/x86_64-linux-gnu`<br>
You should now be able to run `gradlew build` to build Prism.<br>
Outputs are in `build/bin/native`. `prismc` is a client application, `prism` is the server.
