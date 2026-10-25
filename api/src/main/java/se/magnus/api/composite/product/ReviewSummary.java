package se.magnus.api.composite.product;

import lombok.Builder;

@Builder
public record ReviewSummary(
  int reviewId,
  String author,
  String subject) {
}
