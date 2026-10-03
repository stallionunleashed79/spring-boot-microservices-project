package se.magnus.microservices.composite.product.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCompositeIntegration implements ProductService, ReviewService, RecommendationService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    @Value("${app.product-service.host}")
    private String productServiceHost;
    @Value("${app.product-service.port}")
    private int productServicePort;

    @Value("${app.recommendation-service.host}")
    private String recommendationServiceHost;
    @Value("${app.recommendation-service.port}")
    private int recommendationServicePort;

    @Value("${app.review-service.host}")
    private String reviewServiceHost;
    @Value("${app.review-service.port}")
    private int reviewServicePort;

    private String productServiceUrl;
    private String recommendationServiceUrl;
    private String reviewServiceUrl;
    private RestClient restClient = RestClient.create();

    @Override
    public Product getProduct(int productId) {
        try {
            final String url = "http://" + productServiceHost + ":" + productServicePort + "/product/{productId}";
            LOG.debug("Will call getProduct API on URL: {}", url);
            final Product product = restClient.get()
                    .uri(url, productId)
                    .retrieve()
                    .body(Product.class);
            assert product != null;
            LOG.debug("Found a product with id: {}", product.productId());
            return product;
        } catch (HttpClientErrorException ex) {
            switch (ex.getStatusCode()) {
                case NOT_FOUND -> throw new NotFoundException(getErrorMessage(ex));
                case UNPROCESSABLE_ENTITY -> throw new InvalidInputException(getErrorMessage(ex));
                default -> {
                    LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                    LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                    throw ex;
                }
            }
        }
    }

    @Override
    public List<Review> getReviews(int reviewId) {
        // Implementation for fetching review details
        return null; // Placeholder return statement
    }

    @Override
    public List<Recommendation> getRecommendations(int recommendationId) {
        // Implementation for fetching recommendation details
        return null; // Placeholder return statement
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).message();
    }
}
