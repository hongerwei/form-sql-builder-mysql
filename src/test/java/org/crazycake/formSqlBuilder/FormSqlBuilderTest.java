package org.crazycake.formSqlBuilder;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.crazycake.formSqlBuilder.exception.FormIsNullException;
import org.crazycake.formSqlBuilder.model.Sort;
import org.crazycake.formSqlBuilder.model.SqlAndParams;
import org.crazycake.formSqlBuilder.testvo.Person;

import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

public class FormSqlBuilderTest {

	@Test
	public void testBuildCount() throws IllegalArgumentException, SecurityException, FormIsNullException, SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		Person form = new Person("michael", 33, "miami", 0);
		FormSqlBuilder b = new FormSqlBuilder(form, "global2");
		SqlAndParams s = b.buildCount();
		assertThat(s.getSql(),is("SELECT count(1) FROM person WHERE name like ? AND city like ? AND active_status = ? AND age = ? "));
		assertThat((Integer)s.getParams()[2],is(0));
	}

	@Test
	public void testBuild() throws IllegalArgumentException, SecurityException, FormIsNullException, SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		Person form = new Person("jack", 36, "ny", 1);
		FormSqlBuilder b = new FormSqlBuilder(form, "global2");
		b.addLimit(1, 20);
		b.addSort(new Sort("activeStatus", "asc"));
		SqlAndParams s = b.build();
		assertThat(s.getSql(),is("SELECT * FROM person WHERE name like ? AND city like ? AND active_status = ? AND age = ?  ORDER BY active_status asc LIMIT 0,20"));
		assertThat((Integer)s.getParams()[2],is(1));
	}
	
	@Test
	public void testBuildWithWildcard() throws IllegalArgumentException, SecurityException, FormIsNullException, SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		Person form = new Person("jack", 36, "ny", 1);
		form.setBirthdayFrom("1980-01-01");
		FormSqlBuilder b = new FormSqlBuilder(form, "global2");
		b.addLimit(1, 20);
		SqlAndParams s = b.build();
		assertThat(s.getSql(),is("SELECT * FROM person WHERE birthday > ? AND name like ? AND city like ? AND active_status = ? AND age = ?  LIMIT 0,20"));
		assertThat((String)s.getParams()[0],is("1980-01-01"));
	}
	
	@Test
	public void testBuildWithIn() throws IllegalArgumentException, SecurityException, FormIsNullException, SQLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		Person form = new Person("jack", 36, "ny", 1);
		form.setSelectedRoles("user,admin,developer");
		FormSqlBuilder b = new FormSqlBuilder(form, "global2");
		SqlAndParams s = b.build();
		assertThat(s.getSql(),is("SELECT * FROM person WHERE roles in (?,?,?) AND name like ? AND city like ? AND active_status = ? AND age = ? "));
		assertThat((String)s.getParams()[0],is("user"));
		assertThat((String)s.getParams()[1],is("admin"));
		assertThat((String)s.getParams()[2],is("developer"));
		assertThat((String)s.getParams()[3],is("jack"));
	}
	
	@Test
	public void testBuildWithNotIn() throws Exception {
		Person form = new Person("jack", 36, "ny", 1);
		form.setNotSelectedRoles("user,admin,developer");
		FormSqlBuilder b = new FormSqlBuilder(form, "global2");
		SqlAndParams s = b.build();
		assertThat(s.getSql(),is("SELECT * FROM person WHERE roles not in (?,?,?) AND name like ? AND city like ? AND active_status = ? AND age = ? "));
		assertThat((String)s.getParams()[0],is("user"));
		assertThat((String)s.getParams()[1],is("admin"));
		assertThat((String)s.getParams()[2],is("developer"));
		assertThat((String)s.getParams()[3],is("jack"));
	}

}
