/*******************************************************************************
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*******************************************************************************/
package org.worldgrower;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.worldgrower.actions.Actions;
import org.worldgrower.actions.BrawlListener;
import org.worldgrower.attribute.IdMap;
import org.worldgrower.attribute.IdRelationshipMap;
import org.worldgrower.curse.CurseListener;
import org.worldgrower.history.Turn;

public class UTestWorldImpl {

	private World world;
	
	public UTestWorldImpl() {
		world = createWorld();
		WorldObject worldObject = TestUtils.createWorldObject(6, "test");
		world.addWorldObject(worldObject);
	}
	
	@Test
	public void testFindWorldObjects() {
		List<WorldObject> result = world.findWorldObjects(w -> w.getProperty(Constants.ID).intValue() == 6);
		
		assertEquals(1, result.size());
		assertEquals(6, result.get(0).getProperty(Constants.ID).intValue());
	}
	
	@Test
	public void testFindWorldObject() {
		WorldObject result = world.findWorldObjectById(6);
		assertEquals(6, result.getProperty(Constants.ID).intValue());
	}
	
	@Test
	public void testFindWorldObjectNonExisting() {
		try {
			world.findWorldObjectById(7);
			fail("method should fail");
		} catch(IllegalStateException e) {
			assertEquals("Problem retrieving id 7: Id 7 not found in idToIndexMapping; removed ids = []", e.getMessage());	
		}
	}
	
	@Test
	public void testRemoveWorldObject() {
		World world = createWorld();
		WorldObject worldObject = TestUtils.createWorldObject(world.generateUniqueId(), "test");
		int id = worldObject.getProperty(Constants.ID);
		world.addWorldObject(worldObject);
		
		assertEquals(1, world.findWorldObjects(w -> w.getProperty(Constants.ID).intValue() == id).size());
		
		world.removeWorldObject(worldObject);
		assertEquals(0, world.findWorldObjects(w -> w.getProperty(Constants.ID).intValue() == id).size());
	}
	
	@Test
	public void testRemoveDependentWorldObject() {
		World world = createWorld();
		WorldObject person1 = TestUtils.createWorldObject(world.generateUniqueId(), "test");
		world.addWorldObject(person1);
		
		Integer personId1 = person1.getProperty(Constants.ID);
		WorldObject person2 = TestUtils.createIntelligentWorldObject(world.generateUniqueId(), Constants.ARENA_OPPONENT_ID, personId1);
		world.addWorldObject(person2);
		
		assertEquals(personId1, person2.getProperty(Constants.ARENA_OPPONENT_ID));
		
		world.removeWorldObject(person1);
		assertEquals(null, person2.getProperty(Constants.ARENA_OPPONENT_ID));
	}
	
	@Test
	public void testRemoveWorldObjectInIdContainer() {
		World world = createWorld();
		WorldObject person1 = TestUtils.createWorldObject(world.generateUniqueId(), "test");
		IdMap idMap = new IdRelationshipMap();
		idMap.incrementValue(person1, 6);
		WorldObject person2 = TestUtils.createIntelligentWorldObject(world.generateUniqueId(), Constants.RELATIONSHIPS, idMap);
		
		world.addWorldObject(person1);
		world.addWorldObject(person2);

		world.removeWorldObject(person1);
		assertEquals(false, idMap.contains(person1));
		assertEquals(-1, idMap.findBestId(w -> true, world));
		
	}
	
	@Test
	public void testSaveLoad() throws IOException {
		File fileToSave = File.createTempFile("worldgrower", ".sav");
		World world = createWorld();
		WorldObject house = TestUtils.createWorldObject(6, "test");
		WorldObject person = TestUtils.createIntelligentWorldObject(7, Constants.ARENA_OPPONENT_ID, 6);
		house.setProperty(Constants.LEVEL, 1);
		
		world.addWorldObject(house);
		world.addWorldObject(person);
		
		world.getHistory().actionPerformed(new OperationInfo(person, house, Args.EMPTY, Actions.MELEE_ATTACK_ACTION), new Turn());
		
		world.save(fileToSave);
		world = WorldImpl.load(fileToSave);
		
		assertEquals(2, world.getWorldObjects().size());
		assertEquals(Actions.MELEE_ATTACK_ACTION, world.getHistory().getHistoryItem(0).getManagedOperation());
	}
	
