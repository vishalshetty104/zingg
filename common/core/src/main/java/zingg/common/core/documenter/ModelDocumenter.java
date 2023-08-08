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

package zingg.common.core.documenter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;
import zingg.common.core.Context;


public abstract class ModelDocumenter<S,D,R,C,T> extends DocumenterBase<S,D,R,C,T> {

	private static final long serialVersionUID = 1L;
	
	private static final String PAIR_WISE_COUNT = ColName.COL_PREFIX + "pair_wise_count";
	protected static String name = "zingg.ModelDocumenter";
	public static final Log LOG = LogFactory.getLog(ModelDocumenter.class);

	private final String MODEL_TEMPLATE = "model.ftlh";
	protected ModelColDocumenter<S,D,R,C,T> modelColDoc;
	protected  ZFrame<D,R,C>  markedRecords;
	protected  ZFrame<D,R,C>  unmarkedRecords;

	public ModelDocumenter(Context<S,D,R,C,T> context, Arguments args) {
		super(context, args);
		markedRecords = getDSUtil().emptyDataFrame();
	}

	public void process() throws ZinggClientException {
		createModelDocument();
		modelColDoc.process(markedRecords);
	}

	protected void createModelDocument() throws ZinggClientException {
		try {
			LOG.info("Model document generation starts");

			markedRecords = getMarkedRecords();
			unmarkedRecords = getUnmarkedRecords();
			Map<String, Object> root = populateTemplateData();
			writeModelDocument(root);

			LOG.info("Model document generation finishes");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}

	private void writeModelDocument(Map<String, Object> root) throws ZinggClientException {
		checkAndCreateDir(args.getZinggDocDir());
		writeDocument(MODEL_TEMPLATE, root, args.getZinggModelDocFile());
	}

	protected Map<String, Object> populateTemplateData() {
		/* Create a data-model */
		Map<String, Object> root = new HashMap<String, Object>();
		root.put(TemplateFields.MODEL_ID, args.getModelId());
		
		if(!markedRecords.isEmpty()) {
			markedRecords = markedRecords.cache();
			
			root.put(TemplateFields.CLUSTERS, markedRecords.collectAsList());
			root.put(TemplateFields.NUM_COLUMNS, markedRecords.columns().length);
			root.put(TemplateFields.COLUMNS, markedRecords.columns());
			root.put(TemplateFields.ISMATCH_COLUMN_INDEX,
					markedRecords.fieldIndex(ColName.MATCH_FLAG_COL));
			root.put(TemplateFields.CLUSTER_COLUMN_INDEX,
					markedRecords.fieldIndex(ColName.CLUSTER_COLUMN));
			
			putSummaryCounts(root);

		} else {
			// fields required to generate basic document
			List<String> columnList = args.getFieldDefinition().stream().map(fd -> fd.getFieldName())
					.collect(Collectors.toList());
			root.put(TemplateFields.NUM_COLUMNS, columnList.size());
			root.put(TemplateFields.COLUMNS, columnList.toArray());
			root.put(TemplateFields.CLUSTERS, Collections.emptyList());
			root.put(TemplateFields.ISMATCH_COLUMN_INDEX, 0);
			root.put(TemplateFields.CLUSTER_COLUMN_INDEX, 1);
		}
		
		return root;
	}

	private void putSummaryCounts(Map<String, Object> root) {
		// Get the count if not empty
		ZFrame<D,R,C>  markedRecordsPairSummary = markedRecords.groupByCount(ColName.MATCH_FLAG_COL, PAIR_WISE_COUNT);
		List<R> pairCountList = markedRecordsPairSummary.collectAsList();
		long totalPairs = 0;
		long matchPairs = 0;
		long nonMatchPairs = 0;
		long notSurePairs = 0;
		for (Iterator<R> iterator = pairCountList.iterator(); iterator.hasNext();) {
			R r = iterator.next();
			int z_isMatch = markedRecordsPairSummary.getAsInt(r, ColName.MATCH_FLAG_COL);
			long pairWiseCount = markedRecordsPairSummary.getAsLong(r, PAIR_WISE_COUNT);
			if (z_isMatch==ColValues.MATCH_TYPE_MATCH) {
				matchPairs = pairWiseCount/2;
			} else if (z_isMatch==ColValues.MATCH_TYPE_NOT_A_MATCH) {
				nonMatchPairs = pairWiseCount/2;
			} else if (z_isMatch==ColValues.MATCH_TYPE_NOT_SURE) {
				notSurePairs = pairWiseCount/2;
			}
		}
		totalPairs = matchPairs+nonMatchPairs+notSurePairs;
		
		root.put(TemplateFields.TOTAL_PAIRS, totalPairs);
		root.put(TemplateFields.MATCH_PAIRS, matchPairs);
		root.put(TemplateFields.NON_MATCH_PAIRS, nonMatchPairs);
		root.put(TemplateFields.NOT_SURE_PAIRS, notSurePairs);
		
		long markedPairs = markedRecords.count()/2;
		long unmarkedPairs = 0;
		
		if(unmarkedRecords!=null && !unmarkedRecords.isEmpty()) {
			unmarkedPairs = unmarkedRecords.count()/2;
		}
		
		long identifiedPairs = markedPairs+unmarkedPairs;

		root.put(TemplateFields.MARKED_PAIRS, markedPairs);
		root.put(TemplateFields.UNMARKED_PAIRS, unmarkedPairs);
		root.put(TemplateFields.IDENTIFIED_PAIRS, identifiedPairs);
		
		
	}
	
	@Override
	public void execute() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}
}
