package com.gridu.store.repository;

import com.gridu.store.model.ProductEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepo extends CrudRepository<ProductEntity, Long> {

    Page<ProductEntity> findAll(Pageable pageable);
    Optional<ProductEntity> findById(Long id);
    ProductEntity findByTitleAndPrice(String title, double price);
}
