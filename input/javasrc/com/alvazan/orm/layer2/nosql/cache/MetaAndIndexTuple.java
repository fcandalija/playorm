package com.alvazan.orm.layer2.nosql.cache;

import com.alvazan.orm.api.spi.layer2.MetaQuery;

public class MetaAndIndexTuple {

	private MetaQuery metaQuery;
	private String indexName;
	public MetaQuery getMetaQuery() {
		return metaQuery;
	}
	public void setMetaQuery(MetaQuery metaQuery) {
		this.metaQuery = metaQuery;
	}
	public String getIndexName() {
		return indexName;
	}
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
	
	
}