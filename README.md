form-sql-builder-mysql
============

[![Build Status](https://travis-ci.org/alexxiyang/form-sql-builder-mysql.svg?branch=master)](https://travis-ci.org/alexxiyang/form-sql-builder-mysql)

As a programmer did you have the experience that when you comes to implement a search page you can only transform the search form into sql manually? If your project have 20 search page like that then you have to write 20 times to implement the sql query. It's boring and anxious. 
`form-sql-builder-mysql` is designed for ease the burden for you. It can help you to transform search or list form sent from browser into query sql automatically.
> Only use for Mysql!


Maven dependency
-------------
```xml
<dependency>
  <groupId>org.crazycake</groupId>
  <artifactId>form-sql-builder-mysql</artifactId>
  <version>1.0.0-RELEASE</version>
</dependency>
```

#Quick start

 
####STEP 1. create form to sql rules
Create a folder under your classpath named `formSqlRules`. Put a json file named `global.json` in this folder. When your application start `FormSqlBuilder` will auto load all json files in `formSqlRules`  and store them in cache. 
> NOTE
> The json file name is no relate with the content. You can assign whatever you like to the json file and whatever you like to the rule key. In our example the rule key is global.

```json
{
	"global":[
		{
			"field":"String:*",
			"op":"like",
			"rel":"and"
		},{
			"field":"*:*",
			"op":"=",
			"rel":"and"
		}
	]
}
```

This rule means
- If `FormSqlBuilder` means any of String type field, it will use `LIKE` operation symbol and join this field with `AND`
- If it means any of type except String , it will use `=` as the operation symbol.


> You'd better use 1.6+ jdk. Cause I didn't test it on 1.5

####STEP 2. Create a PO for test
Create a PO class named `Person` 

```java
public class Person {
	
	private Integer activeStatus;
	private String name;
	private Integer age;
	private String city;
	
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
}
```

####STEP 3. Call FormSqlBuilder
Here is the code how we use `FormSqlBuilder`

```java
Person form = new Person("jack", 36, "ny", 1);
FormSqlBuilder b = new FormSqlBuilder(form, "global");
b.addLimit(1, 20);
SqlAndParams s = b.build();
System.out.println(s.getSql());
for(Object v:s.getParams()){
	System.out.println(v);
}
```
Console will print
```
SELECT * FROM person WHERE name like ? AND city like ? AND active_status = ? AND age = ?  LIMIT 0,20
jack
ny
1
36
```

Then you can use this sql and values to query database.


#Further more
I will introduce more features I mentioned in introduction. 

##Match rules
Here are points of match rule writing
- The priority of above rule is higher then the below rule. 
- `FormSqlBuilder` support wildcard match.
- When you use wildcard match expression you can assign `wildcardTargetField` value to let it use the wildcard matched part as the column name in SQL.
- field should be wrote like `Class type`:`Match expression`, like `String:name`, `Boolean:married`, `String:*From`, `String:After*`, `*:*`

###Rule fields
|field|required|default|description|
|---|---|------|-----|
|field|required||field match expression|
|op|required||operator|
|rel|required||relation|
|targetField|optional|field|Assign another field as sql column. You can only choose to set one of `wildcardTargetField` or `targetField`|
|wildcardTargetField|optional|false|whether to use the wildcard matched part as target field name|
|members|optional||a group of rules|

###Wildcard match
```json
{
    "field":"String:*From",
	"op":">",
	"wildcardTargetField":true,
	"rel":"and"
}
```
In this example, it will match `birthdayFrom` but it will generate SQL like `AND birthday > ?`

###All support operators

| op	|
|-------|
|`=`		|
|`<`		|
|`>`		|
|`<>`	|
|`like`	|
|`not like`|
|`in`	|
|`not in`|
|`<=`	|
|`>=`	|

###All support relations

|rel	|
|-------|
|`and`	|
|`or`	|


##How to deal with range search
At search page we may mean the date range search. Here is how `FormSqlBuilder` deal with this situation.
I will use an example to show you how to generate date range search sql.
###Example
Imagine that you want to search all people who born between 1980-1-1 to 1981-1-1. So there will be 2 date picker on search page which are "birthday from date" and "birthday to date".  After you choose "birthday from date" to "1980-1-1" and "birthday to date" to "1981-1-1", click search button. `FormSqlBuilder` will change your search query condition into `WHERE birthday > ? and birthday < ?` . Let's see how to make this happen.
####STEP 1 Add @Transient fields
Add 2 transient fields `birthdayFrom` and `birthdayTo` to `Person.java`. like this
```java
package org.crazycake.formSqlBuilder.testvo;
import javax.persistence.Transient;

public class Person {

	private String name;
	private String birthday;
	private String birthdayFrom;
	private String birthdayTo;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@Transient
	public String getBirthdayFrom() {
		return birthdayFrom;
	}
	public void setBirthdayFrom(String birthdayFrom) {
		this.birthdayFrom = birthdayFrom;
	}
	@Transient
	public String getBirthdayTo() {
		return birthdayTo;
	}
	public void setBirthdayTo(String birthdayTo) {
		this.birthdayTo = birthdayTo;
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	
}
```

Remember to put the value of "birthday from date" to `birthdayFrom` and "birthday to date" to `birthdayTo`.

####STEP 4 Add rules
Change the form to sql rules we used before, add 2 new rules
```json
{
	"global":[
		{
			"field":"String:*From",
			"op":">",
			"wildcardTargetField":true,
			"rel":"and"
		},{
			"field":"String:*To",
			"op":"<",
			"wildcardTargetField":true,
			"rel":"and"
		},{
			"field":"String:*",
			"op":"like",
			"rel":"and"
		},{
			"field":"*:*",
			"op":"=",
			"rel":"and"
		}
	]
}
```
`wildcardTargetField` means use the part of wildcard as column name. In this example , if we turn `wildcardTargetField` to `true`. The Sql will be `birthday < ?` , if we turn `wildcardTargetField` to `false` to don't set this property which `FormSqlBuilder` will use its default value `false`, the sql will be `birthday_to < ?`.

####STEP 5 Call FormSqlBuilder
After everything is done. Let's call `FormSqlBuilder` to generate the sql and parameters.
```java
@Test
public void testBuild() throws Exception {
	Person form = new Person();
	form.setBirthdayFrom("1980-01-01");
	form.setBirthdayTo("1981-01-01");
	FormSqlBuilder b = new FormSqlBuilder(form, "global2");
	SqlAndParams s = b.build();
	System.out.println(s.getSql());
	System.out.println(s.getParams()[0]);
	System.out.println(s.getParams()[1]);
}
```

Console output
```
SELECT * FROM person WHERE birthday > ? AND birthday < ? 
1980-01-01
1981-01-01
```

##What if comes to group search
When you means requirement like : the search conditions on page are need to separate into 2 groups and join with `AND`.
For example you want to search for people whose name is jack OR whose age is 23 , but these people `activeStatus` must be `true`  means they still working in our company not leaved. That's a common situation.

Here is the rule json
```json
{
	"groupSearch":[
		{
			"field":"Integer:activeStatus",
			"op":"=",
			"rel":"and"
		},{				
			"rel":"and",
			"members":[
				{
					"field":"String:name",
					"op":"like",
					"rel":"or"
				},
				{
					"field":"Integer:age",
					"op":"=",
					"rel":"or"
				}
			]
		}
	]
}
``` 
You can use `members` field to define more rules under this rule.