	@Test
	public void testIdHigherThan128() {
		// this test is for java Integer caching, see http://stackoverflow.com/questions/3131136/integers-caching-in-java
		World world = createWorld();
		for(int i=0; i<300; i++) {
			int id = world.generateUniqueId();
			world.addWorldObject(TestUtils.createWorldObject(id, "test"));
		}
		assertEquals(290, world.findWorldObjectById(290).getProperty(Constants.ID).intValue());
	}
	
	@Test
	public void testFindWorldObjectsByPropertyMatchingCondition() {
		World world = createWorld();
		WorldObject person1 = TestUtils.createIntelligentWorldObject(6, Constants.FOOD, 500);
		world.addWorldObject(person1);
		
		List<WorldObject> worldObjects = world.findWorldObjectsByProperty(Constants.FOOD, w -> w.getProperty(Constants.FOOD) > 0);
		assertEquals(1, worldObjects.size());
		assertEquals(6, worldObjects.get(0).getProperty(Constants.ID).intValue());
	}
	
	@Test
	public void testFindWorldObjectsByPropertyNonMatchingCondition() {
		World world = createWorld();
		WorldObject person1 = TestUtils.createIntelligentWorldObject(6, Constants.FOOD, 500);
		world.addWorldObject(person1);
		
		List<WorldObject> worldObjects = world.findWorldObjectsByProperty(Constants.FOOD, w -> w.getProperty(Constants.FOOD) > 700);
		assertEquals(0, worldObjects.size());
	}
	
	@Test
	public void testFindWorldObjectsByPropertyRemove() {
		World world = createWorld();
		WorldObject person1 = TestUtils.createIntelligentWorldObject(6, Constants.FOOD, 500);
		world.addWorldObject(person1);
		world.removeWorldObject(person1);
		
		List<WorldObject> worldObjects = world.findWorldObjectsByProperty(Constants.FOOD, w -> w.getProperty(Constants.FOOD) > 0);
		assertEquals(0, worldObjects.size());
	}
	
	@Test
	public void testFindWorldObjectsByPropertyLazyInstantiation() {
		World world = createWorld();
		
		List<WorldObject> worldObjects = world.findWorldObjectsByProperty(Constants.FOOD, w -> w.getProperty(Constants.FOOD) > 0);
		assertEquals(0, worldObjects.size());
		
		WorldObject person1 = TestUtils.createIntelligentWorldObject(world.generateUniqueId(), Constants.FOOD, 500);
		world.addWorldObject(person1);
		
		worldObjects = world.findWorldObjectsByProperty(Constants.FOOD, w -> w.getProperty(Constants.FOOD) > 0);
		assertEquals(1, worldObjects.size());
		assertEquals(person1, worldObjects.get(0));
	}
	
	@Test
	public void testGetListenerByClass() {
		World world = createWorld();
		CurseListener curseListener = new CurseListener(world);
		world.addListener(curseListener);
		
		assertEquals(curseListener, world.getListenerByClass(CurseListener.class));
	}
	
	@Test
	public void testGetListenerByClassNonExistingListener() {
		World world = createWorld();
		CurseListener curseListener = new CurseListener(world);
		world.addListener(curseListener);
		
		try {
			world.getListenerByClass(BrawlListener.class);
			fail("method should fail");
		} catch(IllegalStateException ex) {
			assertEquals(true, ex.getMessage().startsWith("Listener with class class org.worldgrower.actions.BrawlListener not found in list of listeners"));
		}
	}

	private WorldImpl createWorld() {
		return new WorldImpl(1, 1, null, null);
	}
}
