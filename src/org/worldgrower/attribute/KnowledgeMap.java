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
package org.worldgrower.attribute;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.worldgrower.Constants;
import org.worldgrower.World;
import org.worldgrower.WorldObject;

public class KnowledgeMap implements IdContainer, Serializable {

	private final Map<Integer, List<Knowledge>> idsToKnowledge = new HashMap<>();
	
	public KnowledgeMap() {
	}
	
	private KnowledgeMap(Map<Integer, List<Knowledge>> resultMap) {
		idsToKnowledge.putAll(resultMap);
	}

	public final void addKnowledge(WorldObject worldObject, ManagedProperty<?> managedProperty, Object value) {
		addKnowledge(worldObject.getProperty(Constants.ID), managedProperty, value);
	}
	
	public final void addKnowledge(int id, ManagedProperty<?> managedProperty, Object value) {
		List<Knowledge> knowledgeList = idsToKnowledge.get(id);
		if (knowledgeList == null) {
			knowledgeList = new ArrayList<>();
			idsToKnowledge.put(id, knowledgeList);
		}
		addKnowledge(knowledgeList, new Knowledge(managedProperty, value));
	}

	public void addKnowledge(WorldObject worldObject, Knowledge knowledge) {
		int id = worldObject.getProperty(Constants.ID);
		List<Knowledge> knowledgeList = idsToKnowledge.get(id);
		if (knowledgeList == null) {
			knowledgeList = new ArrayList<>();
			idsToKnowledge.put(id, knowledgeList);
		}
		addKnowledge(knowledgeList, new Knowledge(knowledge));
	}
	
	private void addKnowledge(List<Knowledge> knowledgeList, Knowledge knowledge) {
		ManagedProperty<?> property = knowledge.getManagedProperty();
		boolean knowledgeAdded = false;
		
		for(int i=0; i<knowledgeList.size(); i++) {
			if (knowledgeList.get(i).getManagedProperty() == property) {
				knowledgeList.set(i, knowledge);
				knowledgeAdded = true;
			}
		}
		
		if (!knowledgeAdded) {
			knowledgeList.add(knowledge);
		}
	}
	
	public List<WorldObject> findWorldObjects(ManagedProperty<?> managedProperty, Object value, World world) {
		List<WorldObject> worldObjects = new ArrayList<>();
		for(Entry<Integer, List<Knowledge>> entry : idsToKnowledge.entrySet()) {
			int id = entry.getKey();
			List<Knowledge> knowledgeValues = entry.getValue();
			for(Knowledge knowledgeValue : knowledgeValues) {
				if (knowledgeValue.getManagedProperty() == managedProperty && knowledgeValue.getValue().equals(value)) {
					worldObjects.add(world.findWorldObject(Constants.ID, id));
				}
			}
		}
		return worldObjects;
	}
	
	public final boolean hasProperty(WorldObject worldObject, ManagedProperty<?> managedProperty) {
		return hasProperty(worldObject.getProperty(Constants.ID), managedProperty);
	}
	
	public final boolean hasProperty(int id, ManagedProperty<?> managedProperty) {
		List<Knowledge> knowledgeList = idsToKnowledge.get(id);
		if (knowledgeList != null) {
			for(Knowledge knowledge : knowledgeList) {
				if (knowledge.getManagedProperty() == managedProperty) {
					return true;
				}
			}
		}
		return false;
	}
	
	public final boolean hasKnowledge(WorldObject worldObject) {
		return hasKnowledge(worldObject.getProperty(Constants.ID));
	}
	
	public final boolean hasKnowledge(int id) {
		List<Knowledge> knowledgeList = idsToKnowledge.get(id);
		return knowledgeList != null && knowledgeList.size() > 0;
	}
	
	public final List<Knowledge> getKnowledge(WorldObject worldObject) {
		List<Knowledge> knowledgeList = idsToKnowledge.get(worldObject.getProperty(Constants.ID));
		return knowledgeList;
	}
	
	public final void remove(int id) {
		idsToKnowledge.remove(id);
	}
	
	@Override
	public final String toString() {
		return "[" + idsToKnowledge + "]";
	}

	@Override
	public final void remove(WorldObject worldObject, ManagedProperty<?> property, int idToRemove) {
		KnowledgeMapProperty knowledgeMapProperty = (KnowledgeMapProperty) property;
		worldObject.getProperty(knowledgeMapProperty).remove(idToRemove);
		
		removeIdFromIdContainers(idToRemove);
	}

	private void removeIdFromIdContainers(int idToRemove) {
		Iterator<Entry<Integer, List<Knowledge>>> entryIterator = idsToKnowledge.entrySet().iterator();
		while(entryIterator.hasNext()) {
			Entry<Integer, List<Knowledge>> entry = entryIterator.next();
			List<Knowledge> knowledgeValues = entry.getValue();
			Iterator<Knowledge> knowledgeIterator = knowledgeValues.iterator();
			while (knowledgeIterator.hasNext()) {
				Knowledge knowledge = knowledgeIterator.next();
				if (knowledgeContainsId(idToRemove, knowledge)) {
					knowledgeIterator.remove();
					if (knowledgeValues.size() == 0) {
						entryIterator.remove();
					}
				}	
			}
		}
	}

	private boolean knowledgeContainsId(int idToRemove, Knowledge knowledge) {
		return ((knowledge.getManagedProperty() instanceof IdContainer) && (knowledge.getValue().equals(idToRemove)));
	}
	
	public KnowledgeMap copy() {
		KnowledgeMap copy = new KnowledgeMap(idsToKnowledge);
		return copy;
	}

	public KnowledgeMap subtract(KnowledgeMap knowledgeMapToSubtract) {
		Map<Integer, List<Knowledge>> resultMap = new HashMap<>(idsToKnowledge);
		Iterator<Entry<Integer, List<Knowledge>>> entrySetIterator = resultMap.entrySet().iterator();
		while(entrySetIterator.hasNext()) {
			Entry<Integer, List<Knowledge>> entry = entrySetIterator.next();
			Integer id = entry.getKey();
			removeKnowledgeMapFromEntry(knowledgeMapToSubtract, entry, id);
			if (resultMap.get(id).size() == 0) {
				entrySetIterator.remove();
			}
		}
		return new KnowledgeMap(resultMap);
	}

	private void removeKnowledgeMapFromEntry(KnowledgeMap knowledgeMapToSubtract, Entry<Integer, List<Knowledge>> entry, Integer id) {
		Iterator<Knowledge> knowledgeIterator = entry.getValue().iterator();
		while(knowledgeIterator.hasNext()) {
			Knowledge knowledge = knowledgeIterator.next();
			ManagedProperty<?> property = knowledge.getManagedProperty();
			
			List<Knowledge> knowledgeListToSubtract = knowledgeMapToSubtract.idsToKnowledge.get(id);
			if (knowledgeListToSubtract != null) {
				for(Knowledge knowledgeToSubtract : knowledgeListToSubtract) {
					if (knowledgeToSubtract.getManagedProperty() == property) {
						knowledgeIterator.remove();
					}
				}
			}
		}
	}

	public boolean hasKnowledge() {
		return !idsToKnowledge.isEmpty();
	}

	public List<Integer> getIds() {
		return new ArrayList<>(idsToKnowledge.keySet());
	}
}