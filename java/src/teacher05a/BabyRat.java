package teacher05a;

import battlecode.common.*;

public class BabyRat extends RobotSubPlayer {
    
    public BabyRat(RobotController rc) {
        super(rc);
                // Only baby rats will pay attention to this
        currentState = State.FIND_CHEESE;

        //TODO Auto-generated constructor stub
    }

    public static void goForthAndCheese(RobotSubPlayer rc, MapLocation cheeseLoc) throws GameActionException {
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

    public static void runReturnToKing(RobotSubPlayer rc) throws GameActionException {
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

    @Override
    public void doAction(RobotController rc) throws GameActionException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doAction'");
    }

}
