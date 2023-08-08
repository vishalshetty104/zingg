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

package zingg.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import zingg.common.client.Arguments;
import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;
import zingg.spark.client.SparkFrame;

public class TestSparkFrameBase {

	public static Arguments args;
	public static JavaSparkContext ctx;
	public static SparkSession spark;

	public static final Log LOG = LogFactory.getLog(TestSparkFrameBase.class);

	public static final String STR_RECID = "recid";
	public static final String STR_GIVENNAME = "givenname";
	public static final String STR_SURNAME = "surname";
	public static final String STR_COST = "cost";
	public static final String STR_POSTCODE = "postcode";
	public static final String STR_SUBURB = "suburb";

	@BeforeAll
	public static void setup() {
		setUpSpark();
	}

	protected static void setUpSpark() {
		try {
			spark = SparkSession
					.builder()
					.master("local[*]")
					.appName("Zingg" + "Junit")
					.getOrCreate();
			ctx = new JavaSparkContext(spark.sparkContext());
			JavaSparkContext.jarOfClass(TestSparkFrameBase.class);
			args = new Arguments();
		} catch (Throwable e) {
			if (LOG.isDebugEnabled())
				e.printStackTrace();
			LOG.info("Problem in spark env setup");
		}
	}

	@AfterAll
	public static void teardown() {
		if (ctx != null) {
			ctx.stop();
			ctx = null;
		}
		if (spark != null) {
			spark.stop();
			spark = null;
		}
	}

	public Dataset<Row> createSampleDataset() {
		
		if (spark==null) {
			setUpSpark();
		}
		
		StructType schemaOfSample = new StructType(new StructField[] {
				new StructField("recid", DataTypes.StringType, false, Metadata.empty()),
				new StructField("givenname", DataTypes.StringType, false, Metadata.empty()),
				new StructField("surname", DataTypes.StringType, false, Metadata.empty()),
				new StructField("suburb", DataTypes.StringType, false, Metadata.empty()),
				new StructField("postcode", DataTypes.StringType, false, Metadata.empty())
		});

		Dataset<Row> sample = spark.createDataFrame(Arrays.asList(
				RowFactory.create("07317257", "erjc", "henson", "hendersonville", "2873g"),
				RowFactory.create("03102490", "jhon", "kozak", "henders0nville", "28792"),
				RowFactory.create("02890805", "david", "pisczek", "durham", "27717"),
				RowFactory.create("04437063", "e5in", "bbrown", "greenville", "27858"),
				RowFactory.create("03211564", "susan", "jones", "greenjboro", "274o7"),
				RowFactory.create("04155808", "jerome", "wilkins", "battleborn", "2780g"),
				RowFactory.create("05723231", "clarinw", "pastoreus", "elizabeth city", "27909"),
				RowFactory.create("06087743", "william", "craven", "greenshoro", "27405"),
				RowFactory.create("00538491", "marh", "jackdon", "greensboro", "27406"),
				RowFactory.create("01306702", "vonnell", "palmer", "siler sity", "273q4")), schemaOfSample);

		return sample;
	}

