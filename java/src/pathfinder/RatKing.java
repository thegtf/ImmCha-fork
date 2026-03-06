package pathfinder;

import battlecode.common.*;

public class RatKing extends RobotSubPlayer {

    int numRats;
    final static int SPAWN_COOLDOWN_IN_TURNS = 5;
    boolean spawned;
    
    int centerXSignum; // +/- 1 indicating direction toward center from here
    int centerYSignum; // +/- 1 indicating direction toward center from here

    int mapWidth;
    int mapHeight;
    MapLocation here;

    public RatKing(RobotController rc) throws GameActionException {
        super(rc);

        this.mapWidth = rc.getMapWidth();
        this.mapHeight = rc.getMapHeight();
        int midWidth = mapWidth / 2;
        int midHeight = mapHeight / 2;

        here = rc.getLocation();
        this.centerXSignum = Integer.signum(midWidth - here.x);
        this.centerYSignum = Integer.signum(midHeight - here.y);

        numRats = 0;
        spawned = false;

        MapLocation opposite = getOppositeRatKing();
        rc.writeSharedArray(0, opposite.x);
        rc.writeSharedArray(1, opposite.y);
    }

    public MapLocation getOppositeRatKing() {
        return new MapLocation(this.mapWidth - here.x, this.mapHeight - here.y);
    }

    @Override
    public void doAction() throws GameActionException {
        if (spawned) {
            return;
        }

        MapLocation[] potentialSpawnLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8);

        for (MapLocation loc : potentialSpawnLocations) {
            if ((Integer.signum(loc.x - here.x) == this.centerXSignum) &&
                (Integer.signum(loc.y - here.y) == this.centerYSignum)) {
                    if (!spawned && rc.canBuildRat(loc)) {
                        spawned = true;
                        rc.buildRat(loc);
                        numRats += 1;
                        break;
                    }
            }
        }

    }
    
    

}
