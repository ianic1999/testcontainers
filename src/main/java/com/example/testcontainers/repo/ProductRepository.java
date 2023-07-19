package com.example.testcontainers.repo;

import com.example.testcontainers.domain.Product;
import com.example.testcontainers.domain.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
