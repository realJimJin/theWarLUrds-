package examplefuncsplayer;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

//export JAVA_HOME=`/usr/libexec/java_home -v 1.8`

//cd OneDrive/desktop/personal/battlecode/battlecode24-scaffold-main
//gradlew run -PteamA=examplefuncsplayer -PteamB=examplefuncsplayer2 -Pmaps=DefaultMedium
/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;
    static int order = 0;
    static MapLocation[] flagGuesses = null;
    static Direction lastdir = Direction.NORTH;
    static boolean straight = false;
    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    //static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        System.out.println("I'm alive");

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");
        
        while (rc.readSharedArray(order)!=0) order+=1;
		rc.writeSharedArray(order, 1);
		Random rng = new Random(order+1);
		
		Team team = rc.getTeam();
		Team opp = team.opponent();
		RobotInfo[] enemies;
		RobotInfo[] allies;
		FlagInfo[] opflags;
		MapLocation[] spawnLocs = rc.getAllySpawnLocations();
		MapLocation firstLoc = spawnLocs[0];
		int[] distLocs = {0,0,0};
		Direction dir;
		int round;
		int num;
		MapLocation[] flagBroadcast;
		MapLocation dest;
		MapLocation loc;
		boolean cw = (order%2==0);

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!
            
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
            	if (order==0) {
            		rc.setIndicatorString("I am the commander");
            		round = rc.getRoundNum();
            		//if(round == 500) rc.resign();
            		if(round>=GameConstants.SETUP_ROUNDS) {
            			if(round%GameConstants.FLAG_BROADCAST_UPDATE_INTERVAL==0) {
            				flagBroadcast = rc.senseBroadcastFlagLocations();
            				if (flagGuesses==null) flagGuesses=flagBroadcast;
            				else {
            					
            				}
            			}
            		}
            		if(rc.canBuyGlobal(GlobalUpgrade.ACTION)) {
            			rc.buyGlobal(GlobalUpgrade.ACTION);
            		}
            		if(rc.canBuyGlobal(GlobalUpgrade.HEALING)) {
            			rc.buyGlobal(GlobalUpgrade.HEALING);
            		}
            	}
	            else {
	                // Make sure you spawn your robot in before you attempt to take any actions!
	                // Robots not spawned in do not have vision of any tiles and cannot perform any actions.
	                if (!rc.isSpawned()){
	                    /*
	                    // Pick a random spawn location to attempt spawning in.
	                    MapLocation randomLoc = spawnLocs[rng.nextInt(spawnLocs.length)];
	                    if (rc.canSpawn(randomLoc)) rc.spawn(randomLoc);
	                    */
	                    for(int i=0; i<spawnLocs.length; i++) {
	                    	if(rc.canSpawn(spawnLocs[i])) rc.spawn(spawnLocs[i]);
	                    }
	                    // 600 Bytecodes ish
	                }
	                if (rc.isSpawned()){
	                    
	                    // If we are holding an enemy flag, singularly focus on moving towards
	                    // an ally spawn zone to capture it! We use the check roundNum >= SETUP_ROUNDS
	                    // to make sure setup phase has ended.
	                	loc = rc.getLocation();
	                    if (rc.getRoundNum() > GameConstants.SETUP_ROUNDS) {
	                    	if (rc.canPickupFlag(loc)){
		                        rc.pickupFlag(loc);
		                    }
		                    if (rc.hasFlag()){
		                        distLocs[0] = loc.distanceSquaredTo(spawnLocs[0]);
		                        distLocs[1] = loc.distanceSquaredTo(spawnLocs[9]);
		                        distLocs[2] = loc.distanceSquaredTo(spawnLocs[18]);
		                        if(distLocs[0]<distLocs[1]) {
		                        	if(distLocs[0]<distLocs[2]) {
		                        		dir = loc.directionTo(spawnLocs[0]);
		                        	} else {
		                        		dir = loc.directionTo(spawnLocs[18]);
		                        	}
		                        } else {
		                        	if(distLocs[1]<distLocs[2]) {
		                        		dir = loc.directionTo(spawnLocs[9]);
		                        	} else {
		                        		dir = loc.directionTo(spawnLocs[18]);
		                        	}
		                        }
		                    }
		                    else {
		                    	opflags = rc.senseNearbyFlags(-1, opp);
		                    	if (opflags.length>0) {
		                    		dest = opflags[0].getLocation();
		                    		dir = loc.directionTo(dest);
		                    		if(rc.senseRobotAtLocation(dest) != null) dir = dir.opposite(); 
		                    	}
		                    	else {
		                    		flagGuesses = rc.senseBroadcastFlagLocations();
		                    		if (flagGuesses.length>0) dir = loc.directionTo(flagGuesses[0]);
		                    		else dir = directions[rng.nextInt(directions.length)];
		                    	}
		                    	
		                    }
		                    /*double dirdif = Math.IEEEremainder(dir.getDirectionOrderNum()-lastdir.getDirectionOrderNum(),8);
		                    if(dirdif>0)
		                    */
		                    if(straight && dir!=Direction.CENTER) {
		                    	lastdir = dir;
		                    	if(rc.canMove(dir.rotateLeft().rotateLeft())&&rc.canMove(dir.rotateRight().rotateRight())) {
		                    		cw = rng.nextBoolean();
		                    	}
		                    }
		                    straight = false;
		                    if(cw) {
		                    	if(rc.canMove(lastdir)) {
			                    	while (true){
			                    		if(rc.canMove(lastdir)) {
				                    		if(lastdir == dir) {
				                    			straight = true;
				                    			break;
				                    		}
				                    		lastdir = lastdir.rotateRight();
			                    		}
			                    		else {
				                    		lastdir = lastdir.rotateLeft();
				                    		break;
			                    		}
			                    	}
			                    } else {
			                    	num=0;
			                    	while(!rc.canMove(lastdir) && num<8) {
			                    		lastdir = lastdir.rotateLeft();
			                    		num++;
			                    	}
			                    }
		                    } else {
		                    	if(rc.canMove(lastdir)) {
			                    	while (true){
			                    		if(rc.canMove(lastdir)) {
				                    		if(lastdir == dir) {
				                    			straight = true;
				                    			break;
				                    		}
				                    		lastdir = lastdir.rotateLeft();
			                    		}
			                    		else {
				                    		lastdir = lastdir.rotateRight();
				                    		break;
			                    		}
			                    	}
			                    } else {
			                    	num=0;
			                    	while(!rc.canMove(lastdir) && num<8) {
			                    		lastdir = lastdir.rotateRight();
			                    		num++;
			                    	}
			                    }
		                    }
		                    if(rc.canMove(lastdir)) rc.move(lastdir);
		                    
		                    //if(rc.canFill(loc.add(dir))) rc.fill(rc.getLocation().add(dir));
			                
	                    }
	                    else {
	                    	if (rc.getRoundNum()==1) flagGuesses = rc.senseBroadcastFlagLocations();
	                    	if(flagGuesses!=null) {
	                    		if (flagGuesses.length>0) dir = loc.directionTo(flagGuesses[Math.abs(rng.nextInt())%flagGuesses.length]);
	                    		else dir = directions[rng.nextInt(directions.length)];
	                    	}
                    		else dir = directions[rng.nextInt(directions.length)];
		                    if (rc.canMove(dir)){
		                        rc.move(dir);
		                    } else if(rc.canMove(dir.rotateLeft())) {
		                    	rc.move(dir.rotateLeft());
		                    } else if(rc.canMove(dir.rotateRight())) {
		                    	rc.move(dir.rotateRight());
		                    }
	                    }
	                    /*
	                    else if (rc.canAttack(nextLoc)){
	                        rc.attack(nextLoc);
	                        //System.out.println("Take that! Damaged an enemy that was in our way!");
	                    }
	                    */
	                    if(rc.isActionReady()) {
		                    enemies = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, opp);
		                    if (enemies.length>0) {
		                    	if (rc.canAttack(enemies[0].getLocation())) rc.attack(enemies[0].getLocation());
		                    }
	                    }
	                    if(rc.isActionReady()) {
	                    	allies = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, team);
	                    	for(int i=0; i<allies.length; i++) {
	                    		if(rc.canHeal(allies[i].getLocation())) {
	                    			rc.heal(allies[i].getLocation());
	                    		}
	                    	}
	                    }
	                    
	                    // Rarely attempt placing traps behind the robot. Place explosive near spawn zones, and stun traps near large groups of enemies
	                    MapLocation prevLoc = loc.subtract(dir);
