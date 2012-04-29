package com.alvazan.orm.impl.meta;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.alvazan.orm.api.anno.Embeddable;
import com.alvazan.orm.api.anno.Id;
import com.alvazan.orm.api.anno.ManyToOne;
import com.alvazan.orm.api.anno.NoSqlEntity;
import com.alvazan.orm.api.anno.OneToMany;
import com.alvazan.orm.api.anno.OneToOne;
import com.alvazan.orm.api.anno.Transient;

public class ScannerForClass {

	@Inject
	private ScannerForField inspectorField;
	@Inject
	private MetaInfo metaInfo;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addClass(Class clazz) {
		//NOTE: We scan linearly with NO recursion BUT when we hit an object like Activity.java
		//that has a reference to Account.java and so the MetaClass of Activity has fields and that
		//field needs a reference to the MetaClass of Account.  To solve this, it creates a shell
		//of MetaClass that will be filled in here when Account gets scanned(if it gets scanned
		//after Activity that is).  You can open call heirarchy on findOrCreateMetaClass ;).
		MetaClass<?> classMeta = metaInfo.findOrCreate(clazz);
		classMeta.setMetaClass(clazz);
		scanClass(classMeta);
	}
	
	private void scanClass(MetaClass<?> meta) {
		NoSqlEntity noSqlEntity = meta.getMetaClass().getAnnotation(NoSqlEntity.class);
		Embeddable embeddable = meta.getMetaClass().getAnnotation(Embeddable.class);
		if(noSqlEntity != null) {
			String colFamily = noSqlEntity.columnfamily();
			if("".equals(colFamily))
				colFamily = meta.getMetaClass().getSimpleName()+"s";
			meta.setColumnFamily(colFamily);
		} else if(embeddable != null) {
			//nothing to do at this point
		} else {
			throw new RuntimeException("bug, someone added an annotation but didn't add an else if here");
		}
		
		scanFields(meta);
	}
	private void scanFields(MetaClass<?> meta) {
		Class<?> metaClass = meta.getMetaClass();
		List<Field[]> fields = new ArrayList<Field[]>();
		findFields(metaClass, fields);
		
		for(Field[] fieldArray : fields) {
			for(Field field : fieldArray) {
				inspectField(meta, field);
			}
		}
	}

	private void inspectField(MetaClass<?> metaClass, Field field) {
		if(Modifier.isTransient(field.getModifiers()) || 
				Modifier.isStatic(field.getModifiers()) ||
				field.isAnnotationPresent(Transient.class))
			return;
		
		if(field.isAnnotationPresent(Id.class)) {
			MetaIdField idField = inspectorField.processId(field);
			metaClass.setIdField(idField);
			return;
		}
		
		MetaField metaField;
		if(field.isAnnotationPresent(ManyToOne.class))
			metaField = inspectorField.processManyToOne(field);
		else if(field.isAnnotationPresent(OneToOne.class))
			metaField = inspectorField.processOneToOne(field);
		else if(field.isAnnotationPresent(OneToMany.class))
			metaField = inspectorField.processOneToMany(field);
		else if(field.isAnnotationPresent(Embeddable.class))
			metaField = inspectorField.processEmbeddable(field);
		else
			metaField = inspectorField.processColumn(field);
		
		metaClass.addMetaField(metaField);
	}
	
	@SuppressWarnings("rawtypes")
	private void findFields(Class metaClass2, List<Field[]> fields) {
		Class next = metaClass2;
		while(true) {
			Field[] f = next.getDeclaredFields();
			fields.add(f);
			next = next.getSuperclass();
			if(next.equals(Object.class))
				return;
		}
	}

}