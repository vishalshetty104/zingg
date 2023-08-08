/*
 * Zingg
 * Copyright (C) 2021-Present  Zingg Labs,inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package zingg.common.core.hash;

/**
 * Base class for hash functions related to trimming of longs
 *
 */
public class TrimLastDigitsLong extends BaseHash<Long,Long>{
	private static final long serialVersionUID = 1L;
	private int numDigits;
	static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000};
	public TrimLastDigitsLong(int count) {
	    setName("trimLast" + count + "DigitsLong");
		this.numDigits = count;
	}

	public Long call(Long field) {
		Long r = null;
		if (field == null) {
			r = field;
		} else {
			r = field / POWERS_OF_10[numDigits];
		}
		return r;
	}

    public int getNumDigits() {
        return numDigits;
    }
	
}
