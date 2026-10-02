package com.travel.booking.dto;

import java.math.BigDecimal;

/** Result of the SQL function fn_calculate_package_cost. */
public record CostResponse(Long packageId, Integer travelers, BigDecimal totalCost) {
}
