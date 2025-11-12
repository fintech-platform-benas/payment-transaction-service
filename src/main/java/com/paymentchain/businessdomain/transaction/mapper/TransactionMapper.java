package com.paymentchain.businessdomain.transaction.mapper;

import com.paymentchain.businessdomain.transaction.entities.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

public interface TransactionMapper {

    void updateTransactionFromRequest(Transaction source, @MappingTarget Transaction target);

}
