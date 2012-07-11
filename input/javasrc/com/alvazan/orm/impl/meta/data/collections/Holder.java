package com.alvazan.orm.impl.meta.data.collections;

import com.alvazan.orm.api.spi.layer2.NoSqlSession;
import com.alvazan.orm.impl.meta.data.MetaClass;

/**
 * A class specifically so we can account for nulls.  ie. if we don't use holders and we get
 * element[8] and it returns null, we don't know if it cached null or if we need to reprocess.  If we
 * use Holder, element[8] returning null means we have not cached it yet, otherwise it can return a
 * Holder with a null value inside it meaning we cached the null value.
 * @author dhiller
 *
 * @param <T>
 */
public class Holder<T> {

	private byte[] key;
	private boolean hasValue;
	private T value;
	private MetaClass<T> metaClass;
	private NoSqlSession session;

	public Holder(MetaClass<T> metaClass, NoSqlSession session, byte[] key) {
		this.metaClass = metaClass;
		this.session = session;
		this.key = key;
	}
	public Holder(T value) {
		hasValue = true;
		this.value = value;
	}
	public synchronized T getValue() {
		if(!hasValue) {
			//otherwise, we need to create and cache the value
			T proxy = metaClass.convertIdToProxy(key, session);
			value = proxy;
			hasValue = true;
		}
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	public byte[] getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		getValue(); //prime the cache before comparing!!!
		
		final int prime = 31;
		int result = 1;
		int val = 0;
		if(value != null)
			val = value.hashCode();
		result = prime * result + val;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		getValue(); //prime the cache before comparing to make sure real value is there
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Holder other = (Holder) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
}