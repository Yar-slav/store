package com.gridu.store.factory.model;

import com.gridu.store.model.ProductEntity;
import java.util.List;

public class ProductEntityFactory {

    public static List<ProductEntity> createProductsEntity() {
        return List.of(
                new ProductEntity(1L, "phone", 10L, 2000, null),
                new ProductEntity(2L, "phone2", 10L, 3000, null),
                new ProductEntity(3L, "phone3", 10L, 4000, null)
        );
    }
}
