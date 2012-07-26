package com.alvazan.orm.layer3.spi.index.inmemory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alvazan.orm.api.spi3.index.IndexAdd;
import com.alvazan.orm.api.spi3.index.IndexReaderWriter;
import com.alvazan.orm.api.spi3.index.IndexRemove;
import com.alvazan.orm.api.spi3.index.SpiMetaQuery;
import com.alvazan.orm.api.spi3.index.exc.IndexAddFailedException;
import com.alvazan.orm.api.spi3.index.exc.IndexDeleteFailedException;
import com.alvazan.orm.api.spi3.index.exc.IndexErrorInfo;

public class MemoryIndexWriter implements IndexReaderWriter {
	private static final Logger log = LoggerFactory.getLogger(MemoryIndexWriter.class);
	@Inject
	private Indice indice;
	@Inject
	private Provider<SpiMetaQueryImpl> factory;
	
	@Override
	public void sendRemoves(Map<String, List<? extends IndexRemove>> removeFromIndex) {
		List<IndexErrorInfo> exceptions = new ArrayList<IndexErrorInfo>();
		List<? extends IndexRemove> removes = null;
		for(Entry<String, List<? extends IndexRemove>> entry : removeFromIndex.entrySet()) {
			String key = entry.getKey();
			removes = entry.getValue();
			try {
				removeAllFromIndex(key, removes);
			} catch (Exception e) {
				exceptions.add(createRemoves(removes, e));
			}			
		}
		
		if(exceptions.size() > 0) 
			throw new IndexDeleteFailedException(exceptions);
	}

	private IndexErrorInfo createRemoves(List<? extends IndexRemove> removes,
			Exception e) {
		List<String> ids = new ArrayList<String>();
		for(IndexRemove a : removes) {
			ids.add(a.getId());
		}
		IndexErrorInfo info = new IndexErrorInfo();
		info.setCause(e);
		info.setIdsThatFailed(ids);
		return info;
	}

	private void removeAllFromIndex(String indexName,
			List<? extends IndexRemove> removes) throws ParseException, IOException {
		IndexItems index = indice.findOrCreate(indexName);
		
		String queryStr = createQuery(removes);
		Analyzer analyzer = new KeywordAnalyzer();		
		Query query = new QueryParser(Version.LUCENE_35, "title", analyzer).parse(queryStr);		
		
		synchronized(index) {
			IndexWriter writer = null;
			boolean success = false;
			try {
				IndexWriterConfig config  = new IndexWriterConfig(Version.LUCENE_36, analyzer );
				writer = new IndexWriter(index.getRamDirectory(), config);
				
				writer.deleteDocuments(query);

			    success = true;
			} finally {
				silentClose(writer, success);
			}
		}
	}

	private String createQuery(List<? extends IndexRemove> removes) {
		IndexRemove first = removes.remove(0);
		String query = IDKEY+":"+first.getId();
		for(IndexRemove rem : removes) {
			query += " OR "+IDKEY+":"+rem.getId();
		}
		return query;
	}

	@Override
	public void sendAdds(Map<String, List<IndexAdd>> addToIndex) {
		List<IndexErrorInfo> exceptions = new ArrayList<IndexErrorInfo>();
		List<IndexAdd> adds = null;
		for(Entry<String, List<IndexAdd>> entry : addToIndex.entrySet()) {
			try {
				String key = entry.getKey();
				adds = entry.getValue();
				addToIndex(key, adds);
			} catch (Exception e) {
				log.error("creating index error ",e);
				exceptions.add(create(adds, e));
			}
		}
		
		if(exceptions.size() > 0)
			throw new IndexAddFailedException(exceptions);
	}

	private IndexErrorInfo create(List<IndexAdd> adds, Exception e) {
		List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
		for(IndexAdd a : adds) {
			items.add(a.getItem());
		}
		IndexErrorInfo info = new IndexErrorInfo();
		info.setCause(e);
		info.setItemsThatFailed(items);
		return info;
	}

	private void addToIndex(String indexName, List<IndexAdd> adds) throws IOException {
		IndexItems index = indice.findOrCreate(indexName);
		synchronized(index) {
			boolean success = false;
			IndexWriter writer = null;
			try {
				Analyzer analyzer = new KeywordAnalyzer();
				IndexWriterConfig config  = new IndexWriterConfig(Version.LUCENE_36, analyzer );
				writer = new IndexWriter(index.getRamDirectory(), config  );
		
				for(IndexAdd add : adds) {
					Document doc = createDocument(add.getId(), add.getItem());
					writer.addDocument(doc);
				}
				success = true;
			} finally {
				silentClose(writer, success);
			}
		}
	}

	private void silentClose(IndexWriter writer, boolean success) {
		if(writer == null)
			return;
		
		try {
			writer.close();
		} catch (Exception e) {
			if(success)
				throw new RuntimeException(e);
			//if our finally is being run after an exception, we want writer.close to silently fail and debug
			//the first exception
		}
	}

	private static Document createDocument(String idValue, Map<String, Object> map) {
        Document doc = new Document();
        doc.add(new Field(IDKEY, idValue, Field.Store.YES, Field.Index.ANALYZED));
        
        for (Entry<String, Object> item : map.entrySet()) {
        	String key = item.getKey();
        	Object value = item.getValue();
        	if(value==null) {
        		doc.add(new Field(key, "__impossible__value__", Field.Store.NO,
						Field.Index.ANALYZED));
        		continue;
        	}
			// FIXME stupid if else if

			if (value instanceof String){
				doc.add(new Field(key, (String) value, Field.Store.NO,
						Field.Index.ANALYZED));
			}
			else if (value instanceof Number) {
				NumericField numericField = new NumericField(key,
						Field.Store.NO, true);
				if (value.getClass() == Integer.class) {
					numericField.setIntValue((Integer) value);
				} else if ( value.getClass() == Long.class) {
					numericField.setLongValue((Long) value);
				} else if (value.getClass() == Double.class) {
					numericField.setDoubleValue((Double) value);
				} else if (value.getClass() == Float.class) {
					numericField.setFloatValue((Float) value);
				}
				doc.add(numericField);
			} 
			else if (value instanceof Boolean){
				doc.add(new Field(key, value+"", Field.Store.NO,
						Field.Index.ANALYZED));
			}
			else {
				throw new RuntimeException("Unsupport field " + key + " type "
						+ value.getClass());
			}
        
        		
		}

        return doc;
    }

	@Override
	public SpiMetaQuery createQueryFactory() {
		return factory.get();
	}

	@Override
	public void clearIndexesIfInMemoryType() {
		indice.clear();
	}
}
