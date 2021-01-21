package ECtest;

import battlecode.common.*;

public class EnlightenmentCenter extends Robot {

	public EnlightenmentCenter(RobotController rc) {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		RobotType toBuild = RobotType.SLANDERER;
		int influence = 20;
		for (Direction dir : directions) {
			if (rc.canBuildRobot(toBuild, dir, influence)) {
				rc.buildRobot(toBuild, dir, influence);
			} else {
				break;
			}
		}
	}

}