package teacher07a;

import battlecode.common.*;

/* A module for pathfinding with bugnav algorithm */
public class BugNav {

    enum Mode {
        TRACING_FARTHER,
        TRACING_CLOSER,
        STRAIGHT
    }

    Mode mode;
    MapLocation dest;

    // Our current direction
    Direction d;

    // Our original direction to our destination
    // if our leftward direction ever points here, we should go straight
    Direction destDirection;
    Direction scheduledTurn = null;

    int previousDistance;

    // Eight adjacent locations
    MapInfo[] neighbors;

    // We follow an object counter-clocking
    public BugNav(MapLocation dest, RobotController rc) throws GameActionException {
        mode = Mode.TRACING_FARTHER;
        this.dest = dest;
        // destDirection = 
        MapLocation here = rc.getLocation();
        d = here.directionTo(dest);
        System.out.println("Started bugnav " + here.toString() + " heading toward " + dest.toString());
        neighbors = new MapInfo[RobotSubPlayer.directions.length];
        scheduledTurn = leftDirection(rc.getDirection());
        previousDistance = here.distanceSquaredTo(dest);
    }

    public static Direction leftDirection(Direction d) {
        int leftIndex = d.ordinal() - 1;
        if (leftIndex < 0) {
            leftIndex = leftIndex + RobotSubPlayer.directions.length;
        }
        return RobotSubPlayer.directions[leftIndex];
    }

    public static Direction rightDirection(Direction d) {
        int rightIndex = d.ordinal() + 1;
        if (rightIndex >= RobotSubPlayer.directions.length) {
            rightIndex = rightIndex - RobotSubPlayer.directions.length;
        }
        return RobotSubPlayer.directions[rightIndex];
    }

    /*
     * Populate neighbors in all 8 directions from nearby map info's
     * And turn our direction to one that traces the current obstacle in clockwise direction
     * (one where our right location may be free, and we should turn right, or one where our
     * forward may be blocked, and we should turn left until the way forward is free again.
     * @return new direction to turn while keeping wall to our right
     */
    public Direction senseNeighborsForNewTurnDirection(MapInfo[] infos, MapLocation here, Direction forward) {

        // Fill in the neighboring tiles that we can sense
        for (MapInfo info : infos) {
            MapLocation infoLoc = info.getMapLocation();
            if (!infoLoc.isAdjacentTo(here)) {
                continue;
            }
            Direction d = here.directionTo(infoLoc);
            neighbors[d.ordinal()] = info; 
            System.out.println("Filling out neighbor " + d.toString() + " with " + info.getMapLocation().toString());
        }

        Direction left = leftDirection(forward);
        Direction right = rightDirection(forward);
        System.out.println("Left direction " + left.toString());
        System.out.println("Right direction " + right.toString());
        MapInfo leftNeighbor = neighbors[left.ordinal()];
        MapInfo rightNeighbor = neighbors[right.ordinal()];
        if (leftNeighbor.isPassable()) {
            return left;
        } else {
            Direction d = forward;
            MapInfo neighbor = null;
            do {
                d = leftDirection(d);
                neighbor = neighbors[d.ordinal()];
            } while (!neighbor.isPassable());
            // If we reach here we finally found a passable neighbor (which could be 180 degrees behind us)
            return d;
        }
    }

    public boolean move(RobotController rc) throws GameActionException {
        if (!rc.canTurn()) {
            return false;
        }
        if (scheduledTurn != null) {
            System.out.println("Doing scheduled turn to  " + scheduledTurn.toString());
            rc.turn(scheduledTurn);
        }

        MapLocation here = rc.getLocation();
        Direction forward = rc.getDirection();
        MapLocation forwardLoc = here.add(forward);
        int forwardDist = forwardLoc.distanceSquaredTo(dest);
        System.out.println("Forward distance squared " + forwardDist);

        switch (mode) {
            case Mode.STRAIGHT:
                while (rc.canMoveForward()) {
                    rc.moveForward();
                }
                mode = Mode.TRACING_FARTHER;
                return false;
            case Mode.TRACING_FARTHER:
                MapInfo[] infos = rc.senseNearbyMapInfos(2);

                // // Is tracing now moving us further away from our destination?
                if (forwardDist < previousDistance) {
                    mode = Mode.TRACING_CLOSER;
                    return false;
                }
                Direction newDirection = senseNeighborsForNewTurnDirection(infos, here, forward);
                rc.setIndicatorString(("Bugnav, tracing with direction " + newDirection));

                if (forward != newDirection) {
                    if (rc.canTurn()) {
                        d = newDirection;
                        rc.turn(newDirection);
                    }
                }

                if (rc.canMoveForward()) {
                    rc.moveForward();
                }
                break;
            case TRACING_CLOSER:
                int straightDist = here.distanceSquaredTo(dest);
                System.out.println("Straight distance squared " + straightDist);
                if (straightDist < forwardDist) {
                    mode = Mode.STRAIGHT;
                    d = here.directionTo(dest);
                    return true;
                }
                break;
        }
        return false;

    }

}
