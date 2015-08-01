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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface Deity extends Serializable {

	public String getName();
	public String getExplanation();
	
	public static final Demeter DEMETER = new Demeter();
	public static final Hephaestus HEPHAESTUS = new Hephaestus();
	public static final Hades HADES = new Hades();
	public static final Aphrodite APHRODITE = new Aphrodite();
	
	public static final List<Deity> ALL_DEITIES = Arrays.asList(
			DEMETER,
			HEPHAESTUS,
			HADES,
			APHRODITE
			);

	public static List<String> getAllDeityNames() {
		return ALL_DEITIES.stream().map(deity -> deity.getName()).collect(Collectors.toList());
	}
}