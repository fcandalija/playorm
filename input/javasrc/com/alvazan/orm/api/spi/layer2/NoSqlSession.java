package com.alvazan.orm.api.spi.layer2;

import java.util.List;
import java.util.Map;

import com.alvazan.orm.api.spi.db.Column;
import com.alvazan.orm.api.spi.db.NoSqlRawSession;
import com.alvazan.orm.api.spi.db.Row;
import com.alvazan.orm.api.spi.index.IndexReaderWriter;
import com.alvazan.orm.layer2.nosql.cache.NoSqlWriteCacheImpl;
import com.google.inject.ImplementedBy;

@ImplementedBy(NoSqlWriteCacheImpl.class)
public interface NoSqlSession {

	/**
	 * Retrieves the rawest interface that all the providers implement(in-memory, cassandra, hadoop, etc) BUT
	 * be warned, when you call persist, it is executed IMMEDIATELY, there is no flush method on that
	 * interface.  In fact, the flush methods of all higher level interfaces call persist ONCE on the raw
	 * session and providers are supposed to do ONE send to avoid network latency if you are sending 100
	 * actions to the nosql database.
	 * @return
	 */
	public NoSqlRawSession getRawSession();
	
	/**
	 * Retrieves the rawest interfaces that all the indexing providers implement(in-memory, solr, etc) BUT
	 * be warned as any methods you called are executed immediately.  there is NO flush method, so you may
	 * want to call flush to persit your entities BEFORE adding to the index OR remove from the index BEFORE
	 * calling flush on your removal of entities from the database.
	 * 
	 * @return
	 */
	public IndexReaderWriter getRawIndex();
	
	public void persist(String colFamily, byte[] rowKey, List<Column> columns);

	/**
	 * Remove entire row.
	 * 
	 * @param colFamily
	 * @param rowKey
	 */
	public void remove(String colFamily, byte[] rowKey);
	
	/**
	 * Remove specific columns from a row
	 * 
	 * @param colFamily
	 * @param rowKey
	 * @param columns
	 */
	public void remove(String colFamily, byte[] rowKey, List<String> columnNames);
	
	public List<Row> find(String colFamily, List<byte[]> rowKeys);
	
	public void removeFromIndex(String indexName, String id);
	public void addToIndex(String indexName, Map<String, String> item);
	
	public void flush();

}