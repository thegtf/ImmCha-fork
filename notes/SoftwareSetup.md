# Software Setup

We will emulate a Unix development environment for this class, so you can develop in Java
directly on your home machines. However, this setup will also work for lab computers on campus.

The directions for Mac and Linux are similar, and there is a separate track for Windows below.
They start out differently for Steps 1 through 3, then from Step 4 onwards they are all the same.

Start with the track that applies to your operating system.

Step 0: For all the track, start with Battlecode 2026's quickstart page. This is where you should return if in doubt.
   - [https://play.battlecode.org/bc26java/home](https://play.battlecode.org/bc26/quick_start)

NOTE: You must use Java 21 for this class. The Battlecode Engine is compiled for it and will not work with other versions,
especially the latest major release Java 25. You can use SDKman below if you need to use and switch between multiple
versions of Java.

## Mac and Linux Development, Steps 1-3

1. Download and install [SDKman](https://sdkman.io/), which is used to manage multiple versions of languages in the Java and Virtual Machine family,
like Scala, Kotlin, Clojure, and more. If you are using Java, especially different versions, for other classes and projects, SDKman is where it's at. In your Terminal:

```
curl -s "https://get.sdkman.io" | bash
```

2. Close the terminal and open it again. This will load `sdk` command from its startup script.

3. Install this version of Java 21:
```
sdk install java 21.0.2-graalce
```

Set it as the default, and use it for now.
```
sdk default java 21.0.2-graalce
sdk use java 21.0.2-graalce
```

## Windows Development, Steps 1-3

If you have Java 25 installed, uninstall it first.

You can follow [this video](https://www.youtube.com/watch?v=tcDLevC7wmU) to see the major steps.

### Install Java and Git Bash

1. Install Oracle's Java Development Kit 21.
   - https://www.oracle.com/java/technologies/downloads/#jdk21-windows 
   
2. Install Git-Bash via the Git For Windows package by downloading and running the installer from here

  https://gitforwindows.org/

3. Open up Git-Bash (or a MINGW64 terminal window).


### Check your Installation 

4. Test your Java installation
```
	java -version
```

If you tried to run the Gradle build tool before having the right Java version, you may have
a version of Gradle that will no longer run with Java 21, so delete any downloaded Gradles you have.

```
rm -Rf ~/.gradle
```

Not to worry, we'll run and download a Java 21 version of gradle below.

### Configure Git SSH Key Access

5. Create your SSH keypair (we've been using the ECDSA cryptosystem, but you can use any other supported cryptosystem)

```
ssh-keygen -t ecdsa
```

And then press enter 3 times, to accept default key location

6. Dump the public key to standard output on the command line
```
cat ~/.ssh/id_ecdsa.pub
```
Select and copy the whole line.

If you used a different cryptosystem, substitute that for the public key filesystem.

7. Go to https://github.com/settings/profile

8. Click on “SSH and GPG Keys” on left sidebar

9. Click the blue “New SSH Key” button

10. Paste into the big text input
11. Give the key a title like “Git Bash”
12. And click “Add SSH Key”

### Clone Class Repo and Build

13. Clone the class repo (you'll need to create a github account first and submit your username as part of HW1 on Canvas)
by pasting the following into the Git Bash command-line.
```
git clone git@github.com:TheEvergreenStateCollege/dgp-26wi.git
```

14. Change into your cloned directory and build the Battlecode client
```
cd dgp-26wi/java
```

15. Update the game engine version and build it
```
./gradlew update
./gradlew build
```

16. Run the Battlecode client by finding it in the `dgp-26wi/java/client` folder of your graphical shell (Windows Explorer, Mac Finder, Linux Gnautilus, or similar) and double-clicking it.

### Run Battlecode Matches

You will follow some form of this development cycle for the rest of class:

A. Write and save code in VSCode, in `*.java` files inside your player package directory.
B. Build it and fix any errors. Go back to Step A. as necessary.
  * `./gradlew build`
C. Run the match in Battlecode, observe behavior, read the log files or console to debug. Go back to Step A as necessary.

17. Try and run the `examplefuncsplayer` player against itself as shown at the end of our first class.

You can see these steps [in this section of the class screenrecording video](https://evergreen.hosted.panopto.com/Panopto/Pages/Viewer.aspx?id=18edeacf-2800-4ca6-895a-b3cb0047be74&start=3670).

In Windows and Linux, you can use the `Runner` tab in the Battlecode Client.

<img width="391" height="701" alt="image" src="https://github.com/user-attachments/assets/19da8e6e-31fd-45f3-811e-f98601208f05" />

On Mac, you can use run the match on the command-line and save the log file as necessary.

Edit the file `gradle.properties` to have the desired player packages for `teamA` and `teamB`, and change the `map` name to be the
map that you want to run the match on.

<img width="766" height="660" alt="image" src="https://github.com/user-attachments/assets/6c889060-3122-4e43-9ff6-8d857316296a" />


```
./gradlew run | tee match.log
```

Then in the graphical client, use the `Queue` tab in the Battlecode Client. Choose the match file if it's already present (it will auto-select the latest one and outline it in pink if possible). If you can't find your desired match, click "Upload .bc26 file" and find it in the `./matches` directory.

<img width="1339" height="840" alt="image" src="https://github.com/user-attachments/assets/20946572-2c96-4d6c-82b3-e505b38c8e57" />

Then click the play button near the bottom right. You're running a match!

<img width="628" height="63" alt="image" src="https://github.com/user-attachments/assets/ea19c057-8de3-48df-83e9-7c418cf3ebd1" />

Unfortunately on Mac/Linux, you cannot click on the Console button to view log messages, so  you must examine the
file `match.log` that you created above.

You can open it in the VS Code.

You are welcome to post any questions or problems you encounter [to the Canvas discussion boards](https://canvas.evergreen.edu/courses/7979/discussion_topics), or bring it to class next time.
