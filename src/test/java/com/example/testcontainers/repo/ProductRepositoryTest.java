package com.example.testcontainers.repo;

import com.example.testcontainers.common.AbstractTestContainersIntegrationTest;
import com.example.testcontainers.domain.Product;
import com.example.testcontainers.domain.ProductCategory;
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
class ProductRepositoryTest extends AbstractTestContainersIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @Sql("/sql/products.sql")
    public void findByCategory_whenInvoked_expectedResult() {
        List<Product> result = productRepository.findByCategory(ProductCategory.PHONES);

        assertThat(result).hasSize(2)
                .extracting(Product::getCode)
                .containsExactly("product1", "product2");
    }

    @Test
    @Sql("/sql/products.sql")
    public void findByGlobalSearch_whenEmptyString_returnsAllRecords() {
        List<Product> result = productRepository.findByGlobalSearch("");

        assertThat(result).hasSize(3);
    }

    @Test
    @Sql("/sql/products.sql")
    public void findByGlobalSearch_whenInvoked_filtersCorrectly() {
        List<Product> result = productRepository.findByGlobalSearch("COM");

        assertThat(result).hasSize(1)
                .extracting(Product::getCode)
                .containsExactly("product3");
    }

    @Test
    @Sql("/sql/products.sql")
    public void setOutOfStockByIds_whenInvoked_updatesCorrectData() {
        productRepository.setOutOfStockByIds(List.of(1L, 2L));

        assertThat(productRepository.findByInStockTrue()).hasSize(1)
                .extracting(Product::getCode)
                .containsExactly("product3");
    }

    @Test
    @Sql({"/sql/customers.sql", "/sql/products.sql", "/sql/customer_favorite_products.sql"})
    public void findFavoriteByCustomerId_whenInvoked_expectedResult() {
        List<Product> result = productRepository.findFavoriteByCustomerId(1L);

        assertThat(result).hasSize(2)
                .extracting(Product::getId)
                .containsExactly(1L, 3L);
    }
}
