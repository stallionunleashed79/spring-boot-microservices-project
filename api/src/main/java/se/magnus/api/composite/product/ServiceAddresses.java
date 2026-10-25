package se.magnus.api.composite.product;

import lombok.Builder;

@Builder
public record ServiceAddresses(
  String cmp,
  String pro,
  String rev,
  String rec) {
}
