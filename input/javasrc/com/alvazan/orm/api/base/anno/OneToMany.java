package com.alvazan.orm.api.base.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToMany {
    @SuppressWarnings("rawtypes")
	Class entityType();

	String columnName() default "";

	/**
	 * When using Map instead of a List, the field in entityType() needs to 
	 * be specified here...
	 * @return
	 */
	String keyFieldForMap() default "";
	
	/**
	 * With OneToMany, List or Map, no entities are immediate fetched from the
	 * database.  As you loop through your list and do child.getId() the database
	 * is still not hit, BUT the first time as you loop through the children, you
	 * call child.get<ANY OTHER METHOD>(), there are two methods of loading from
	 * the database. FetchType.ONE_AT_A_TIME will ensure it fetches just that
	 * entity while FetchType.ALL_ONCE_HOT fetchs all entities as it assumes you
	 * are going to keep goin through the loop and calling child.getSomething() which
	 * would load each entity one at a time(which is a bad idea for performance if
	 * you have a lot of entities).
	 * 
	 * @return
	 */
	FetchType fetchType() default FetchType.ALL_ONCE_HIT;

}
