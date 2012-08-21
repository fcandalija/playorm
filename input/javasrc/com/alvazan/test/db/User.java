package com.alvazan.test.db;

import com.alvazan.orm.api.base.NoSqlEntityManager;
import com.alvazan.orm.api.base.Query;
import com.alvazan.orm.api.base.anno.NoSqlEntity;
import com.alvazan.orm.api.base.anno.NoSqlId;
import com.alvazan.orm.api.base.anno.NoSqlIndexed;
import com.alvazan.orm.api.base.anno.NoSqlQuery;

@NoSqlEntity
@NoSqlQuery(name="findByName", query="select u from TABLE as u where :name = u.name")
public class User {

	@NoSqlId
	private String id;
	
	@NoSqlIndexed
	private String name;

	private String lastName;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public static User findByName(NoSqlEntityManager mgr, String name) {
		Query<User> query = mgr.createNamedQuery(User.class, "findByName");
		query.setParameter("name", name);
		return query.getSingleObject();
	}
	
}