	public Dataset<Row> createSampleDatasetHavingMixedDataTypes() {
		if (spark==null) {
			setUpSpark();
		}
		
		StructType schemaOfSample = new StructType(new StructField[] {
				new StructField(STR_RECID, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(STR_GIVENNAME, DataTypes.StringType, false, Metadata.empty()),
				new StructField(STR_SURNAME, DataTypes.StringType, false, Metadata.empty()),
				new StructField(STR_COST, DataTypes.DoubleType, false, Metadata.empty()),
				new StructField(STR_POSTCODE, DataTypes.IntegerType, false, Metadata.empty())
		});

		Dataset<Row> sample = spark.createDataFrame(Arrays.asList(
				RowFactory.create(7317, "erjc", "henson", 0.54, 2873),
				RowFactory.create(3102, "jhon", "kozak", 99.009, 28792),
				RowFactory.create(2890, "david", "pisczek", 58.456, 27717),
				RowFactory.create(4437, "e5in", "bbrown", 128.45, 27858)
				), schemaOfSample);

		return sample;
	}

	protected SparkFrame getZScoreDF() {
		Row[] rows = { 
				RowFactory.create( 0,100,900),
				RowFactory.create( 1,100,1001),
				RowFactory.create( 1,100,1002),
				RowFactory.create( 1,100,2001),
				RowFactory.create( 1,100,2002),
				RowFactory.create( 11,100,9002),
				RowFactory.create( 3,300,3001),
				RowFactory.create( 3,300,3002),
				RowFactory.create( 3,400,4001),
				RowFactory.create( 4,400,4002)
		};
		StructType schema = new StructType(new StructField[] {
				new StructField(ColName.ID_COL, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(ColName.CLUSTER_COLUMN, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(ColName.SCORE_COL, DataTypes.IntegerType, false, Metadata.empty())});
		SparkFrame df = new SparkFrame(spark.createDataFrame(Arrays.asList(rows), schema));
		return df;
	}	

	protected SparkFrame getInputData() {
		Row[] rows = { 
				RowFactory.create( 1,"fname1","b"),
				RowFactory.create( 2,"fname","a"),
				RowFactory.create( 3,"fna","b"),
				RowFactory.create( 4,"x","c"),
				RowFactory.create( 5,"y","c"),
				RowFactory.create( 11,"new1","b"),
				RowFactory.create( 22,"new12","a"),
				RowFactory.create( 33,"new13","b"),
				RowFactory.create( 44,"new14","c"),
				RowFactory.create( 55,"new15","c")				
		};
		StructType schema = new StructType(new StructField[] {
				new StructField(ColName.ID_COL, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField("fname", DataTypes.StringType, false, Metadata.empty()),
				new StructField(ColName.SOURCE_COL, DataTypes.StringType, false, Metadata.empty())});
		SparkFrame df = new SparkFrame(spark.createDataFrame(Arrays.asList(rows), schema));
		return df;
	}	
	
	
	protected SparkFrame getClusterData() {
		Row[] rows = { 
				RowFactory.create( 1,100,1001,"b"),
				RowFactory.create( 2,100,1002,"a"),
				RowFactory.create( 3,100,2001,"b"),
				RowFactory.create( 4,900,2002,"c"),
				RowFactory.create( 5,111,9002,"c")
		};
		StructType schema = new StructType(new StructField[] {
				new StructField(ColName.ID_COL, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(ColName.CLUSTER_COLUMN, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(ColName.SCORE_COL, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(ColName.SOURCE_COL, DataTypes.StringType, false, Metadata.empty())});
		SparkFrame df = new SparkFrame(spark.createDataFrame(Arrays.asList(rows), schema));
		return df;
	}	
	
	protected SparkFrame getClusterDataWithNull() {
		Row[] rows = { 
				RowFactory.create( 1,100,1001,"b"),
				RowFactory.create( 2,100,1002,"a"),
				RowFactory.create( 3,100,2001,null),
				RowFactory.create( 4,900,2002,"c"),
				RowFactory.create( 5,111,9002,null)
		};
		StructType schema = new StructType(new StructField[] {
				new StructField(ColName.COL_PREFIX+ ColName.ID_COL, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(ColName.CLUSTER_COLUMN, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(ColName.SCORE_COL, DataTypes.IntegerType, false, Metadata.empty()),
				new StructField(ColName.SOURCE_COL, DataTypes.StringType, true, Metadata.empty())});
		SparkFrame df = new SparkFrame(spark.createDataFrame(Arrays.asList(rows), schema));
		return df;
	}	
	
	protected void assertTrueCheckingExceptOutput(ZFrame<Dataset<Row>, Row, Column> sf1, ZFrame<Dataset<Row>, Row, Column> sf2, String message) {
		assertTrue(sf1.except(sf2).isEmpty(), message);
	}
	
	
	protected void assertTrueCheckingExceptOutput(ZFrame<Dataset<Row>, Row, Column> sf1, Dataset<Row> df2, String message) {
		SparkFrame sf2 = new SparkFrame(df2);
		assertTrue(sf1.except(sf2).isEmpty(), message);
	}
}