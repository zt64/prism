# prism
A minimal window manager written in [Kotlin](https://kotlinlang.org/)

## Building

---

Install dependencies using your system package manager.

| Distro               | Dependencies                                                               |
|----------------------|----------------------------------------------------------------------------|
| Arch Linux           | glibc libx11 libxft libxpm freetype2                                       |
| Debian (Ubuntu, etc) | libc6-dev libx11-dev libxft-dev libxpm-dev libfreetype-dev build-essential |

You should now be able to run `./gradlew build` to build Prism.<br>
Outputs are in `build/bin/native`. `prismc` is a client application, `prism` is the server.