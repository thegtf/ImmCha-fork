package teacher05a;

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

    public CheeseFinder(RobotController rc) {
        super(rc);
        currentState = State.FIND_CHEESE;
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

        System.out.println("Sensed " + nearbyInfos.length + " tiles");
        MapLocation cheeseLoc = null;
        for (MapInfo info : nearbyInfos) {
            MapLocation loc = info.getMapLocation();
            if (info.getCheeseAmount() > 0) {
                Direction toCheese = rc.getLocation().directionTo(loc);

                if (rc.canTurn(toCheese)) {
                    rc.turn(toCheese);
                    cheeseLoc = info.getMapLocation();
                    break;
                }
            }
            if (info.hasCheeseMine()) {
                mineLoc = info.getMapLocation();
                System.out.println("Found a cheese mine at " + mineLoc.toString());
                rc.setIndicatorString("Found a cheese mine at " + mineLoc.toString());
            }
            if (rc.canRemoveDirt(loc)) {
                rc.removeDirt(loc);
            }
        }

        if (rc.canMoveForward()) {
            rc.moveForward();
            rc.setIndicatorString("Finding cheese.");
        } else {
            d = directions[rand.nextInt(directions.length-1)];
            if (rc.canTurn()) {
                rc.turn(d);
            }
            rc.setIndicatorString("Blocked while finding cheese, turning " + d.toString());
            return;
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

            RobotInfo[] kingLocations = rc.senseNearbyRobots(kingLoc, 8, rc.getTeam());

            for (RobotInfo robotInfo : kingLocations) {
                if (robotInfo.getType().isRatKingType()) {
                    MapLocation actualKingLoc = robotInfo.getLocation();
                    boolean result = rc.canTransferCheese(actualKingLoc, rawCheese);
                    rc.setIndicatorString("Can transfer " + rawCheese + " to king at " + actualKingLoc.toString() + "? " + result);
                    if (result) {
                        rc.transferCheese(actualKingLoc, rawCheese);
                        currentState = State.FIND_CHEESE;
                    } else {
                        // Return to finding cheese, try to randomly come back to king another way
                        currentState = State.FIND_CHEESE;
                    }
                    break;
                } else {
                    // we found another baby rat, squeak or read squeaks,
                    // based on whether we have a mine location to go to
                    if (mineLoc != null) {
                        int msgBytes = getSqueak(SqueakType.CHEESE_MINE, toInteger(mineLoc));
                        rc.squeak(msgBytes);
                        System.out.println("From " + here.toString() + " Sent a squeak " + msgBytes + " for mine at " + mineLoc.toString());
                        // go back to finding cheese mode
                    } else {
                        Message[] squeakMessages = rc.readSqueaks(rc.getRoundNum());

                        for (Message m : squeakMessages) {
                            int msg = m.getBytes();
                            if (getSqueakType(msg) == SqueakType.CHEESE_MINE) {
                                int encodedLoc = getSqueakValue(msg);
                                mineLoc = new MapLocation(getX(encodedLoc), getY(encodedLoc));
                            }
                        }
                    }
                    // whether we already have a mine location or just received one
                    // go back to mining cheese
                    currentState = State.FIND_CHEESE;
                }
            }

        }

        if (rc.canRemoveDirt(nextLoc)) {
            rc.removeDirt(nextLoc);
        }

        // TODO replace with pathfinding for the pathfinding lecture
        if (rc.canMove(toKing)) {
           rc.move(toKing);
        }


        if (rawCheese == 0) {
            currentState = State.FIND_CHEESE;
        }
        
    }


}
