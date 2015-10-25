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

import org.worldgrower.condition.WorldStateChangedListeners;

/**
 * Each time a turn passes, the onTurn method is called to process ongoing effects on a WorldObject.
 */
public interface OnTurn extends Serializable {

	public void onTurn(WorldObject worldObject, World world, WorldStateChangedListeners creatureTypeChangedListeners);
}
