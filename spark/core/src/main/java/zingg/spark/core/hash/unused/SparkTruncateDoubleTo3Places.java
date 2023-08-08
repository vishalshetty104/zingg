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

package zingg.spark.core.hash.unused;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.spark.core.hash.SparkTruncateDouble;

/**
 * Spark specific trunc function for double (3 digit)
 * 
 * 
 *
 */
public class SparkTruncateDoubleTo3Places extends SparkTruncateDouble {
	
	public static final Log LOG = LogFactory.getLog(SparkTruncateDoubleTo3Places.class);

	public SparkTruncateDoubleTo3Places(){
	    super(3);
	}
	
}
