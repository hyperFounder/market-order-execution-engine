package com.hft.engine.marketorderexecutionengine.mapper;

import com.hft.engine.marketorderexecutionengine.dto.OrderRequestDto;
import com.hft.engine.marketorderexecutionengine.dto.OrderResponseDto;
import com.hft.engine.marketorderexecutionengine.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper{

    @Mapping(target = "orderId", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "status", constant = "PENDING_ROUTE")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    OrderEntity toEntity(OrderRequestDto requestDto);

    @Mapping(target = "executionRoute", constant = "DIRECT_EXCHANGE_DMA")
    OrderResponseDto toResponseDto(OrderEntity entity);

}