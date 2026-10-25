package se.magnus.api.composite.product;

import lombok.Builder;

import java.util.List;

@Builder
public record ProductAggregate(
  int productId,
  String name,
  int weight,
  List<RecommendationSummary> recommendations,
  List<ReviewSummary> reviews,
  ServiceAddresses serviceAddresses) {
}
