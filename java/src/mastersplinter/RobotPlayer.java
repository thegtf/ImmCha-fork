package mastersplinter;

import battlecode.common.*;

public class RobotPlayer {

    private static RobotSubPlayer chooseBrain(RobotController rc) {
        if (rc.getType().isRatKingType()) return new RatKingSubPlayer(rc);

        int mod = Math.floorMod(rc.getID(), 10);
        if (mod == 0) return new KingBuilderSubPlayer(rc);
        if (mod == 1) return new ExpanderSubPlayer(rc);
        if (mod == 2 || mod == 3) return new FighterSubPlayer(rc);
        return new ExplorerSubPlayer(rc);
    }

    public static void run(RobotController rc) {
        RobotSubPlayer brain = null;

        while (true) {
            try {
                // if type changes (promotion), swap brain
                if (rc.getType().isRatKingType()) {
                    if (!(brain instanceof RatKingSubPlayer)) brain = new RatKingSubPlayer(rc);
                } else {
                    if (brain == null || brain instanceof RatKingSubPlayer) brain = chooseBrain(rc);
                }

                brain.step();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }

    public static MapLocation chooseNearestKnownMine(RobotController rc, MapLocation location) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'chooseNearestKnownMine'");
    }
}