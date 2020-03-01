package Robos;

import robocode.Droid;
import robocode.HitByBulletEvent;
import robocode.MessageEvent;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;
import static robocode.util.Utils.normalRelativeAngleDegrees;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

public class Droid2 extends TeamRobot {
	int movementDirection = 1;

	double oldEnergy = 100;

	public void run() {
		out.println("Droid2 ready.");
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		setTurnRadarRight(1000); // initial scan
		execute();
		while (true) {
			if (getRadarTurnRemaining() == 0) {
				setTurnRadarRight(30);
			}
			execute();
		}
	}

	public void onMessageReceived(MessageEvent e) {
		if (e.getMessage() instanceof robotColors) {
			robotColors teamColors = (robotColors) e.getMessage();
			setBodyColor(teamColors.bodyColor);
			setGunColor(teamColors.gunColor);
			setRadarColor(teamColors.radarColor);
			setScanColor(teamColors.scanColor);
			setBulletColor(teamColors.bulletColor);
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		if (isTeammate(e.getName())) {
			return;
		}
		
		//fazer lock on no inimigo
		setTurnRadarRight(getHeading() - getRadarHeading() + e.getBearing());

		//ficvar a 90 graus do inimigo
		setTurnRight(e.getBearing() + 90 - 20 * movementDirection);

		//apontar para o inimigo
		double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
		setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()));

		double energyChange = oldEnergy - e.getEnergy();
		if (energyChange > 0 && energyChange <= 3) {
			// Dodge!
			movementDirection *= -1;
			setAhead((e.getDistance() / 5 + 30) * movementDirection);
		}

		if (e.getDistance() < 200) {
			fire(3);
		} else if (e.getDistance() >= 200 && e.getDistance() < 300) {
			fire(2);
		} else {
			fire(1);
		}

		oldEnergy = e.getEnergy();
	}
	
	public void onHitByBullet(HitByBulletEvent e) {
		movementDirection *= -1;
		setTurnRadarRight(360);
		setAhead(60 * movementDirection);
	}
	
}
