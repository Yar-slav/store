package com.gridu.store.service.implementation;

import com.gridu.store.dto.request.ProductRequestDto;
import com.gridu.store.dto.response.ProductResponseDto;
import com.gridu.store.exception.ApiException;
import com.gridu.store.exception.Exceptions;
import com.gridu.store.mapper.ProductMapper;
import com.gridu.store.model.ProductEntity;
import com.gridu.store.repository.ProductRepo;
import com.gridu.store.service.ProductService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final ProductMapper productMapper;

    @Override
    public List<ProductResponseDto> getAll(Pageable pageable) {
        return productRepo.findAll(pageable).stream()
                .map(productMapper::toProductResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponseDto addProduct(ProductRequestDto requestDto, String token) {
        ProductEntity productEntity = productMapper.toProductEntity(requestDto);
        ProductEntity byTitleAndPrice = productRepo.findByTitleAndPrice(requestDto.getTitle(), requestDto.getPrice());
        if (byTitleAndPrice != null) {
            byTitleAndPrice.setAvailable(byTitleAndPrice.getAvailable() + requestDto.getQuantity());
            productEntity = byTitleAndPrice;
        }
        productEntity = productRepo.save(productEntity);
        return productMapper.toProductResponseDto(productEntity);
    }

    public ProductEntity getProductEntity(Long productId) {
        return productRepo.findById(productId)
                .orElseThrow(() -> new ApiException(Exceptions.PRODUCT_NOT_FOUND));
    }
}
