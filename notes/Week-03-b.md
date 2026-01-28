# Week 03b Notes
2026-01-22

## First Half

### Review from Last Time
* Four Git File Statuses
	* Committed Clean
	* Untracked
	* Modified Unstaged
	* Modified Staged
* Baby Rat modes:
	* Explore and find cheese and pick it up
	* Return to king

### Transfer Cheese

Once baby rats return to their king, or close enough
* they should transfer their cheese

* What two or three lines can we add to our baby rat strategy to accomplish this?
* What are the API methods for RobotController that let us do this?
	* Hint: one to check if we are close to our rat king
	* one to transfer cheese
* Which mode of the baby rat strategy should we use? 
### Class Activity (in Breakout Rooms)

### Git Commands

* `git status`
* `git add`
* `git commit`
* `git push`
* `git pull`

## Break

## Second Half

### Radio Communication

Two kinds
* Shared array (one-way broadway, rat kings to the everyone)
* Squeaks (peer-to-peer, baby rats) to all rats on locations within this range

Questions:
* Can you hear squeaks from the other rat team, or from cats?
* Can cats hear your squeaks?
	* Do they move toward you if you squeak near them?

#### Shared Array

Communication from (any) Rat King to (all) Baby Rats (one-way only) and also fellow Rat Kings

* 64 slots of 10 bits each
* Index of 0 to 63
* Can store numeric value of 0 to 1023

* No discernible sender / receiver.
* Not tied to round number
	* Once data is written, it can be read until the end of the game
* Distance doesn't matter.
* Rat Kings can write (and overwrite) in any of the slots, any number of times
* Anyone can read any of the slots, any number of times
* As a team, you agree what each of the slots and values mean.

![[rat-shared-array.png]]

Like a shared radio program in the airwaves.
#### Squeak (Messages)

* Defined sender ID
	* You can tell which robot sent a message
* Can only send to locations within range ()
	* or rather, units on those locations
* Tied to the specific round number when squeak is sent (only lasts for 5 rounds after that)
* Once you read / retrieve a message 
* The limit of 5 rounds lets you have some slack between your strategy of sending and receiving
```
public Message(int bytes, int senderID, int round, MapLocation sourceLoc) {

this.senderID = senderID;

this.round = round;

this.bytes = bytes;

this.sourceLoc = sourceLoc;

}
```

* Can send 4 bytes (4 slots of 8 bits each)
