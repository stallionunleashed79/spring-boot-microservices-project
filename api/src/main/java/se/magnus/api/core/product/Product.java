package se.magnus.api.core.product;

public record Product(int productId,
                      String name,
                      int weight,
                      String serviceAddress) {
}
