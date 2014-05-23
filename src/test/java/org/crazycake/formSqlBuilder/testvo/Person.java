package org.crazycake.formSqlBuilder.testvo;

public class Person {
	
	private Integer activeStatus;
	private String name;
	private Integer age;
	private String city;
	private String birthdayFrom;
	private String afterThat;
	private String birthday;
	
	public Person(String name, Integer age, String city, Integer activeStatus){
		this.name = name;
		this.age = age;
		this.city = city;
		this.activeStatus = activeStatus;
	}

	public Integer getActiveStatus() {
		return activeStatus;
	}

	public void setActiveStatus(Integer activeStatus) {
		this.activeStatus = activeStatus;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getBirthdayFrom() {
		return birthdayFrom;
	}

	public void setBirthdayFrom(String birthdayFrom) {
		this.birthdayFrom = birthdayFrom;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getAfterThat() {
		return afterThat;
	}

	public void setAfterThat(String afterThat) {
		this.afterThat = afterThat;
	}
	
	
}
