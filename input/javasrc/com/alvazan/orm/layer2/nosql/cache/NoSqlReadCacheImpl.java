package com.alvazan.orm.layer2.nosql.cache;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.alvazan.orm.layer2.nosql.NoSqlSession;
import com.alvazan.orm.layer2.nosql.Row;
import com.alvazan.orm.layer3.spi.Column;
import com.alvazan.orm.layer3.spi.NoSqlRawSession;

public class NoSqlReadCacheImpl implements NoSqlSession {

	@Inject @Named("writecachelayer")
	private NoSqlSession session;
	
	@Override
	public void persist(String colFamily, byte[] rowKey, List<Column> columns) {
		session.persist(colFamily, rowKey, columns);
	}

	@Override
	public void remove(String colFamily, byte[] rowKey) {
		session.remove(colFamily, rowKey);
	}

	@Override
	public void remove(String colFamily, byte[] rowKey, List<String> columnNames) {
		session.remove(colFamily, rowKey, columnNames);
	}

	@Override
	public List<Row> find(String colFamily, List<byte[]> rowKeys) {
		//READ FROM CACHE HERE to skip reading from database.
		//All Proxies read from this find method too so they can get cache hits when you have a large 
		//object graph and fill themselves in properly
		List<Row> rows = session.find(colFamily, rowKeys);
		//We can cache stuff here....
		cacheRows(rows);
		return rows;
	}
	
	private void cacheRows(List<Row> rows) {
	}

	@Override
	public void flush() {
		session.flush();
	}

	@Override
	public NoSqlRawSession getRawSession() {
		return session.getRawSession();
	}

}