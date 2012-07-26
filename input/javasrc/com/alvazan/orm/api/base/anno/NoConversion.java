package com.alvazan.orm.api.base.anno;

import com.alvazan.orm.api.spi3.db.conv.Converter;

public class NoConversion implements Converter {
	@Override
	public byte[] convertToNoSql(Object value) {
		return null;
	}
	@Override
	public Object convertFromNoSql(byte[] value) {
		return null;
	}
	@Override
	public String convertToIndexFormat(Object value) {
		throw new UnsupportedOperationException();
	}
}
