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

package zingg.spark.core.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.Transformer;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.feature.PolynomialExpansion;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;
import zingg.common.core.feature.Feature;
import zingg.common.core.model.Model;
import zingg.common.core.similarity.function.SimFunction;
import zingg.spark.client.SparkFrame;
import zingg.spark.client.ZSparkSession;
import zingg.spark.core.similarity.SparkSimFunction;
import zingg.spark.core.similarity.SparkTransformer;

public class SparkModel extends Model<ZSparkSession, DataType, Dataset<Row>, Row, Column>{
	
	public static final Log LOG = LogFactory.getLog(SparkModel.class);
	public static final Log DbLOG = LogFactory.getLog("WEB");
	//private Map<FieldDefinition, Feature> featurers;
	List<PipelineStage> pipelineStage;
	List<SparkTransformer> featureCreators; 
	LogisticRegression lr;
	Transformer transformer;
	BinaryClassificationEvaluator binaryClassificationEvaluator;
	List<String> columnsAdded;
	VectorValueExtractor vve;
	
	public SparkModel(Map<FieldDefinition, Feature<DataType>> f) {
		featureCreators = new ArrayList<SparkTransformer>();
		pipelineStage = new ArrayList<PipelineStage> ();
		columnsAdded = new ArrayList<String> ();
		int count = 0;
		for (FieldDefinition fd : f.keySet()) {
			Feature fea = f.get(fd);
			List<SimFunction> sfList = fea.getSimFunctions();
			for (SimFunction sf : sfList) {
				String outputCol = ColName.SIM_COL + count;
				columnsAdded.add(outputCol);	
				SparkTransformer st = new SparkTransformer(fd.fieldName, new SparkSimFunction(sf), outputCol);
				count++;
				//pipelineStage.add(sf);
				featureCreators.add(st);
			}
		}
		
		VectorAssembler assembler = new VectorAssembler();
		assembler.setInputCols(columnsAdded.toArray(new String[columnsAdded.size()]));
		assembler.setOutputCol(ColName.FEATURE_VECTOR_COL);
		columnsAdded.add(ColName.FEATURE_VECTOR_COL);
		pipelineStage.add(assembler);
		PolynomialExpansion polyExpansion = new PolynomialExpansion()
		  .setInputCol(ColName.FEATURE_VECTOR_COL)
		  .setOutputCol(ColName.FEATURE_COL)
		  .setDegree(3);	
		columnsAdded.add(ColName.FEATURE_COL);
		pipelineStage.add(polyExpansion);
		lr = new LogisticRegression();
		lr.setMaxIter(100);
		lr.setFeaturesCol(ColName.FEATURE_COL);
		lr.setLabelCol(ColName.MATCH_FLAG_COL);
		lr.setProbabilityCol(ColName.PROBABILITY_COL);
		lr.setPredictionCol(ColName.PREDICTION_COL);
		lr.setFitIntercept(true);
		pipelineStage.add(lr);
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																					
		vve = new VectorValueExtractor(ColName.PROBABILITY_COL,ColName.SCORE_COL);
		//vve.setInputCol(ColName.PROBABILITY_COL);
		//vve.setOutputCol(ColName.SCORE_COL);
		//pipelineStage.add(vve);
		columnsAdded.add(ColName.PROBABILITY_COL);	
		columnsAdded.add(ColName.RAW_PREDICTION);	
	}
	
	
	
	public void fit(ZFrame<Dataset<Row>,Row,Column> pos, ZFrame<Dataset<Row>,Row,Column> neg) {
		//transform
		ZFrame<Dataset<Row>,Row,Column> input = transform(pos.union(neg)).coalesce(1).cache();
		//if (LOG.isDebugEnabled()) input.write().csv("/tmp/input/" + System.currentTimeMillis());
		Pipeline pipeline = new Pipeline();
		pipeline.setStages(pipelineStage.toArray(new PipelineStage[pipelineStage.size()]));
		
		LOG.debug("Pipeline is " + pipeline);
		//create lr params
		ParamMap[] paramGrid = new ParamGridBuilder()
		  .addGrid(lr.regParam(), getGrid(0.0001, 1, 10, true))
		  .addGrid(lr.threshold(), getGrid(0.40, 0.55, 0.05, false))
		  .build();
		
		binaryClassificationEvaluator = new BinaryClassificationEvaluator();
		binaryClassificationEvaluator.setLabelCol(ColName.MATCH_FLAG_COL);
		CrossValidator cv = new CrossValidator()
		  .setEstimator(pipeline)
		  .setEvaluator(binaryClassificationEvaluator)
		  .setEstimatorParamMaps(paramGrid)
		  .setNumFolds(2);  // Use 3+ in practice
		  //.setParallelism(2);
		CrossValidatorModel cvModel = cv.fit(input.df());
		transformer = cvModel;
		LOG.debug("threshold after fitting is " + lr.getThreshold());
	}
	
	
	public void load(String path) {
		transformer =  CrossValidatorModel.load(path);
	}
	
	
	public ZFrame<Dataset<Row>,Row,Column> predict(ZFrame<Dataset<Row>,Row,Column> data) {
		return predict(data, true);
	}
	
	@Override
	public ZFrame<Dataset<Row>,Row,Column> predict(ZFrame<Dataset<Row>,Row,Column> data, boolean isDrop) {
		//create features
		LOG.info("threshold while predicting is " + lr.getThreshold());
		//lr.setThreshold(0.95);
		//LOG.info("new threshold while predicting is " + lr.getThreshold());
		
		Dataset<Row> predictWithFeatures = transformer.transform(transform(data).df());
		//LOG.debug(predictWithFeatures.schema());
		predictWithFeatures = vve.transform(predictWithFeatures);
		//LOG.debug("Original schema is " + predictWithFeatures.schema());
		if (isDrop) {
			Dataset<Row> returnDS = predictWithFeatures.drop(columnsAdded.toArray(new String[columnsAdded.size()]));
			//LOG.debug("Return schema after dropping additional columns is " + returnDS.schema());
			return new SparkFrame(returnDS);
		}
		LOG.debug("Return schema is " + predictWithFeatures.schema());
		return new SparkFrame(predictWithFeatures);
		
	}

	public void save(String path) throws IOException{
		((CrossValidatorModel) transformer).write().overwrite().save(path);
	}

	public ZFrame<Dataset<Row>,Row,Column> transform(Dataset<Row> input) {
		for (SparkTransformer bsf: featureCreators) {
			input = bsf.transform(input);
		}
		return new SparkFrame(input); //.cache();
	}
	
	public ZFrame<Dataset<Row>,Row,Column> transform(ZFrame<Dataset<Row>,Row,Column> i) {
		return transform(i.df());
	}



	@Override
	public void register(ZSparkSession spark) {
		if (featureCreators != null) {
			for (SparkTransformer bsf: featureCreators) {
				bsf.register(spark);
			}
		}
		vve.register(spark);
		
	}
	
}
