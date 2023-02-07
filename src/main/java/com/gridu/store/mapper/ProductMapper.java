package com.gridu.store.mapper;

import com.gridu.store.dto.request.ProductRequestDto;
import com.gridu.store.dto.response.ProductResponseDto;
import com.gridu.store.model.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponseDto toProductResponseDto(ProductEntity productEntity);

    @Mapping(target = "available", source = "quantity")
    ProductEntity toProductEntity(ProductRequestDto productRequestDto);
}
