package se.magnus.microservices.composite.product.services;

import ch.qos.logback.classic.joran.action.ReceiverAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.message.ReusableObjectMessage;
import org.bouncycastle.asn1.x509.ReasonFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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

import javax.security.sasl.RealmChoiceCallback;
import javax.sound.midi.Receiver;
import java.util.Collections;
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
            processClientException(ex);
            return null;
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        try {
            final String url = "http://" + reviewServiceHost + ":" + reviewServicePort + "/product/{productId}";
            LOG.debug("Will call getProduct API on URL: {} with id: {}", url, productId);
            // FIX: Use ParameterizedTypeReference so Jackson knows to map the JSON to Review objects
            final List<Review> reviews = restClient.get()
                    .uri(url, productId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            assert reviews != null;
            LOG.debug("Found reviews for product with id: {}", productId);
            return reviews;
        } catch (HttpClientErrorException ex) {
            processClientException(ex);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        try {
            final String url = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/product/{productId}";
            LOG.debug("Will call getProduct API on URL: {} with id: {}", url, productId);
            // FIX: Use ParameterizedTypeReference so Jackson knows to map the JSON to Review objects
            final List<Recommendation> recommendations = restClient.get()
                    .uri(url, productId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            assert recommendations != null;
            LOG.debug("Found recommendations for product with id: {}", productId);
            return recommendations;
        } catch (HttpClientErrorException ex) {
            processClientException(ex);
            return Collections.emptyList();
        }
    }

    private void processClientException(HttpClientErrorException ex) {
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

    private String getErrorMessage(HttpClientErrorException ex) {
        return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).message();
    }
}
