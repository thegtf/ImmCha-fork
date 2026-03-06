package pathfinder;

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

    // Whether we tried turning right on the last turn in the roomba wall bumping logic 
    boolean turningRight;

    // Eight adjacent locations
    MapInfo[] neighbors;

    boolean followingWall = false;

    // We follow an object counter-clocking
    public BugNav(MapLocation dest, RobotController rc) throws GameActionException {
        followingWall = false;
        mode = Mode.TRACING_FARTHER;
        this.dest = dest;
        turningRight = false;
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
        //MapInfo leftNeighbor = neighbors[left.ordinal()];
        MapInfo rightNeighbor = neighbors[right.ordinal()];
        
        if (rightNeighbor.isPassable()) {
            return right;
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

    /**
     * Do a step of the bugnav algorithm, returning when to stop doing
     * bugnav and resume straight line walking toward destination.
     * @param rc
     * @return true if we should end bugnav and walk straight
     * false if we should keep calling bugnav next time
     * @throws GameActionException
     */
    public boolean move(RobotController rc) throws GameActionException {

        destDirection = rc.getLocation().directionTo(dest);
        if (rc.canMove(destDirection)) { //if can move to goal
            rc.turn(destDirection);
            d = destDirection;
            rc.moveForward();
            followingWall = false;
            return true;
        } else {
            followingWall = true;
        }

        if (followingWall) { //if can't, follow wall

            // Test right wall and turn if possible (roomba wall bumping)
            /*
            Direction right = rightDirection(d);
            if (!turningRight && rc.canTurn()) {
                rc.turn(right);
                MapInfo rightInfo = rc.senseMapInfo(rc.adjacentLocation(right));
                if (rightInfo.isPassable()) {
                    if (rc.canMoveForward()) {
                        rc.moveForward();
                    }
                    // If we've cleared the right, update our direction to face right
                    d = right;
                    System.out.println("Able to bump right in roomba logic");
                    return false;
                } else {
                    // We weren't able to turn right, turn back on next turn because of turn cooldown
                    turningRight = true;
                }
            } else {
                rc.turn(d);
                turningRight = false;
            }
             */

            // Turn away to the left 
            if (rc.canMoveForward()) {
                rc.moveForward();
            } else {
                Direction left = leftDirection(d);

                if (rc.canTurn()) {
                    rc.turn(left);
                    d = left;
                }
            }
        }
        return false;

    }

}
