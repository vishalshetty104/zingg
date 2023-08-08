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

package zingg.common.core.util;
import zingg.common.client.Arguments;
import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchType;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;
import zingg.common.core.feature.Feature;
import zingg.common.core.feature.FeatureFactory;
import zingg.common.core.model.Model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class ModelUtil<S,T, D,R,C> {

    public static final Log LOG = LogFactory.getLog(ModelUtil.class);
    protected Map<FieldDefinition, Feature<T>> featurers;
    protected S session;
    
    public abstract FeatureFactory<T> getFeatureFactory();

    public void loadFeatures(Arguments args) throws ZinggClientException {
		try{
		LOG.info("Start reading internal configurations and functions");
        if (args.getFieldDefinition() != null) {
			featurers = new HashMap<FieldDefinition, Feature<T>>();
			for (FieldDefinition def : args.getFieldDefinition()) {
				if (! (def.getMatchType() == null || def.getMatchType().contains(MatchType.DONT_USE))) {
					Feature fea =  (Feature) getFeatureFactory().get(def.getDataType());
					fea.init(def);
					featurers.put(def, fea);			
				}
			}
			LOG.info("Finished reading internal configurations and functions");
			}
		}
		catch(Throwable t) {
			LOG.warn("Unable to initialize internal configurations and functions");
            t.printStackTrace();
			if (LOG.isDebugEnabled()) t.printStackTrace();
			throw new ZinggClientException("Unable to initialize internal configurations and functions");
		}
	}

    public Map<FieldDefinition,Feature<T>> getFeaturers(Arguments args) throws ZinggClientException {
        if (this.featurers == null) loadFeatures(args);
        return this.featurers;
    }

    public void setFeaturers(Map<FieldDefinition,Feature<T>> featurers) {
        this.featurers = featurers;
    }

	public Model<S,T,D,R,C> createModel(ZFrame<D,R,C> positives,
        ZFrame<D,R,C> negatives, boolean isLabel, Arguments args) throws Exception, ZinggClientException {
        LOG.info("Learning similarity rules");
        ZFrame<D,R,C> posLabeledPointsWithLabel = positives.withColumn(ColName.MATCH_FLAG_COL, ColValues.MATCH_TYPE_MATCH);
        posLabeledPointsWithLabel = posLabeledPointsWithLabel.cache();
        posLabeledPointsWithLabel = posLabeledPointsWithLabel.drop(ColName.PREDICTION_COL);
        ZFrame<D,R,C> negLabeledPointsWithLabel = negatives.withColumn(ColName.MATCH_FLAG_COL, ColValues.MATCH_TYPE_NOT_A_MATCH);
        negLabeledPointsWithLabel = negLabeledPointsWithLabel.cache();
        negLabeledPointsWithLabel = negLabeledPointsWithLabel.drop(ColName.PREDICTION_COL);
        if (LOG.isDebugEnabled()) {
            LOG.debug(" +,-,Total labeled data "
                    + posLabeledPointsWithLabel.count() + ", "
                    + negLabeledPointsWithLabel.count());
        }
        Model<S,T, D,R,C> model = getModel(isLabel, args);
        model.register(session);
        model.fit(posLabeledPointsWithLabel, negLabeledPointsWithLabel);
        return model;
    }

    public abstract Model<S,T,D,R,C> getModel(boolean isLabel, Arguments args) throws ZinggClientException;

    public abstract Model<S,T,D,R,C> loadModel(boolean isLabel, Arguments args) throws ZinggClientException;




    
}
