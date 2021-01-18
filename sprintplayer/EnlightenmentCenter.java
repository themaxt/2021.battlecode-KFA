package sprintplayer;

import battlecode.common.*;

public class EnlightenmentCenter extends Robot {

	public EnlightenmentCenter(RobotController rc) throws GameActionException {
		super(rc);
	}

	public void takeTurn() throws GameActionException {
		super.takeTurn();
		RobotType toBuild = RobotType.MUCKRAKER;
		int influence = 50;
		for (Direction dir : directions) {
			if (rc.canBuildRobot(toBuild, dir, influence)) {
				rc.buildRobot(toBuild, dir, influence);
			} else {
				break;
			}
		}
	}

}