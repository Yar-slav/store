package com.gridu.store.service;

import com.gridu.store.dto.request.ProductRequestDto;
import com.gridu.store.dto.response.ProductResponseDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    List<ProductResponseDto> getAll(Pageable pageable);

    ProductResponseDto addProduct(ProductRequestDto requestDtom, String token);

}
