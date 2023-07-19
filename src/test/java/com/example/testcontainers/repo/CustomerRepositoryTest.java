package com.example.testcontainers.repo;

import com.example.testcontainers.common.AbstractTestContainersIntegrationTest;
import com.example.testcontainers.domain.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class CustomerRepositoryTest extends AbstractTestContainersIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @Sql("/sql/customers.sql")
    public void findAllActive_whenInvoked_expectedResult() {
        List<Customer> result = customerRepository.findAllActive();

        assertThat(result).hasSize(2)
                .extracting(Customer::getUsername)
                .containsExactly("user1", "user2");
    }

}
