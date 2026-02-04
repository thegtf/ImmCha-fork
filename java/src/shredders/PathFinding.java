package shredders;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class PathFinding {


    public static enum PathState {
        FOLLOW_WALL,
        MOVE_TO_TARGET,
    }

    public static PathState ps = PathState.MOVE_TO_TARGET;

    static int bestDist;
    static MapLocation target = null;

    public static void moveToTarget(RobotController rc, MapLocation target, MapLocation curr) throws GameActionException {
        MapLocation current = rc.getLocation();
        Direction d = rc.getLocation().directionTo(target);

        if (ps == PathState.MOVE_TO_TARGET) {
            if (rc.canMove(d)) {
                rc.move(d);
            } else {
                ps = PathState.FOLLOW_WALL;

            } 
        }
        
    }


    public static void followWall(RobotController rc, MapLocation target, MapLocation curr) throws GameActionException {
            Direction dir = rc.getLocation().directionTo(target);
            MapLocation current = rc.getLocation();

            // Rotate left until we find a valid move
            for (int i = 0; i < 8; i++) {
                     dir = dir.rotateLeft();
                    if (rc.canMove(dir)) {
                        try {
                            rc.move(dir);
                        } catch (GameActionException e) {
                            System.out.println("GameActionException in followWall:");
                            e.printStackTrace();
                        }
                        break;
                }

            }

        }
    }

