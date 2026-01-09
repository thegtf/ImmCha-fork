# Software Setup

We will emulate a Unix development environment on Windows for this class, so you can develop in Java
directly on your home machines.

You can follow [this video](https://www.youtube.com/watch?v=tcDLevC7wmU) to see the major steps.

0. Go to Battlecode 2026's quickstart page. This is where you should return if in doubt.
   - [https://play.battlecode.org/bc26java/home](https://play.battlecode.org/bc26/quick_start)

## Install Java and Git Bash

1. Install Oracle's Java Development Kit 21.
   - https://www.oracle.com/java/technologies/downloads/#jdk21-windows 
   
2. Install Git-Bash via the Git For Windows package by downloading and running the installer from here

  https://gitforwindows.org/

3. Open up Git-Bash (or a MINGW64 terminal window).


4. Test your Java installation
```
	java -version
```

## Configure Git SSH Key Access

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

## Clone Class Repo and Build

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

16. Run the Battlecode client, by tab-completing to see what executable file was compiled, then press Enter
```
./client/<tab tab>
```

17. Try and run the `week01a` player against itself as shown at the end of our first class.

Bring any questions and problems you encounter to class next time.
