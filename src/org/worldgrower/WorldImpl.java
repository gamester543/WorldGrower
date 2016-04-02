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
package org.worldgrower;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.worldgrower.attribute.IdContainer;
import org.worldgrower.attribute.ManagedProperty;
import org.worldgrower.condition.WorldStateChangedListener;
import org.worldgrower.condition.WorldStateChangedListeners;
import org.worldgrower.goal.Goal;
import org.worldgrower.history.History;
import org.worldgrower.history.HistoryImpl;
import org.worldgrower.history.Turn;
import org.worldgrower.terrain.Terrain;
import org.worldgrower.terrain.TerrainImpl;

public class WorldImpl implements World, Serializable {

	private final List<WorldObject> worldObjects = new ArrayList<>();
	private final IdToIndexMapping idToIndexMapping = new IdToIndexMapping();
	private final PropertyCache propertyCache = new PropertyCache();
	private int nextId;
	private transient List<ManagedOperationListener> listeners = new ArrayList<>();
	private final Terrain terrain;
	private final DungeonMaster dungeonMaster;
	private final History history = new HistoryImpl();
	private Turn currentTurn = new Turn();
	private final WorldOnTurn worldOnTurn;
	private transient WorldStateChangedListeners worldStateChangedListeners = new WorldStateChangedListeners();
	
	//TODO: temporary for debugging purposes
	private final transient List<Integer> removedIds = new ArrayList<>();
	
	public WorldImpl(int width, int height, DungeonMaster dungeonMaster, WorldOnTurn worldOnTurn) {
		this(new TerrainImpl(width, height), dungeonMaster, worldOnTurn);
	}
	
	private WorldImpl(Terrain terrain, DungeonMaster dungeonMaster, WorldOnTurn worldOnTurn) {
		this.terrain = terrain;
		this.dungeonMaster = dungeonMaster;
		this.worldOnTurn = worldOnTurn;
	}

	@Override
	public void addWorldObject(WorldObject worldObject) {
		worldObjects.add(worldObject);
		idToIndexMapping.idAdded(worldObjects);
		propertyCache.idAdded(worldObject);
	}
	
	@Override
	public void removeWorldObject(WorldObject worldObjectToRemove) {
		worldObjects.remove(worldObjectToRemove);
		idToIndexMapping.idRemoved(worldObjects);
		
		removeIdContainers(worldObjectToRemove);
		
		propertyCache.idRemoved(worldObjectToRemove);
		
		removedIds.add(worldObjectToRemove.getProperty(Constants.ID));
	}

	private void removeIdContainers(WorldObject worldObjectToRemove) {
		int id = worldObjectToRemove.getProperty(Constants.ID);
		for(WorldObject worldObject : worldObjects) {
			List<IdContainer> worldObjectIds = getIdProperties();
			for(IdContainer worldObjectId : worldObjectIds) {
				ManagedProperty<?> property = (ManagedProperty<?>) worldObjectId;
				if (worldObject.hasProperty(property) && (worldObject.getProperty(property) != null)) {
					worldObjectId.remove(worldObject, property, id);
				}
			}
		}
	}
	
	private List<IdContainer> getIdProperties() {
		List<IdContainer> result = new ArrayList<>();
		for(ManagedProperty<?> property : Constants.ALL_PROPERTIES) {
			if (property instanceof IdContainer) {
				result.add((IdContainer)property);
			}
		}
		return result;
	}
	
	@Override
	public List<WorldObject> getWorldObjects() {
		return Collections.unmodifiableList(worldObjects);
	}

	@Override
	public List<WorldObject> findWorldObjects(WorldObjectCondition worldObjectCondition) {
		return worldObjects
			.stream()
			.filter(w -> worldObjectCondition.isWorldObjectValid(w))
			.collect(Collectors.toList());
	}
	
	@Override
	public List<WorldObject> findWorldObjectsByProperty(ManagedProperty<?> managedProperty, WorldObjectCondition worldObjectCondition) {
		return propertyCache.findWorldObjectsByProperty(managedProperty, worldObjectCondition, this);
	}
	
