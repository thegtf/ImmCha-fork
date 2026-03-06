package pathfinder;

import static org.junit.Assert.*;
import org.junit.Test;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;
import battlecode.common.UnitType;

public class RatKingTest {


	@Test
	public void testOpposite() {
        RobotController rc = new MockRobotController();
		try {
            RatKing rk = new RatKing(rc);
            assertEquals(rk.centerXSignum, 1);
            assertEquals(rk.centerYSignum, 1);

            MapLocation opposite = rk.getOppositeRatKing();
            System.out.println("Opposite Rat King location: " + opposite.toString());
            assertEquals(90, opposite.x);
            assertEquals(91, opposite.y);
        } catch (GameActionException e) {
            e.printStackTrace();
            assertFalse(false);
        }
        
	}

}
