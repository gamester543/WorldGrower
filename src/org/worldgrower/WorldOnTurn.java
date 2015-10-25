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

import java.io.Serializable;

import org.worldgrower.condition.WorldStateChangedListener;
import org.worldgrower.creaturetype.CreatureType;

/**
 * Each time a turn passes, the onTurn method is called to process ongoing effects.
 */
public interface WorldOnTurn extends Serializable {

	public void onTurn(World world);
	public void addWorldStateChangedListener(WorldStateChangedListener listener);
	public void creatureTypeChange(WorldObject worldObject, CreatureType newCreatureType, String description);
	public void electionFinished(WorldObject winner, WorldObject organization);
}
