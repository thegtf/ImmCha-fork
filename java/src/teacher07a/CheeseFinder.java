package teacher07a;

import java.util.ArrayList;
import java.util.List;

import battlecode.common.*;

public class CheeseFinder extends BabyRat {

    public static enum State {
        FIND_CHEESE,
        RETURN_TO_KING,
    }

    public static State currentState;
    public static MapLocation mineLoc = null;
    public static int numMines = 0;
    public static List<MapLocation> mineLocs = new ArrayList<>();
    public static BugNav bugNav = null;

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

    private MapLocation senseCheeseAndMine() throws GameActionException {
        MapInfo[] nearbyInfos = rc.senseNearbyMapInfos();

        // If we don't know of a cheese mine yet, or we're close enough
        // just sense for cheese and pick them up
        for (MapInfo info : nearbyInfos) {
            MapLocation loc = info.getMapLocation();
            if (info.getCheeseAmount() > 0) {
                Direction toCheese = rc.getLocation().directionTo(loc);

                if (rc.canTurn(toCheese)) {
                    rc.turn(toCheese);
                    return info.getMapLocation();
                }
            }
            if (info.hasCheeseMine()) {
                mineLoc = info.getMapLocation();
                System.out.println("Found a cheese mine at " + mineLoc.toString());
                rc.setIndicatorString("Found a cheese mine at " + mineLoc.toString());
            }
        }

        return null;
    }

    private void senseAndAttackCat() throws GameActionException {
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();

        // If we don't know of a cheese mine yet, or we're close enough
        // just sense for cheese and pick them up
        for (RobotInfo info : nearbyRobots) {
            if (info.getType().isCatType()) {
                MapLocation robotLoc = info.getLocation();
                if (rc.canAttack(robotLoc)) {
                    rc.attack(robotLoc);
                }
            }
        }
    }


    public void runFindCheese() throws GameActionException {
        // search for cheese
        MapLocation cheeseLoc = null;
        MapLocation here = rc.getLocation();


        // Choose a cheese mine at random if we can, otherwise, sense and explore
        // any cheese mine around us
        if ((mineLoc == null) || (mineLoc.distanceSquaredTo(here) < 25)) {
            // if we have more than two mine locations, pick one at random
            if (mineLocs.size() > 2) {
                mineLoc = mineLocs.get(rand.nextInt(mineLocs.size()));
            } else {
                // set out in a random direction to explore
                setRandomDirection();
                cheeseLoc = senseCheeseAndMine();
            }

        }
                
        if (mineLoc != null) {
            // We know of a cheese mine, get closer because we are too far away
            System.out.println("Going straight to cheese mine at " + mineLoc.toString());
            Direction toMine = rc.getLocation().directionTo(mineLoc);
            if (rc.canTurn(toMine)) {
                rc.turn(toMine);
            }
        }

        // At this point, we are locked in on a mine
        // or randomly exploring

        MapLocation forward = here.add(rc.getDirection());
        if (rc.canRemoveDirt(forward)) {
            rc.removeDirt(forward);
        }

        senseAndAttackCat();

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
            if (rc.getRawCheese() > 20) {
                currentState = State.RETURN_TO_KING;
            }
            rc.setIndicatorString("Full of cheese, returning to king.");
        }

    }

    public void runReturnToKing() throws GameActionException {

        if (bugNav != null) {
            bugNav.move(rc);
            return;
        }

        MapLocation here = rc.getLocation();
        Direction toKing = here.directionTo(kingLoc);
        MapLocation nextLoc = here.add(toKing);
        int rawCheese = rc.getRawCheese();

        // Only turn to the king if we are unstuck
        if (rc.canTurn(toKing)) {
            rc.turn(toKing);
        }

        int dist = kingLoc.distanceSquaredTo(here);
        boolean canSense = rc.canSenseLocation(kingLoc);
        System.out.println("Can sense kingLoc " + kingLoc.toString() + " ? " + canSense + " at distSquared " + dist);
        // We could have spawned on the opposite side of our king, 5 is max distance across a 3x3 king
        if (dist <= 25) {

            RobotInfo[] nearbyRobots = rc.senseNearbyRobots(kingLoc, 8, rc.getTeam());
            boolean squeaked = false;
            boolean receivedMineLoc = false;

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
                    kingLoc = actualKingLoc; // update our kingLoc for next time, since king moves around
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
                        squeaked = true;
                    } else {
                        //while (!receivedMineLoc) {
                            Message[] squeakMessages = rc.readSqueaks(rc.getRoundNum());
                            System.out.println("Received " + squeakMessages.length + " messages");

                            for (Message m : squeakMessages) {
                                int msg = m.getBytes();
                                if (getSqueakType(msg) == SqueakType.CHEESE_MINE) {
                                    int encodedLoc = getSqueakValue(msg);
                                    mineLoc = new MapLocation(getX(encodedLoc), getY(encodedLoc));
                                    mineLocs.add(mineLoc);
                                    System.out.println("Received cheese mine " + mineLoc.toString());
                                    System.out.println("Mine locs size " + mineLocs.size());
                                    receivedMineLoc = true;
                                    // return to cheese finding
                                    // if we received a squeak telling us about a mine

                                    mineLoc = null; // reset so we choose a random new mine next time
                                    return;
                                }
                            }

                        //}
                    }
                }
            }

            // If we haven't squeaked to another baby rat, stay here until we have
            if (squeaked || receivedMineLoc) {
                currentState = State.FIND_CHEESE;
                mineLoc = null; // reset so we choose a random new mine next time
                return;
            }
        }

        MapLocation forwardLoc = rc.adjacentLocation(rc.getDirection());
        RobotInfo forwardRobot = rc.senseRobotAtLocation(forwardLoc);

        if (rc.canRemoveDirt(nextLoc)) {
            rc.removeDirt(nextLoc);
        }

        if (rc.canMoveForward()) {
            rc.moveForward();
            rc.setIndicatorString("Returning to king.");
        } else if (forwardRobot != null) {
            // simply skip this turn and wait for the other robot to move
            return;
        } else {
            MapInfo forwardInfo = rc.senseMapInfo(forwardLoc);
            if (forwardInfo.isWall() && bugNav == null) {
                bugNav = new BugNav(kingLoc, rc);
            }

            rc.setIndicatorString("Blocked while returning to king, turning " + d.toString());
            return;
        }

        if (rawCheese == 0) {
            currentState = State.FIND_CHEESE;
        }
        
    }


}
