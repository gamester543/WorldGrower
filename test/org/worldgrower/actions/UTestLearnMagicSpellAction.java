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
package org.worldgrower.actions;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import org.worldgrower.Constants;
import org.worldgrower.TestUtils;
import org.worldgrower.World;
import org.worldgrower.WorldImpl;
import org.worldgrower.WorldObject;
import org.worldgrower.attribute.WorldObjectContainer;
import org.worldgrower.generator.Item;

public class UTestLearnMagicSpellAction {

	private LearnMagicSpellFromBookAction action = Actions.LEARN_MAGIC_SPELL_FROM_BOOK_ACTION;
	
	@Test
	public void testExecute() {
		World world = new WorldImpl(1, 1, null, null);
		WorldObject performer = createPerformer(1);
		world.addWorldObject(performer);
		performer.setProperty(Constants.KNOWN_SPELLS, new ArrayList<>());
		
		performer.getProperty(Constants.INVENTORY).add(Item.generateSpellBook(Actions.FIRE_BOLT_ATTACK_ACTION));
		
		action.execute(performer, performer, new int[] {0}, world);
		
		assertEquals(true, performer.getProperty(Constants.KNOWN_SPELLS).contains(Actions.FIRE_BOLT_ATTACK_ACTION));
	}
	
	@Test
	public void testIsValidInventoryItem() {
		World world = new WorldImpl(1, 1, null, null);
		WorldObject performer = createPerformer(1);
		world.addWorldObject(performer);
		performer.setProperty(Constants.KNOWN_SPELLS, new ArrayList<>());
		
		WorldObject inventoryItem = Item.generateSpellBook(Actions.FIRE_BOLT_ATTACK_ACTION);
		WorldObjectContainer inventory = performer.getProperty(Constants.INVENTORY);
		inventory.add(inventoryItem);
		
		assertEquals(true, action.isValidInventoryItem(inventoryItem, inventory, performer));
		
		performer.setProperty(Constants.KNOWN_SPELLS, Arrays.asList(Actions.FIRE_BOLT_ATTACK_ACTION));
		assertEquals(false, action.isValidInventoryItem(inventoryItem, inventory, performer));
	}
	
	private WorldObject createPerformer(int id) {
		WorldObject performer = TestUtils.createSkilledWorldObject(id, Constants.INVENTORY, new WorldObjectContainer());
		performer.setProperty(Constants.X, 0);
		performer.setProperty(Constants.Y, 0);
		performer.setProperty(Constants.WIDTH, 1);
		performer.setProperty(Constants.HEIGHT, 1);
		return performer;
	}
}