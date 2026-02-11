package shredders;

import java.util.ArrayList;

import battlecode.common.*;

public class CheeseFinder extends BabyRat {
    public static enum State {
        FIND_CHEESE,
        RETURN_TO_KING,
    }

    public State currentState;
    public MapLocation mineLoc = null;
    public static int numMines = 0;
    public static ArrayList<Integer> mineLocs = new ArrayList<>();
    public MapLocation lastPathTarget = null;
    public final PathFinding pf = new PathFinding();

    public CheeseFinder(RobotController rc) {
        super(rc);
        currentState = State.FIND_CHEESE;
        rc.setIndicatorString("Cheesefinder reporting for duty");
    }

    public void doAction() throws GameActionException {
        //updateKingLocFromShared
        int kx = rc.readSharedArray(1);
        int ky = rc.readSharedArray(2);
        if (!(kx == 0 && ky == 0)) {
            kingLoc = new MapLocation(kx, ky);
    }
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
                            rc.setIndicatorString("Found a cheese mine at " + loc.toString());
                        }
                    }
                }

            if (cheeseLoc != null) {
                if (lastPathTarget == null || !lastPathTarget.equals(cheeseLoc)) {
                    pf.reset();
                    lastPathTarget = cheeseLoc;
                }
                pf.moveToTarget(rc, cheeseLoc, rc.getLocation());
                rc.setIndicatorString("finding cheese" + cheeseLoc);
                // no visible cheese, go to known mineLoc
            } else if (newCheeseMine != null) {
                if (lastPathTarget == null || !lastPathTarget.equals(newCheeseMine)) {
                    pf.reset();
                    lastPathTarget = newCheeseMine;
                }
                pf.moveToTarget(rc, newCheeseMine, rc.getLocation());
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
        int rawCheese = rc.getRawCheese();

        if (kingLoc == null) {
            //no beacon yet, return to FIND_CHEESE
            currentState = State.FIND_CHEESE;
            return;
        }

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
                    boolean result = (rawCheese > 0) && rc.canTransferCheese(actualKingLoc, rawCheese);
                    rc.setIndicatorString("Can transfer " + rawCheese + " to king at " + actualKingLoc.toString() + "? " + result);
                    if (result) {
                        rc.transferCheese(actualKingLoc, rawCheese);
                        currentState = State.FIND_CHEESE;
                        pf.reset();
                        lastPathTarget = null;
                        return;
                    }
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

        if (rc.canMove(toKing)) {
            rc.move(toKing);
        } else {
            Direction left = toKing.rotateLeft();
            Direction right = toKing.rotateRight();
            if (rc.canMove(left)) rc.move(left);
            else if (rc.canMove(right)) rc.move(right);
            else RobotSubPlayer.moveRandom(rc);
        }
                if (rawCheese == 0) {
                    currentState = State.FIND_CHEESE;
        }
    }
}
        

   