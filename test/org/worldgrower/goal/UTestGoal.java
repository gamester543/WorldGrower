/*******************************************************************************
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.worldgrower.goal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.worldgrower.Constants;
import org.worldgrower.TestUtils;
import org.worldgrower.World;
import org.worldgrower.WorldImpl;
import org.worldgrower.WorldObject;
import org.worldgrower.attribute.PropertyCountMap;
import org.worldgrower.personality.Personality;
import org.worldgrower.personality.PersonalityTrait;

public class UTestGoal {

	private FoodGoal goal = Goals.FOOD_GOAL;
	
	@Test
	public void testDefaultGoalMetOrNot() {
		World world = new WorldImpl(1, 1, null, null);
		WorldObject performer = createPerformer();
		performer.setProperty(Constants.DEMANDS, new PropertyCountMap<>());
		
		goal.defaultGoalMetOrNot(performer, world, false, Constants.FOOD);
		assertEquals(1, performer.getProperty(Constants.DEMANDS).count(Constants.FOOD));
		
		goal.defaultGoalMetOrNot(performer, world, true, Constants.FOOD);
		assertEquals(0, performer.getProperty(Constants.DEMANDS).count(Constants.FOOD));
	}
	
	@Test
	public void testCalculateSign() {
		WorldObject performer = createPerformer();
		performer.setProperty(Constants.NAME, "performer");
		
		assertEquals(-1, goal.calculateSign(performer, PersonalityTrait.GREEDY));
		
		performer.setProperty(Constants.NAME, "qerformer");
		assertEquals(1, goal.calculateSign(performer, PersonalityTrait.GREEDY));
	}
	
	@Test
	public void testChangePersonality() {
		World world = new WorldImpl(1, 1, null, null);
		WorldObject performer = createPerformer();
		performer.setProperty(Constants.NAME, "performer");
		performer.setProperty(Constants.FOOD, 0);
		
		goal.changePersonality(performer, PersonalityTrait.GREEDY, 10, false, "hungry", world);
		
		assertEquals(-10, performer.getProperty(Constants.PERSONALITY).getValue(PersonalityTrait.GREEDY));
	}
	
	@Test
	public void testChangePersonalityGoalMet() {
		World world = new WorldImpl(1, 1, null, null);
		WorldObject performer = createPerformer();
		performer.setProperty(Constants.NAME, "performer");
		performer.setProperty(Constants.FOOD, 0);
		
		goal.changePersonality(performer, PersonalityTrait.GREEDY, 10, true, "hungry", world);
		
		assertEquals(0, performer.getProperty(Constants.PERSONALITY).getValue(PersonalityTrait.GREEDY));
	}
	
	@Test
	public void testChangePersonalityUrgentGoalMet() {
		World world = new WorldImpl(1, 1, null, null);
		WorldObject performer = createPerformer();
		performer.setProperty(Constants.NAME, "performer");
		performer.setProperty(Constants.FOOD, 500);
		
		goal.changePersonality(performer, PersonalityTrait.GREEDY, 10, false, "hungry", world);
		
		assertEquals(0, performer.getProperty(Constants.PERSONALITY).getValue(PersonalityTrait.GREEDY));
	}

	private WorldObject createPerformer() {
		WorldObject performer = TestUtils.createSkilledWorldObject(1, Constants.PERSONALITY, new Personality());
		performer.setProperty(Constants.X, 0);
		performer.setProperty(Constants.Y, 0);
		performer.setProperty(Constants.WIDTH, 1);
		performer.setProperty(Constants.HEIGHT, 1);
		return performer;
	}
}