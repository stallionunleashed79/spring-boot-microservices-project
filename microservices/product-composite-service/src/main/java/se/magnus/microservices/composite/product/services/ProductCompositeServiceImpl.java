package se.magnus.microservices.composite.product.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.magnus.api.composite.product.*;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.util.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private final ProductCompositeIntegration productCompositeIntegration;
    private final ServiceUtil serviceUtil;

    @Override
    public ProductAggregate getProduct(int productId) {
        final Product product = productCompositeIntegration.getProduct(productId);
        if (product == null) throw new NotFoundException("No product found for productId: " + productId);

        final List<Recommendation> recommendations = productCompositeIntegration.getRecommendations(productId);
        final List<Review> reviews = productCompositeIntegration.getReviews(productId);
        return buildProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    private ProductAggregate buildProductAggregate(final Product product,
                                                   final List<Recommendation> recommendations,
                                                   final List<Review> reviews,
                                                   final String serviceAddress) {
       final List<RecommendationSummary> recommendationSummaries = recommendations == null || recommendations.isEmpty()
               ? Collections.emptyList()
               : recommendations.stream().map(recommendation ->
                         RecommendationSummary.builder()
                                 .author(recommendation.author())
                                 .rate(recommendation.rate())
                                 .recommendationId(recommendation.recommendationId())
                                 .build()
              ).toList();
        final List<ReviewSummary> reviewSummaries = reviews == null || reviews.isEmpty()
                ? Collections.emptyList()
                : reviews.stream().map(review ->
                ReviewSummary.builder()
                    .reviewId(review.reviewId())
                    .author(review.author())
                    .subject(review.subject())
                        .build()
                ).toList();
        // 4. Create info regarding the involved microservices addresses
        final String productAddress = product.serviceAddress();
        final String reviewAddress = (reviews != null && !reviews.isEmpty()) ? reviews.get(0).serviceAddress() : "";
        final String recommendationAddress = (recommendations != null && !recommendations.isEmpty()) ? recommendations.get(0).serviceAddress() : "";
        final ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress,
                productAddress, reviewAddress, recommendationAddress);
        return ProductAggregate.builder()
                .productId(product.productId())
                .name(product.name())
                .weight(product.weight())
                .recommendations(recommendationSummaries)
                .reviews(reviewSummaries)
                .serviceAddresses(serviceAddresses)
                .build();
    }
}
