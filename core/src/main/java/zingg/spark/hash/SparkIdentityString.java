package zingg.spark.hash;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

import zingg.client.ZFrame;
import zingg.hash.IdentityString;

import org.apache.spark.sql.types.DataType;

public class SparkIdentityString extends IdentityString<Dataset<Row>, Row, Column,DataType,DataType> implements UDF1<String, String>{
	
	public SparkIdentityString() {
		super();
		setDataType(DataTypes.StringType);
		setReturnType(DataTypes.StringType);
	}

	@Override
	public Object getAs(Row r, String column) {
		return (String) r.getAs(column);
	}



	@Override
	public ZFrame<Dataset<Row>, Row, Column> apply(ZFrame<Dataset<Row>, Row, Column> ds, String column,
			String newColumn) {
		return ds.withColumn(newColumn, functions.callUDF(this.name, ds.col(column)));
	}


	
}