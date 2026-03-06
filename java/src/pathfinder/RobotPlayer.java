package pathfinder;

import battlecode.common.*;

public class RobotPlayer {

    static RobotSubPlayer rsp;

    public static void run(RobotController rc) {

        try {
            if (rc.getType().isRatKingType()) {
                rsp = new RatKing(rc);
            } else {
                rsp = new BabyRat(rc);
            }
        } catch (GameActionException e) {
            System.out.println("GameActionException in RobotPlayer:");
            e.printStackTrace();
        }
        while (true) {
            try {
                rsp.doAction();
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
    
}