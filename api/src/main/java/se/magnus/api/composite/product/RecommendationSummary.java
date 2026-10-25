package se.magnus.api.composite.product;

import lombok.Builder;

@Builder
public record RecommendationSummary(
  int recommendationId,
  String author,
  int rate) {
}
