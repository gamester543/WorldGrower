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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UTestConstants {

	@Test
	public void testIsTool() {
		assertEquals(false, Constants.isTool(Constants.HIT_POINTS));
		assertEquals(true, Constants.isTool(Constants.REPAIR_QUALITY));
		assertEquals(true, Constants.isTool(Constants.SAW_QUALITY));
	}
	
	@Test
	public void testGetToolProperties() {
		assertEquals(false, Constants.getToolProperties().contains(Constants.HIT_POINTS));
		assertEquals(true, Constants.getToolProperties().contains(Constants.REPAIR_QUALITY));
		assertEquals(true, Constants.getToolProperties().contains(Constants.SAW_QUALITY));
	}
}
