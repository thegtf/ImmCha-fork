package shredders;

import battlecode.common.*;

public abstract class BabyRat extends RobotSubPlayer {
    protected static MapLocation kingLoc = null;

    public static SqueakType[] squeakTypes = SqueakType.values();

    public static enum SqueakType {
        INVALID,
        ENEMY_RAT_KING,
        ENEMY_COUNT,
        CHEESE_MINE,
        CHEESE_MINE_ACK,
        CAT_FOUND,
    }

    public BabyRat(RobotController rc) {
        super(rc); }
        // Save the location of the king who spawned us
     public void updateKingLocFromShared() throws GameActionException {
    int kx = rc.readSharedArray(1); // SA_KING_X
    int ky = rc.readSharedArray(2); // SA_KING_Y
    if (!(kx == 0 && ky == 0)) {
        kingLoc = new MapLocation(kx, ky);
    }
}


    public static BabyRat createToggle(RobotController rc) throws GameActionException {
    int round = rc.getRoundNum();
    int bucket = Math.floorMod(rc.getID(), 20); // 0..19

    if (round >= 1300) {
        // LONG GAME:
        // 0-1  (10%) KingBuilder
        // 2-7  (30%) Kamikaze
        // 8-10 (15%) CatAttacker
        // 11-19 (45%) CheeseFinder
        if (bucket <= 1) return new KingBuilder(rc);
        if (bucket <= 8) return new Kamikaze(rc);
        if (bucket <= 10) return new Attacker(rc);
        return new CheeseFinder(rc);
    }

    // NORMAL GAME:
    // 0-1  (10%) KingBuilder
    // 2-4  (15%) Attacker
    // 5-8  (20%) Kamikaze
    // 9-19 (55%) CheeseFinder
    if (bucket <= 1) return new KingBuilder(rc);
    if (bucket <= 4) return new Attacker(rc);
    if (bucket <= 8) return new Kamikaze(rc);
    return new CheeseFinder(rc);
}

    public static int getSqueak(SqueakType type, int value) {
        switch (type) {
            case ENEMY_RAT_KING:
                return (1 << 12) | value;
            case ENEMY_COUNT:
                return (2 << 12) | value;
            case CHEESE_MINE:
                return (3 << 12) | value;
            case CAT_FOUND:
                return (4 << 12) | value;
            default:
                return value;
        }
    }
    
    public static SqueakType getSqueakType(int rawSqueak) {
        return squeakTypes[rawSqueak >> 12];
    }

    public static int getSqueakValue(int rawSqueak) {
        // Only uses lower 12 bits
        return rawSqueak % 4096;
    }

    

    public abstract void doAction() throws GameActionException;

    public boolean tryTransferToAnyNearbyKing() throws GameActionException {
    int raw = rc.getRawCheese();
    if (raw <= 0) return false;

    RobotInfo[] allies = rc.senseNearbyRobots(rc.getType().getVisionRadiusSquared(), rc.getTeam());
    for (RobotInfo r : allies) {
        if (r.getType().isRatKingType()) {
            MapLocation k = r.getLocation();
            if (rc.canTransferCheese(k, raw)) {
                rc.transferCheese(k, raw);
                return true;
            }
        }
    }
    return false;
}

}

