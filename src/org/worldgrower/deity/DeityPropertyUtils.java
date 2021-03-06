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
package org.worldgrower.deity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.worldgrower.Constants;
import org.worldgrower.World;
import org.worldgrower.WorldObject;
import org.worldgrower.attribute.IntProperty;
import org.worldgrower.goal.GroupPropertyUtils;

public class DeityPropertyUtils {

	public static boolean deityIsLessWorshippedThanOthers(Deity deity, World world) {
		List<WorldObject> worshippers = getAllWorshippers(world);
		Map<Deity, Integer> worshippersByDeity = createWorshippersByDeity();
		
		for(WorldObject worshipper : worshippers) {
			Deity worshippedDeity = worshipper.getProperty(Constants.DEITY);
			worshippersByDeity.put(worshippedDeity, worshippersByDeity.get(worshippedDeity) + 1);
		}
		
		int worshipCountForDeity = worshippersByDeity.get(deity);
		int averageWorshipCount = getAverageWorshipCount(worshippersByDeity);
		
		if (worshipCountForDeity < averageWorshipCount) {
			return true;
		} else {
			return false;
		}
	}
	
	public static int getTotalNumberOfWorshippers(World world) {
		List<WorldObject> worshippers = getAllWorshippers(world);
		return worshippers.size();
	}
	
	public static List<WorldObject> getAllWorshippers(World world) {
		List<WorldObject> worshippers = world.findWorldObjectsByProperty(Constants.STRENGTH, w -> w.hasProperty(Constants.DEITY) && w.getProperty(Constants.DEITY) != null);
		return worshippers;
	}
	
	public static Map<Deity, Integer> getWorshippersByDeity(World world) {
		List<WorldObject> worshippers = world.findWorldObjectsByProperty(Constants.STRENGTH, w -> w.hasProperty(Constants.DEITY) && w.getProperty(Constants.DEITY) != null);
		Map<Deity, Integer> worshippersByDeity = new HashMap<>();
		for(Deity deity : Deity.ALL_DEITIES) {
			worshippersByDeity.put(deity, 0);
		}
		for(WorldObject worshipper : worshippers) {
			Deity deity = worshipper.getProperty(Constants.DEITY);
			worshippersByDeity.put(deity, worshippersByDeity.get(deity) + 1);
		}
		
		return worshippersByDeity;
	}
	
	private static Map<Deity, Integer> createWorshippersByDeity() {
		Map<Deity, Integer> worshippersByDeity = new HashMap<>();
		for(Deity deity : Deity.ALL_DEITIES) {
			worshippersByDeity.put(deity, 0);
		}
		
		return worshippersByDeity;
	}
	
	private static int getAverageWorshipCount(Map<Deity, Integer> worshippersByDeity) {
		int totalWorshipperCount = 0;
		for(int count : worshippersByDeity.values()) {
			totalWorshipperCount += count;
		}
		
		return totalWorshipperCount / worshippersByDeity.size();
	}
	
	public static List<WorldObject> getWorshippersFor(Deity deity, World world) {
		List<WorldObject> targets = world.findWorldObjectsByProperty(Constants.STRENGTH, w -> w.hasProperty(Constants.DEITY) && w.getProperty(Constants.DEITY) == deity);
		return targets;
	}
	
	public static boolean deityIsUnhappy(Deity deity, World world) {
		return GroupPropertyUtils.getVillagersOrganization(world).getProperty(Constants.DEITY_ATTRIBUTES).isUnHappy(deity);
	}
	
	public static void destroyResource(IntProperty resourceProperty, Deity deity, String description, World world) {
		List<WorldObject> targets = world.findWorldObjectsByProperty(resourceProperty, w -> true);
		final WorldObject target;
		if (targets.size() > 0) {
			target = targets.get(0);
		} else {
			target = null;
		}
		
		if (target != null) {
			target.setProperty(Constants.HIT_POINTS, 0);
			world.getWorldStateChangedListeners().deityRetributed(deity, description);
		}
	}
}