//	                    if (rc.canBuild(TrapType.STUN, prevLoc) && rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length >5)
//	                        rc.build(TrapType.STUN, prevLoc);
	                    if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && rng.nextInt() % 37 == 1)
	                        rc.build(TrapType.EXPLOSIVE, prevLoc);
	                    
	                    // We can also move our code into different methods or classes to better organize it!
	                    //updateEnemyRobots(rc);
	                    rc.setIndicatorString("order: " + String.valueOf(order) + " dir: " + String.valueOf(dir) + " lastdir: " + String.valueOf(lastdir));
	                }
            	}

            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }
    public static void updateEnemyRobots(RobotController rc) throws GameActionException{
        // Sensing methods can be passed in a radius of -1 to automatically 
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0){
            // Save an array of locations with enemy robots in them for future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++){
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            // Let the rest of our team know how many enemy robots we see!
            if (rc.canWriteSharedArray(order, enemyRobots.length)){
                rc.writeSharedArray(order, enemyRobots.length);
                int numEnemies = rc.readSharedArray(0);
            }
        }
    }
}


//package examplefuncsplayer;
//
//import battlecode.common.*;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Random;
//import java.util.Set;
//
///**
// * RobotPlayer is the class that describes your main robot strategy.
// * The run() method inside this class is like your main function: this is what we'll call once your robot
// * is created!
// */
//public strictfp class RobotPlayer {
//
//    /**
//     * We will use this variable to count the number of turns this robot has been alive.
//     * You can use static variables like this to save any information you want. Keep in mind that even though
//     * these variables are static, in Battlecode they aren't actually shared between your robots.
//     */
//    static int turnCount = 0;
//
//    /**
//     * A random number generator.
//     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
//     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
//     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
//     */
//    static final Random rng = new Random(6147);
//
//    /** Array containing all the possible movement directions. */
//    static final Direction[] directions = {
//        Direction.NORTH,
//        Direction.NORTHEAST,
//        Direction.EAST,
//        Direction.SOUTHEAST,
//        Direction.SOUTH,
//        Direction.SOUTHWEST,
//        Direction.WEST,
//        Direction.NORTHWEST,
//    };
//
//    /**
//     * run() is the method that is called when a robot is instantiated in the Battlecode world.
//     * It is like the main function for your robot. If this method returns, the robot dies!
//     *
//     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
//     *            information on its current status. Essentially your portal to interacting with the world.
//     **/
//    @SuppressWarnings("unused")
//    public static void run(RobotController rc) throws GameActionException {
//
//        // Hello world! Standard output is very useful for debugging.
//        // Everything you say here will be directly viewable in your terminal when you run a match!
//        System.out.println("I'm alive");
//
//        // You can also use indicators to save debug notes in replays.
//        rc.setIndicatorString("Hello world!");
//
//        while (true) {
//            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
//            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
//            // loop, we call Clock.yield(), signifying that we've done everything we want to do.
//
//            turnCount += 1;  // We have now been alive for one more turn!
//
//            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
//            try {
//                // Make sure you spawn your robot in before you attempt to take any actions!
//                // Robots not spawned in do not have vision of any tiles and cannot perform any actions.
//                if (!rc.isSpawned()){
//                    MapLocation[] spawnLocs = rc.getAllySpawnLocations();
//                    // Pick a random spawn location to attempt spawning in.
//                    MapLocation randomLoc = spawnLocs[rng.nextInt(spawnLocs.length)];
//                    if (rc.canSpawn(randomLoc)) rc.spawn(randomLoc);
//                }
//                else{
//                	if(rc.canBuyGlobal(GlobalUpgrade.ACTION)) {
//                		rc.buyGlobal(GlobalUpgrade.ACTION);
//                	}else if(rc.canBuyGlobal(GlobalUpgrade.CAPTURING)){
//                		rc.buyGlobal(GlobalUpgrade.CAPTURING);
//                	}
//                    if (rc.canPickupFlag(rc.getLocation())){
//                        rc.pickupFlag(rc.getLocation());
//                        rc.setIndicatorString("Holding a flag!");
//                    }
//                    // If we are holding an enemy flag, singularly focus on moving towards
//                    // an ally spawn zone to capture it! We use the check roundNum >= SETUP_ROUNDS
//                    // to make sure setup phase has ended.
//                    if (rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS){
//                        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
//                        MapLocation firstLoc = spawnLocs[0];
//                        Direction dir = rc.getLocation().directionTo(firstLoc);
//                        if (rc.canMove(dir)) rc.move(dir);
//                    }
//                    // Move and attack randomly if no objective.
//                    Direction dir = directions[rng.nextInt(directions.length)];
//                    if(rc.getRoundNum() > GameConstants.SETUP_ROUNDS) {
//                    	if(rc.senseBroadcastFlagLocations().length >0)dir = rc.getLocation().directionTo(rc.senseBroadcastFlagLocations()[0]);
//                    	} 
//                    FlagInfo[] flager = rc.senseNearbyFlags(GameConstants.VISION_RADIUS_SQUARED, rc.getTeam().opponent());
//                    if(flager.length>0) {
//                    	FlagInfo f = flager[0];
//                    	dir = rc.getLocation().directionTo(f.getLocation());
//                   	
//                    }
//                    MapLocation nextLoc = rc.getLocation().add(dir);
//                    if(rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length>0) {
//                    	nextLoc=rc.senseNearbyRobots(-1, rc.getTeam().opponent())[0].location;
//                    }
//                    if (rc.canMove(dir)){
//                        rc.move(dir);
//                    }
//                    else if (rc.canAttack(nextLoc)){
//                        rc.attack(nextLoc);
//                        System.out.println("Take that! Damaged an enemy that was in our way!");
//                    }else {
//                    	for(int i=0; i<directions.length;i++) {
//                    		if(rc.canMove(directions[i]))rc.move(directions[i]);
//                    	}
//                    }
//
//                    // Rarely attempt placing traps behind the robot.
////                    MapLocation prevLoc = rc.getLocation().subtract(dir);
////                    if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && rc.readSharedArray(0) > 10)
////                        rc.build(TrapType.EXPLOSIVE, prevLoc);
//                    // We can also move our code into different methods or classes to better organize it!
//                    updateEnemyRobots(rc);
//                }
//
//            } catch (GameActionException e) {
//                // Oh no! It looks like we did something illegal in the Battlecode world. You should
//                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
//                // world. Remember, uncaught exceptions cause your robot to explode!
//                System.out.println("GameActionException");
//                e.printStackTrace();
//
//            } catch (Exception e) {
//                // Oh no! It looks like our code tried to do something bad. This isn't a
//                // GameActionException, so it's more likely to be a bug in our code.
//                System.out.println("Exception");
//                e.printStackTrace();
//
//            } finally {
//                // Signify we've done everything we want to do, thereby ending our turn.
//                // This will make our code wait until the next turn, and then perform this loop again.
//                Clock.yield();
//            }
//            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
//        }
//
//        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
//    }
//    public static void updateEnemyRobots(RobotController rc) throws GameActionException{
//        // Sensing methods can be passed in a radius of -1 to automatically 
//        // use the largest possible value.
//        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
//        if (enemyRobots.length != 0){
//            rc.setIndicatorString("There are nearby enemy robots! Scary!");
//            // Save an array of locations with enemy robots in them for future use.
//            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
//            for (int i = 0; i < enemyRobots.length; i++){
//                enemyLocations[i] = enemyRobots[i].getLocation();
//            }
//            // Let the rest of our team know how many enemy robots we see!
//            if (rc.canWriteSharedArray(0, enemyRobots.length)){
//                rc.writeSharedArray(0, enemyRobots.length);
//                int numEnemies = rc.readSharedArray(0);
//            }
//        }
//    }
//}
//
//
//
////package examplefuncsplayer;
////
////import battlecode.common.*;
////
////import java.util.Arrays;
////import java.util.HashMap;
////import java.util.HashSet;
////import java.util.Map;
////import java.util.Random;
////import java.util.Set;
////
/////**
//// * RobotPlayer is the class that describes your main robot strategy.
//// * The run() method inside this class is like your main function: this is what we'll call once your robot
//// * is created!
//// */
////public strictfp class RobotPlayer {
////
////    /**
////     * We will use this variable to count the number of turns this robot has been alive.
////     * You can use static variables like this to save any information you want. Keep in mind that even though
////     * these variables are static, in Battlecode they aren't actually shared between your robots.
////     */
////    static int turnCount = 0;
////
////    /**
////     * A random number generator.
////     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
////     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
////     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
////     */
////    static final Random rng = new Random(6147);
////
////    /** Array containing all the possible movement directions. */
////    static final Direction[] directions = {
////        Direction.NORTH,
////        Direction.NORTHEAST,
////        Direction.EAST,
////        Direction.SOUTHEAST,
////        Direction.SOUTH,
////        Direction.SOUTHWEST,
////        Direction.WEST,
////        Direction.NORTHWEST,
////    };
////
////    /**
////     * run() is the method that is called when a robot is instantiated in the Battlecode world.
////     * It is like the main function for your robot. If this method returns, the robot dies!
////     *
////     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
////     *            information on its current status. Essentially your portal to interacting with the world.
////     **/
////    @SuppressWarnings("unused")
////    public static void run(RobotController rc) throws GameActionException {
////
////        // Hello world! Standard output is very useful for debugging.
////        // Everything you say here will be directly viewable in your terminal when you run a match!
////        System.out.println("I'm alive");
////
////        // You can also use indicators to save debug notes in replays.
////        rc.setIndicatorString("Hello world!");
////
////        while (true) {
////            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
////            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
////            // loop, we call Clock.yield(), signifying that we've done everything we want to do.
////
////            turnCount += 1;  // We have now been alive for one more turn!
////
////            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
////            try {
////                // Make sure you spawn your robot in before you attempt to take any actions!
////                // Robots not spawned in do not have vision of any tiles and cannot perform any actions.
////                if (!rc.isSpawned()){
////                    MapLocation[] spawnLocs = rc.getAllySpawnLocations();
////                    // Pick a random spawn location to attempt spawning in.
////                    MapLocation randomLoc = spawnLocs[rng.nextInt(spawnLocs.length)];
////                    if (rc.canSpawn(randomLoc)) rc.spawn(randomLoc);
////                }
////                else{
////                    if (rc.canPickupFlag(rc.getLocation())){
////                        rc.pickupFlag(rc.getLocation());
////                        rc.setIndicatorString("Holding a flag!");
////                    }
////                    // If we are holding an enemy flag, singularly focus on moving towards
////                    // an ally spawn zone to capture it! We use the check roundNum >= SETUP_ROUNDS
////                    // to make sure setup phase has ended.
////                    if (rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS){
////                        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
////                        MapLocation firstLoc = spawnLocs[0];
////                        Direction dir = rc.getLocation().directionTo(firstLoc);
////                        if (rc.canMove(dir)) rc.move(dir);
////                    }
////                    // Move and attack randomly if no objective.
////                    Direction dir = directions[rng.nextInt(directions.length)];
////                    MapLocation nextLoc = rc.getLocation().add(dir);
////                    if (rc.canMove(dir)){
////                        rc.move(dir);
////                    }
////                    else if (rc.canAttack(nextLoc)){
////                        rc.attack(nextLoc);
////                        System.out.println("Take that! Damaged an enemy that was in our way!");
////                    }
////
////                    // Rarely attempt placing traps behind the robot.
////                    MapLocation prevLoc = rc.getLocation().subtract(dir);
////                    if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && rng.nextInt() % 37 == 1)
////                        rc.build(TrapType.EXPLOSIVE, prevLoc);
////                    // We can also move our code into different methods or classes to better organize it!
////                    updateEnemyRobots(rc);
////                }
////
////            } catch (GameActionException e) {
////                // Oh no! It looks like we did something illegal in the Battlecode world. You should
////                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
////                // world. Remember, uncaught exceptions cause your robot to explode!
////                System.out.println("GameActionException");
////                e.printStackTrace();
////
////            } catch (Exception e) {
////                // Oh no! It looks like our code tried to do something bad. This isn't a
////                // GameActionException, so it's more likely to be a bug in our code.
////                System.out.println("Exception");
////                e.printStackTrace();
////
////            } finally {
////                // Signify we've done everything we want to do, thereby ending our turn.
////                // This will make our code wait until the next turn, and then perform this loop again.
////                Clock.yield();
////            }
////            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
////        }
////
////        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
////    }
////    public static void updateEnemyRobots(RobotController rc) throws GameActionException{
////        // Sensing methods can be passed in a radius of -1 to automatically 
////        // use the largest possible value.
////        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
////        if (enemyRobots.length != 0){
////            rc.setIndicatorString("There are nearby enemy robots! Scary!");
////            // Save an array of locations with enemy robots in them for future use.
////            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
////            for (int i = 0; i < enemyRobots.length; i++){
////                enemyLocations[i] = enemyRobots[i].getLocation();
////            }
////            // Let the rest of our team know how many enemy robots we see!
////            if (rc.canWriteSharedArray(0, enemyRobots.length)){
////                rc.writeSharedArray(0, enemyRobots.length);
////                int numEnemies = rc.readSharedArray(0);
////            }
////        }
////    }
////}
