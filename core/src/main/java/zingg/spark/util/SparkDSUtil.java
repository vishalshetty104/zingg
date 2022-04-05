package zingg.spark.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import zingg.client.ZFrame;
import zingg.util.DSUtil;
import zingg.scala.DFUtil;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SparkDSUtil extends DSUtil<SparkSession, Dataset<Row>, Row, Column>{

    public static final Log LOG = LogFactory.getLog(SparkDSUtil.class);	

    public ZFrame<Dataset<Row>, Row, Column> addClusterRowNumber(ZFrame<Dataset<Row>, Row, Column> ds) {
        return DFUtil.addClusterRowNumber(ds);
    }

	

	
	

	
}