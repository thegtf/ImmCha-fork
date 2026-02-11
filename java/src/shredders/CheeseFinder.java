package shredders;

import java.util.ArrayList;

import battlecode.common.*;

public class CheeseFinder extends BabyRat {
    public static enum State {
        FIND_CHEESE,
        RETURN_TO_KING,
    }

    public static State currentState;
    public static MapLocation mineLoc = null;
    public static int numMines = 0;
    public static ArrayList<Integer> mineLocs = new ArrayList<>();
    static MapLocation lastPathTarget = null;

    public CheeseFinder(RobotController rc) {
        super(rc);
        int rawCheese = rc.getRawCheese();
        if (rawCheese == 0) currentState = State.FIND_CHEESE;
        rc.setIndicatorString("Cheesefinder reporting for duty");
    }

    public void doAction() throws GameActionException {
        switch (currentState) {
            case FIND_CHEESE:
                runFindCheese();
                break;
            case RETURN_TO_KING:
                runReturnToKing();
                break;
        }
    }


    public void runFindCheese() throws GameActionException {
        // search for cheese
        MapInfo[] nearbyInfos = rc.senseNearbyMapInfos();
        MapLocation cheeseLoc = null;
        MapLocation newCheeseMine = null;

            System.out.println("Sensed " + nearbyInfos.length + " tiles");

            for (MapInfo info : nearbyInfos) {
                MapLocation loc = info.getMapLocation();

                if (info.getCheeseAmount() > 0 && cheeseLoc == null) {
                    cheeseLoc = loc;
                }

                    if (info.hasCheeseMine()) {
                        int encMine = toInteger(loc);
                        if (!mineLocs.contains(encMine)) {
                            mineLocs.add(encMine);
                            newCheeseMine = loc;
                            mineLoc = loc;
                            System.out.println("Found a new cheese mine at " + loc);
                        }
                    }
                
                if (info.hasCheeseMine()) {
                    MapLocation m = info.getMapLocation();
                    int encMine = toInteger(m);

                    if (!mineLocs.contains(encMine)) {
                        mineLocs.add(encMine);
                        newCheeseMine = m;
                        mineLoc = m;
                    }
                    System.out.println("Found a cheese mine at " + m.toString());
                    rc.setIndicatorString("Found a cheese mine at " + m.toString());
                }
                if (rc.canRemoveDirt(loc)) {
                    rc.removeDirt(loc);
                    return;
                }
            }

            if (cheeseLoc != null) {
                if (lastPathTarget == null || !lastPathTarget.equals(cheeseLoc)) {
                    PathFinding.reset();
                    lastPathTarget = cheeseLoc;
                }
                PathFinding.moveToTarget(rc, cheeseLoc, rc.getLocation());
                rc.setIndicatorString("finding cheese" + cheeseLoc);
                // no visible cheese, go to known mineLoc
            } else if (mineLoc != null) {
                if (lastPathTarget == null || !lastPathTarget.equals(mineLoc)) {
                    PathFinding.reset();
                    lastPathTarget = newCheeseMine;
                }
                PathFinding.moveToTarget(rc, mineLoc, rc.getLocation());
                rc.setIndicatorString("finding cheese mine at " + mineLoc.toString());
            } else {
                // no visible cheese or known mine, keep wandering
                RobotSubPlayer.moveRandom(rc);
                rc.setIndicatorString("wandering for cheese");
            }


        if ((cheeseLoc != null) && rc.canPickUpCheese(cheeseLoc)) {
            rc.pickUpCheese(cheeseLoc);
            currentState = State.RETURN_TO_KING;
            rc.setIndicatorString("Returning to king.");
        }
    }



    public void runReturnToKing() throws GameActionException {
        MapLocation here = rc.getLocation();
        Direction toKing = here.directionTo(kingLoc);
        MapLocation nextLoc = here.add(toKing);
        int rawCheese = rc.getRawCheese();

        if (rc.canTurn(toKing)) {
            rc.turn(toKing);
        }

        if (rc.canSenseLocation(kingLoc) && (kingLoc.distanceSquaredTo(here) <= 4 )) {

            RobotInfo[] nearbyRobots = rc.senseNearbyRobots(kingLoc, 8, rc.getTeam());

            for (RobotInfo robotInfo : nearbyRobots) {
                if (robotInfo.getTeam() != rc.getTeam()) {
                    continue;
                }
                if (robotInfo.getType().isRatKingType()) {
                    MapLocation actualKingLoc = robotInfo.getLocation();
                    boolean result = rc.canTransferCheese(actualKingLoc, rawCheese);
                    rc.setIndicatorString("Can transfer " + rawCheese + " to king at " + actualKingLoc.toString() + "? " + result);
                    if (result) {
                        rc.transferCheese(actualKingLoc, rawCheese);
                    }
                    break;
                } else {
                    // we found another baby rat, squeak or read squeaks,
                    // based on whether we have a mine location to go to


                    if (mineLoc != null) {
                        int msgByte = getSqueak(SqueakType.CHEESE_MINE, toInteger(mineLoc));
                        rc.squeak(msgByte);
                        System.out.println("From " + here.toString() + " Sent a squeak " + msgByte + " for mine at " + mineLoc.toString());
                        // go back to finding cheese mode
                        // if we have squeaked to at least one other baby rat
                        currentState = State.FIND_CHEESE;
                        break;
                    } else {
                            Message[] squeakMessages = rc.readSqueaks(rc.getRoundNum());

                            for (Message m : squeakMessages) {
                                int msg = m.getBytes();
                                if (getSqueakType(msg) == SqueakType.CHEESE_MINE) {
                                    int encodedLoc = getSqueakValue(msg);
                                    mineLoc = new MapLocation(getX(encodedLoc), getY(encodedLoc));
                                    System.out.println("Received cheese mine " + mineLoc.toString());
                                    // return to cheese finding
                                    // if we received a squeak telling us about a mine
                                    currentState = State.FIND_CHEESE;
                                    break;
                                }
                            }

                        }
                    }
                }
            }

        if (rc.canRemoveDirt(nextLoc)) {
            rc.removeDirt(nextLoc);
            return;
        }
        if (rc.canMove(toKing)) {
            rc.move(toKing);
        } else if (rc.canMoveForward()) {
            rc.moveForward();
        } else {
            RobotSubPlayer.moveRandom(rc);
}
        if (rawCheese == 0) {
            currentState = State.FIND_CHEESE;
        }
    }
}
        

   