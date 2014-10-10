form-sql-builder-mysql
============

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

##How to deal with range search
At search page we may mean the date range search. Here is how `FormSqlBuilder` deal with this situation
