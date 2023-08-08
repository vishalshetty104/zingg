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

package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.core.documenter.DataDocumenter;
import zingg.common.core.documenter.ModelDocumenter;

public abstract class Documenter<S,D,R,C,T> extends ZinggBase<S,D,R,C,T> {

	protected static String name = "zingg.Documenter";
	public static final Log LOG = LogFactory.getLog(Documenter.class);

	public Documenter() {
		setZinggOptions(ZinggOptions.GENERATE_DOCS);
 	}

	public void execute() throws ZinggClientException {
		try {
			LOG.info("Documenter starts");
			//Documentation out of model
			ModelDocumenter modelDoc = getModelDocumenter();
			modelDoc.process();

			//Documentation of data
			DataDocumenter dataDoc = getDataDocumenter();
			dataDoc.process();
			

			LOG.info("Documenter finishes");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}
	
	protected abstract ModelDocumenter<S,D,R,C,T> getModelDocumenter();
	
	protected abstract DataDocumenter<S,D,R,C,T> getDataDocumenter();
	
}
