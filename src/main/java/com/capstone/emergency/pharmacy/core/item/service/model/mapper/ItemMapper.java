package com.capstone.emergency.pharmacy.core.item.service.model.mapper;

import com.capstone.emergency.pharmacy.core.item.repository.model.Item;
import com.capstone.emergency.pharmacy.core.item.service.model.Dose;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring"
)
public interface ItemMapper {
    Item.Dose toDoseEntity(Dose dose);
}
