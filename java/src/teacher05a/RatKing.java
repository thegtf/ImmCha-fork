package teacher05a;

import battlecode.common.*;

public class RatKing extends RobotSubPlayer {

    public RatKing(RobotController rc) {
        super(rc);
        //TODO Auto-generated constructor stub
    }

    @Override
    public void doAction(RobotController rc) throws GameActionException {
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
        System.out.println("Received " + squeaks.length + " squeaks");

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

        // moveRandom(rc);

        // TODO make more efficient and expand communication in the communication lecture
        rc.writeSharedArray(0, rc.getLocation().x);
        rc.writeSharedArray(1, rc.getLocation().y);
    }
    
    

}
