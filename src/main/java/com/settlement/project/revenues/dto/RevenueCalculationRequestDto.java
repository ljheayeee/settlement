package com.settlement.project.revenues.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class RevenueCalculationRequestDto {
    private final LocalDate calculationDate;

}