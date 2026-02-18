package teacher06b;

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
        MapLocation cheeseLoc = null;
        MapLocation here = rc.getLocation();

        if ((mineLoc == null) || (mineLoc.distanceSquaredTo(here) < 25)) {
            // If we don't know of a cheese mine yet, or we're close enough
            // just sense for cheese and pick them up
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
            }
        } else {
            // We know of a cheese mine, get closer because we are too far away
            System.out.println("Going straight to cheese mine at " + mineLoc.toString());
            Direction toMine = rc.getLocation().directionTo(mineLoc);
            if (rc.canTurn(toMine)) {
                rc.turn(toMine);
            }
        }

        MapLocation forward = here.add(rc.getDirection());
        if (rc.canRemoveDirt(forward)) {
            rc.removeDirt(forward);
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

        // Only turn to the king if we are unstuck
        if (gettingUnstuck && rc.canTurn(toKing)) {
            rc.turn(toKing);
        }

        if (rc.canSenseLocation(kingLoc) && (kingLoc.distanceSquaredTo(here) <= 16 )) {

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
                    continue; // try to find other baby rats to squeak to
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
                        return;
                    } else {
                        boolean receivedMineLoc = false;
                        //while (!receivedMineLoc) {
                            Message[] squeakMessages = rc.readSqueaks(rc.getRoundNum());
                            System.out.println("Received " + squeakMessages.length + " messages");

                            for (Message m : squeakMessages) {
                                int msg = m.getBytes();
                                if (getSqueakType(msg) == SqueakType.CHEESE_MINE) {
                                    int encodedLoc = getSqueakValue(msg);
                                    mineLoc = new MapLocation(getX(encodedLoc), getY(encodedLoc));
                                    System.out.println("Received cheese mine " + mineLoc.toString());
                                    receivedMineLoc = true;
                                    // return to cheese finding
                                    // if we received a squeak telling us about a mine
                                    currentState = State.FIND_CHEESE;
                                    return;
                                }
                            }

                        //}
                    }
                }
            }

        }

        if (rc.canRemoveDirt(nextLoc)) {
            rc.removeDirt(nextLoc);
        }

        if (rc.canMoveForward()) {
            rc.moveForward();
            rc.setIndicatorString("Returning to king.");
        } else {
            // Toggle getting unstuck; we proceed in a straight line
            // after getting unstuck until the next time we hit an
            // obstacle, then we go straight back to king again
            // hopefully from a different direction.
            gettingUnstuck = !gettingUnstuck;
            if (!rc.canMoveForward()) {
                d = directions[rand.nextInt(directions.length-1)];
                if (rc.canTurn()) {
                    rc.turn(d);
                }
            }
            rc.moveForward();
            rc.setIndicatorString("Blocked while returning to king, turning " + d.toString());
            return;
        }

        if (rawCheese == 0) {
            currentState = State.FIND_CHEESE;
        }
        
    }


}
