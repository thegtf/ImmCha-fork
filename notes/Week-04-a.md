# Week04a Notes
2026-01-27 Tuesday

## First Half

### Review from Last Time
* Transferring Cheese back to King
* Radio Communication
	* Shared Array
	* Squeaks
* Subclasses for Different Robots or Behavior
### Announcements
* [US Qualifier Tournament](https://www.youtube.com/watch?v=w7vNMi8vWQo) to seed the Top 16 for final submission on Thursday
* Reference Player is now available for online scrimmages
	* This is how you'll earn credit in this class!
* I'll ask them to keep scrimmages open for February so we have time to run our players.
* We'll submit our players, but don't worry, we'll continue with our class after the competition is over.
### Squeaks (Rat-to-Rat)
* We'll have baby rats communicate cheese mines when they come back to the rat king.
* The rat king will print it out
### Shared Array
* The rat king will broadcast this location to all baby rats
* Baby rats will head to a random cheese target.

## Break

## Second Half

### Forking Class Repo

To keep each team's code secret from each other, I'll ask each team's gitter to fork the class repo and add their teammates as collaborators.

In breakout rooms:

#### 1. Fork repo

Decide who will be the team gitter (whoever has most experience with `git`)
![[git-fork-repo.png]]
#### 2. Add collaborators

Add other teammates GitHub usernames as collaborators by first clicking "Settings" on your fork (*not* the class repo)

![[git-repo-settings.png]]

Then choosing "Collaborators and teams", signing in or confirming your passkey as necessary.

<img src="git-repo-collabs.png" width="300" />

![[git-collabs-add.png]]
#### 3. Add this repo as a new remote

Your local working repo has a remote called `origin` which points to the original class monorepo.

```
git remote -v
```


You will add another remote called `team` that will point to your fork's clone URL.

```
git remote add team git@github.com:<username>/dgp-26wi.git 
```

From now on, any private work you want only your team to see (like your Battlecode player code), you will push to / pull from `team` only.

```
git pull team main
```

instead of just

```
git pull
```

and

```
git push team main
```
instead of just
```
git push
```

#### 4. Create a new player directory

In `java/src`, create a new directory named after your team, such as `shredders` or `immortal-chariot`.

Create at least one file, called `RobotPlayer.java`, which you can copy from any of the class players we have been working on together, or the `lectureplayer`.

#### 5. Build and play a match

Build your new player.
```
./gradlew build
```

and run a match with it by editing `gradle.properties`

and running

```
./gradlew run > match.log
```

#### 6. Strategy

Come up with an English sentence describing part of your strategy that you would like to implement over the next week.

Create a GitHub issue with this sentence, similar to the one we wrote in class together.

#### 7. Implement

For the rest of class, work on implementing your strategy.
During your weekly 
