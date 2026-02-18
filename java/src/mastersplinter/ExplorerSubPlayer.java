package mastersplinter;

import battlecode.common.*;

public class ExplorerSubPlayer extends RobotSubPlayer {

    private MapLocation lastMineSeen = null;

    public ExplorerSubPlayer(RobotController rc) {
        super(rc);
    }

    @Override
    public void step() throws GameActionException {
        // sense mines + nearby cheese
        MapInfo[] infos = rc.senseNearbyMapInfos();
        MapLocation cheeseLoc = null;

        for (MapInfo info : infos) {
            if (info.hasCheeseMine()) lastMineSeen = info.getMapLocation();
            if (cheeseLoc == null && info.getCheeseAmount() > 0) cheeseLoc = info.getMapLocation();
        }

        // pick up adjacent cheese
        for (Direction dir : directions) {
            MapLocation loc = rc.getLocation().add(dir);
            if (rc.canPickUpCheese(loc)) { rc.pickUpCheese(loc); break; }
        }

        // if carrying, deliver
        if (rc.getRawCheese() > 0) {
            if (deliverToNearestVisibleKing()) {
                squeakMineIfAny();
                rc.setIndicatorString("EXP: delivered");
                return;
            }
            MapLocation beacon = kingBeacon();
            if (beacon != null) Pathfinder.stepToward(rc, beacon);
            squeakMineIfAny();
            rc.setIndicatorString("EXP: to beacon w/cheese");
            return;
        }

        // if see cheese, go to it
        if (cheeseLoc != null) {
            Pathfinder.stepToward(rc, cheeseLoc);
            squeakMineIfAny();
            rc.setIndicatorString("EXP: to cheese");
            return;
        }

        // wander
        wanderWithDig();
        squeakMineIfAny();
        rc.setIndicatorString("EXP: wander");
    }

    private void squeakMineIfAny() throws GameActionException {
        if (lastMineSeen == null) return;
        Comms.squeakMine(rc, lastMineSeen);
        lastMineSeen = null;
    }
}