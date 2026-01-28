package teacher02b;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;

public class RobotPlayer {

    public static enum State {
        FIND_CHEESE,
        RETURN_TO_KING,        
    }

    public static enum SqueakType {
        INVALID,
        ENEMY_RAT_KING,
        ENEMY_COUNT,
        CHEESE_MINE,
        CAT_FOUND,
    }

    public static State currentState;

    public static SqueakType[] squeakTypes = SqueakType.values();
    public static Direction[] directions = Direction.values();
    public static MapLocation mineLoc = null;
    public static int numMines = 0;
    public static ArrayList<Integer> mineLocs = new ArrayList<>();
    static final Random rand = new Random(6147);
    static Direction d = null;

    static MapLocation kingLoc = null;

    public static void run(RobotController rc) {

        // Only baby rats will pay attention to this
        currentState = State.FIND_CHEESE;

        while (true) {
        try {
            if (rc.getType().isRatKingType()) {
                runRatKing(rc);
            } else {
                if (kingLoc == null) {
                    kingLoc = rc.getLocation();
                }
                switch (currentState) {
                    case FIND_CHEESE:
                        runFindCheese(rc);
                        break;
                    case RETURN_TO_KING:
                        runReturnToKing(rc);
                        break;
                }
                }
            } catch (GameActionException e) {
                System.out.println("GameActionException in RobotPlayer:");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception in RobotPlayer:");
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }

    public static void runRatKing(RobotController rc) throws GameActionException {
        int currentCost = rc.getCurrentRatCost();

        MapLocation[] potentialSpawnLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8);
        boolean spawn = currentCost <= 10 || rc.getAllCheese() > currentCost + 2500;

        for (MapLocation loc : potentialSpawnLocations) {
            if (spawn && rc.canBuildRat(loc)) {
                rc.buildRat(loc);
                break;
            }

            if (rc.canPickUpCheese(loc)) {
                rc.pickUpCheese(loc);
                break;
            }
        }

        Message[] squeaks = rc.readSqueaks(rc.getRoundNum());

        for (Message msg : squeaks) {
            int rawSqueak = msg.getBytes();

            if (getSqueakType(rawSqueak) != SqueakType.CHEESE_MINE) {
                continue;
            }

            int encodedLoc = getSqueakValue(rawSqueak);

            if (mineLocs.contains(encodedLoc)) {
                continue;
            }

            mineLocs.add(encodedLoc);
            int firstInt = getFirstInt(encodedLoc);
            int lastInt = getLastInt(encodedLoc);

            rc.writeSharedArray(2 * numMines + 2, firstInt);
            rc.writeSharedArray(2 * numMines + 3, lastInt);
            System.out.println("Writing to shared array: " + firstInt + ", " + lastInt);
            System.out.println("Cheese mine located at: " + getX(encodedLoc) + ", " + getY(encodedLoc));

            numMines++;
        }

        moveRandom(rc);

        // TODO make more efficient and expand communication in the communication lecture
        rc.writeSharedArray(0, rc.getLocation().x);
        rc.writeSharedArray(1, rc.getLocation().y);

    }

    // Move in a straight line until we bump into something
    // then turn to a new direction
    public static void moveRandom(RobotController rc) throws GameActionException {

        if (d == null) {
            d = directions[rand.nextInt(directions.length-1)];
        }

        if (rc.canMoveForward()) {
            rc.moveForward();
        } else {
            d = directions[rand.nextInt(directions.length-1)];
            if (rc.canTurn()) {
                rc.turn(d);
            }
        }

    }

    public static void runFindCheese(RobotController rc) throws GameActionException {
        // search for cheese
        MapInfo[] nearbyInfos = rc.senseNearbyMapInfos();

        System.out.println("Sensed " + nearbyInfos.length + " tiles");
        MapLocation cheeseLoc = null;
        for (MapInfo info : nearbyInfos) {
            if (info.getCheeseAmount() > 0) {
                Direction toCheese = rc.getLocation().directionTo(info.getMapLocation());

                if (rc.canTurn(toCheese)) {
                    rc.turn(toCheese);
                    cheeseLoc = info.getMapLocation();
                    break;
                }
            }
        }

        if (rc.canMoveForward()) {
            rc.moveForward();
        } else {
            d = directions[rand.nextInt(directions.length-1)];
            if (rc.canTurn()) {
                rc.turn(d);
            }
        }

        if (rc.canPickUpCheese(cheeseLoc)) {
            rc.pickUpCheese(cheeseLoc);
            currentState = State.RETURN_TO_KING;
        }
        
    }

    public static void runReturnToKing(RobotController rc) throws GameActionException {
        Direction toKing = rc.getLocation().directionTo(kingLoc);
        MapLocation nextLoc = rc.getLocation().add(toKing);

        if (rc.canTurn(toKing)) {
            rc.turn(toKing);
        }

        if (rc.canRemoveDirt(nextLoc)) {
            rc.removeDirt(nextLoc);
        }

        // TODO replace with pathfinding for the pathfinding lecture
        if (rc.canMove(toKing)) {
            rc.move(toKing);
        }

        int rawCheese = rc.getRawCheese();

        if (rawCheese == 0) {
            currentState = State.FIND_CHEESE;

        if (kingLoc.distanceSquaredTo(rc.getLocation()) <= 4) {

        if (rc.canTransferCheese(kingLoc, rawCheese)) {
            rc.transferCheese(kingLoc, rawCheese);
            currentState = State.FIND_CHEESE;
            }
        }
    }
}

    public static int getFirstInt(int loc) {
        // extract 10 smallest place value bits from toInteger(loc)
        return loc % 1024;
    }

    public static int getLastInt(int loc) {
        // extract bits with place values >= 2^10 from toInteger(loc)
        return loc >> 10;
    }

    public static int getX(int encodedLoc) {
        return encodedLoc >> 6;
    }

    public static int getY(int encodedLoc) {
        return encodedLoc % 64;
    }
    
    public static SqueakType getSqueakType(int rawSqueak) {
        return squeakTypes[rawSqueak >> 12];
    }

    public static int getSqueakValue(int rawSqueak) {
        return rawSqueak % 4096;
    }
    
}
