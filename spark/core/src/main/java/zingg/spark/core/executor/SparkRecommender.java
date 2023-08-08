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

package zingg.spark.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.Arguments;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.client.license.IZinggLicense;
import zingg.common.core.executor.Recommender;
import zingg.common.core.recommender.StopWordsRecommender;
import zingg.spark.client.ZSparkSession;
import zingg.spark.core.recommender.SparkStopWordsRecommender;


/**
 * Spark specific implementation of Recommender
 *
 */
public class SparkRecommender extends Recommender<ZSparkSession, Dataset<Row>, Row, Column,DataType> {

	private static final long serialVersionUID = 1L;
	public static String name = "zingg.spark.core.executor.SparkRecommender";
	public static final Log LOG = LogFactory.getLog(SparkRecommender.class);

	public SparkRecommender() {
		setZinggOptions(ZinggOptions.RECOMMEND);
		setContext(new ZinggSparkContext());
	}

    @Override
    public void init(Arguments args, IZinggLicense license)  throws ZinggClientException {
        super.init(args, license);
        getContext().init(license);
    }	

    @Override
    public StopWordsRecommender<ZSparkSession, Dataset<Row>, Row, Column, DataType> getStopWordsRecommender() {
    	StopWordsRecommender<ZSparkSession, Dataset<Row>, Row, Column, DataType> stopWordsRecommender = new SparkStopWordsRecommender(getContext(),args);    	
    	return stopWordsRecommender;
    }
	
}
