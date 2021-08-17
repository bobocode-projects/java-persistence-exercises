# <img src="https://raw.githubusercontent.com/bobocode-projects/resources/master/image/logo_transparent_background.png" height=50/>Employee profile exercise :muscle:
Improve your *JPA One-To-One* relationship mapping skills
### Task
`Employee.java` and `EmployeeProfile.java` are related JPA entities. The relationship between those two entities is **one
to one**. Each employee can have only one profile, while each profiles is always associated with one employee. **Profile
cannot exist separately without an employee, while employee can exist with no profile associated.**

Your job is to **implement unidirectional mapping** between those entities using **derived identifier** (a shared primary 
key for both entities).  

To verify your implementation, run `EmployeeProfileMappingTest.java`

 
### Pre-conditions :heavy_exclamation_mark:
You're supposed to be familiar with *JPA* mapping strategies and *Hibernate ORM*

### How to start :question:
* Just clone the repository and start implementing the **todo** section, verify your changes by running tests
* If you don't have enough knowledge about this domain, check out the [links below](#related-materials-information_source)
* Don't worry if you got stuck, checkout the **exercise/completed** branch and see the final implementation
 
### Related materials :information_source:
 * [JPA and Hibernate basics tutorial](https://github.com/boy4uck/jpa-hibernate-tutorial/tree/master/jpa-hibernate-basics) <img src="https://raw.githubusercontent.com/bobocode-projects/resources/master/image/logo_transparent_background.png" height=20/>
 * [Derived identifiers](http://docs.jboss.org/hibernate/orm/5.3/userguide/html_single/Hibernate_User_Guide.html#identifiers-derived)<img src="https://upload.wikimedia.org/wikipedia/commons/2/22/Hibernate_logo_a.png" height=15/>
 * [The best way to map a @OneToOne relationship with JPA and Hibernate](https://vladmihalcea.com/the-best-way-to-map-a-onetoone-relationship-with-jpa-and-hibernate/)
 

