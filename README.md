Super Mario Bros. Clone
==========
![image](logo.png)

A clone of Super Mario Bros. 16-bit version game.

**NOTE:** Original assets have been replaced!

### About
This game is based of the Super Mario Bros. 16-bit version featured on Super Mario All-Starts for the Super Nintendo (SNES). The game is written in Java an using the [LibGdx](http://libgdx.badlogicgames.com/) engine.

Features of the game:
* Mini Mario turns into big Mario with the help of mushrooms.
* If you walk of the platform into the deep, you will die.
* Goomba's (enemies) will kill Mario, Mario can kill the Goomba's by jumping on their heads.
* Jump up against bricks, they will be destroyed if you are big Mario.
* Bonus bricks which can contain multiple Mushrooms.
* At the end of the level you can slide down the flag, you will then walk automatically to the castle. The game will then restart.

### Demo video

[![Youtube video](http://img.youtube.com/vi/GxyUYAL4O7I/0.jpg)](http://www.youtube.com/watch?v=GxyUYAL4O7I)

### Development setup

Using IntelliJ:

1.  Install Java JDK. Make sure JAVA_HOME is in your environment variables.
2.   Install the Android SDK and setup an Android platform. Make sure to set ANDROID_HOME in your environment variables.
3.  Install Gradle.
3.  Clone this repository. Open the project in your IDE.
If you are using IntlliJ the Gradle plugin should be installed by default. The gradle project settings should popup.
Accept the default settings. If it is missing the Android SDK
 or JAVA_HOME make sure to setup Java and Android SDK in your environment variables.
4. Edit your run configuration. Make sure `android/assets` is set as your working directory!
5. When running in IntelliJ it should automatically compile.