	public<T> WorldObject findWorldObject(ManagedProperty<T> propertyKey, T value) {
		if (propertyKey == Constants.ID) {
			return findWorldObjectById((Integer) value);
		} else {
			List<WorldObject> result = 
					worldObjects
					.stream()
					.filter(w -> w.getProperty(propertyKey).equals(value))
					.collect(Collectors.toList());
			if (result.size() == 1) {
				return result.get(0);
			} else {
				throw new IllegalStateException("Problem finding worldObject with propertyKey " + propertyKey + " and value " + value);
			}
		}
	}
	
	private WorldObject findWorldObjectById(int id) {
		try {
			int index = idToIndexMapping.getIndex(id);
			return worldObjects.get(index);
		} catch(IllegalStateException ex) {
			throw new IllegalStateException("Problem retrieving id " + id + ": " + ex.getMessage() + "; removed ids = " + removedIds);
		}
	}
	
	@Override
	public boolean exists(WorldObject worldObject) {
		return idToIndexMapping.idExists(worldObject.getProperty(Constants.ID));
	}
	
	@Override
	public boolean exists(int id) {
		return idToIndexMapping.idExists(id);
	}
	
	@Override
	public int generateUniqueId() {
		return nextId++;
	}

	@Override
	public void addListener(ManagedOperationListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeListener(ManagedOperationListener listener) {
		listeners.remove(listener);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getListenerByClass(Class<T> clazz) {
		for(ManagedOperationListener listener : listeners) {
			if (listener.getClass() == clazz) {
				return (T) listener;
			}
		}
		throw new IllegalStateException("Listener with class " + clazz + " not found in list of listeners " + listeners);
	}
	
	@Override
	public<T> void logAction(ManagedOperation managedOperation, WorldObject performer, WorldObject target, int[] args, T value) {
		listeners.stream().forEach(l -> l.actionPerformed(managedOperation, performer, target, args, value));
	}

	@Override
	public int getWidth() {
		return terrain.getWidth();
	}

	@Override
	public int getHeight() {
		return terrain.getHeight();
	}

	@Override
	public Terrain getTerrain() {
		return terrain;
	}
	
	@Override
	public Goal getGoal(WorldObject worldObject) {
		return dungeonMaster.getGoal(worldObject);
	}
	
	@Override
	public OperationInfo getImmediateGoal(WorldObject worldObject, World world) {
		return dungeonMaster.getImmediateGoal(worldObject, world);
	}

	@Override
	public History getHistory() {
		return history;
	}
	
	@Override
	public void save(File fileToSave) {
		try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(fileToSave)))) {

			objectOutputStream.writeObject ( Version.getVersion() );
	    	objectOutputStream.writeObject ( this );
	    	
		} catch(IOException ex) {
			throw new RuntimeException("Problem saving file " + fileToSave, ex);
		}
	}
	
	public static World load(File fileToLoad) {
		try (ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(fileToLoad)))) {
			String versionFromFile = (String) objectInputStream.readObject();
			
			if (!versionFromFile.equals( Version.getVersion() )) {
				throw new RuntimeException("Version in file " + fileToLoad + " doesn't match: " + versionFromFile + " isn't equal to " + Version.getVersion());
			}
			
			WorldImpl world = (WorldImpl) objectInputStream.readObject();
			world.listeners = new ArrayList<>();
			world.worldStateChangedListeners = new WorldStateChangedListeners();
			return world;
			
		} catch(IOException | ClassNotFoundException ex) {
			throw new RuntimeException("Problem loading file " + fileToLoad, ex);
		}
	}
	
	@Override
	public Turn getCurrentTurn() {
		return currentTurn;
	}

	@Override
	public void nextTurn() {
		worldOnTurn.onTurn(this);
		currentTurn = currentTurn.next();
	}

	@Override
	public WorldOnTurn getWorldOnTurn() {
		return worldOnTurn;
	}
	
	@Override
	public WorldStateChangedListeners getWorldStateChangedListeners() {
		return worldStateChangedListeners;
	}
	
	@Override
	public void addWorldStateChangedListener(WorldStateChangedListener worldStateChangedListener) {
		worldStateChangedListeners.addWorldStateChangedListener(worldStateChangedListener);
	}
}
