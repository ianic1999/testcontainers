# Testing Data Layers with Testcontainers in Spring Boot

In this project, we will explore how to test JPA repositories in a Spring Boot project using Testcontainers. Testcontainers is a Java library that allows you to use lightweight, throwaway containers for your integration tests, ensuring that your tests are isolated and reliable.

We will implement a project for managing favorite products for different customers. Pretty simple.

## Prerequisites

Before proceeding, make sure you have the following installed:

- Java JDK (8 or later)
- Spring Boot
- Testcontainers
- Docker

## 1. Set Up Dependencies

In your Spring Boot project, you need to add the necessary dependencies for Testcontainers and the database you want to test against. For this documentation, we'll use PostgreSQL as an example.

### Maven Dependencies

```xml
<!-- Add Testcontainers dependencies -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>LATEST_VERSION</version>
    <scope>test</scope>
</dependency>

<!-- Add the database driver for Testcontainers -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>LATEST_VERSION</version>
    <scope>test</scope>
</dependency>

<!-- Add the database driver for your actual project (outside of test scope) -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>LATEST_VERSION</version>
</dependency>
```

## 2. Create Domain and Repositories

We have 2 entities:

```java
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(unique = true)
    private String username;

    private String firstName;

    private String lastName;

    private boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "customer_favorite_product",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    private List<Product> favoriteProducts = new ArrayList<>();
}
```

```java
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(unique = true)
    private String code;

    private String name;

    private Double price;

    private boolean inStock = true;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @ManyToMany(mappedBy = "favoriteProducts")
    private List<Customer> customers = new ArrayList<>();
}
```

Now, let's create JPA repositories for our entities, providing different custom queries:

```java
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("select c from Customer c where c.active = true")
    List<Customer> findAllActive();
}
```

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByInStockTrue();

    @Query(nativeQuery = true,
            value = "select p.* from product p " +
                    "where upper(p.code) like %:search% " +
                    "   or upper(p.name) like %:search% " +
                    "   or upper(p.category) like %:search%")
    List<Product> findByGlobalSearch(@Param("search") String search);

    @Query("select p from Product p " +
            "join p.customers c " +
            "where c.id = :customerId")
    List<Product> findFavoriteByCustomerId(@Param("customerId") Long customerId);

    List<Product> findByCategory(ProductCategory category);

    @Modifying
    @Query("update Product p set p.inStock = false where p.id in (:ids)")
    void setOutOfStockByIds(@Param("ids") List<Long> ids);
}
```

## 3. Configuring Testcontainers for PostgreSQL

We need to create a abstract class that will prepare temporary database for our tests using **Testcotainers**.

```java
public class AbstractTestContainersIntegrationTest {

    // 1
    @Container
    static final PostgreSQLContainer<?> postgres = (PostgreSQLContainer<?>) new PostgreSQLContainer(DockerImageName.parse("postgres:13.3"))
            .withDatabaseName("test")
            .withUsername("postgres")
            .withPassword("password");

    // 2
    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    // 3
    @BeforeAll
    public static void setUp() {
        postgres.start();
    }

}
```

Our class performs the following configuration:
1. Creates de PostgreSQL container
2. Configure spring datasource properties so that it connects to the created container instead of default database connection
3. Starts the container before all tests are run

## 4. Writing integration tests for data layers

Now you can write your JPA repository tests as you would normally do. Testcontainers will automatically set up a PostgreSQL container for your tests and provide the necessary DataSource.

In our app, we create schema for our tests using *schema.sql* file in the classpath. It will create all the necessary tables.

Also, we need to provide data for out tests. It can be done using *@Sql* annotation that will execute provided scripts before test run.

A typical test case looks like this:

```java
    @Test
    @Sql({"/sql/customers.sql", "/sql/products.sql", "/sql/customer_favorite_products.sql"})
    public void findFavoriteByCustomerId_whenInvoked_expectedResult() {
        List<Product> result = productRepository.findFavoriteByCustomerId(1L);

        assertThat(result).hasSize(2)
                .extracting(Product::getId)
                .containsExactly(1L, 3L);
    }
```

## 5. Run the Tests

Now you can run your test. The tests will start the PostgreSQL container, execute the tests against it, and then shut it down after the tests finish.

That's it! You now have a setup to test your JPA repositories using Testcontainers in your Spring Boot project with PostgreSQL. This approach can be extended to test other databases or even multiple containers for more complex scenarios.

## Conclusion

In conclusion, using Testcontainers to test JPA repositories in a Spring Boot project provides several benefits. By leveraging lightweight, disposable containers, we ensure that our integration tests are isolated and independent from the development environment, leading to more reliable and consistent test results. The use of Testcontainers allows us to simulate real database environments, such as PostgreSQL, without the need to set up and manage dedicated test databases.

Additionally, Testcontainers simplifies the testing setup by automatically handling container lifecycle management. It spins up the required containers before running the tests and tears them down after the tests complete, ensuring a clean environment for each test execution.
