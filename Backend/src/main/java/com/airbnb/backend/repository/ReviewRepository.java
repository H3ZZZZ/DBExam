package com.airbnb.backend.repository;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@Repository
public class ReviewRepository {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    /**
     * Get reviews for a property with pagination, sorting, and filtering
     */
    public Map<String, Object> getPropertyReviews(Integer propertyId, Integer limit, Integer skip, String sortBy, String sortOrder) {
        try {
            limit = limit != null ? limit : 10;
            skip = skip != null ? skip : 0;
            sortBy = sortBy != null ? sortBy : "created_at";
            sortOrder = sortOrder != null ? sortOrder : "desc";
            
            String validatedSortBy = validateSortField(sortBy);
            int sortDirection = "asc".equalsIgnoreCase(sortOrder) ? 1 : -1;
            
            // Aggregation pipeline for review retrieval with computed fields
            List<Document> reviewRetrievalPipeline = Arrays.asList(
                new Document("$match", new Document("property_id", propertyId)),
                new Document("$addFields", new Document()
                    .append("review_age_days", new Document("$divide", Arrays.asList(
                        new Document("$subtract", Arrays.asList(new java.util.Date(), "$created_at")),
                        86400000
                    )))
                    .append("overall_rating", new Document("$avg", Arrays.asList("$cleanliness_rating", "$guest_satisfaction")))
                ),
                new Document("$sort", new Document(validatedSortBy, sortDirection)),
                new Document("$skip", skip),
                new Document("$limit", limit)
            );
            
            List<Document> reviews = mongoTemplate.getCollection("reviews")
                .aggregate(reviewRetrievalPipeline)
                .into(new java.util.ArrayList<>());
            
            // Get total count for pagination
            long totalReviews = mongoTemplate.count(
                new Query(Criteria.where("property_id").is(propertyId)), 
                "reviews"
            );
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("property_id", propertyId);
            response.put("reviews", reviews);
            response.put("total_count", totalReviews);
            response.put("returned_count", reviews.size());
            
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("property_id", propertyId);
            response.put("error", "Error retrieving reviews: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * Add a new review
     */
    public Map<String, Object> addReview(Integer propertyId, Integer bookingId, Integer cleanlinessRating, Integer satisfactionRating, String comment) {
        try {
            Document review = new Document()
                .append("property_id", propertyId)
                .append("booking_id", bookingId)
                .append("cleanliness_rating", cleanlinessRating)
                .append("guest_satisfaction", satisfactionRating)
                .append("text_comment", comment)
                .append("created_at", new java.util.Date());
            
            mongoTemplate.insert(review, "reviews");
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("property_id", propertyId);
            response.put("booking_id", bookingId);
            response.put("review_id", review.getObjectId("_id").toString());
            response.put("cleanliness_rating", cleanlinessRating);
            response.put("guest_satisfaction", satisfactionRating);
            response.put("message", "Review added successfully");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("property_id", propertyId);
            response.put("booking_id", bookingId);
            response.put("error", "Review insertion error: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * Get all reviews with advanced filtering
     */
    public Map<String, Object> getAllReviews(Integer limit, Integer skip, String sortBy, String sortOrder, Integer minRating, Integer maxRating) {
        try {
            limit = limit != null ? limit : 20;
            skip = skip != null ? skip : 0;
            sortBy = sortBy != null ? sortBy : "created_at";
            sortOrder = sortOrder != null ? sortOrder : "desc";
            
            String validatedSortBy = validateSortField(sortBy);
            int sortDirection = "asc".equalsIgnoreCase(sortOrder) ? 1 : -1;
            
            // Build dynamic match criteria
            Document matchCriteria = new Document();
            if (minRating != null || maxRating != null) {
                Document ratingFilter = new Document();
                if (minRating != null) ratingFilter.append("$gte", minRating);
                if (maxRating != null) ratingFilter.append("$lte", maxRating);
                matchCriteria.append("guest_satisfaction", ratingFilter);
            }
            
            // Aggregation pipeline with conditional filtering
            List<Document> pipeline = Arrays.asList(
                new Document("$match", matchCriteria),
                new Document("$addFields", new Document()
                    .append("overall_rating", new Document("$avg", Arrays.asList("$cleanliness_rating", "$guest_satisfaction")))
                ),
                new Document("$sort", new Document(validatedSortBy, sortDirection)),
                new Document("$skip", skip),
                new Document("$limit", limit)
            );
            
            List<Document> reviews = mongoTemplate.getCollection("reviews")
                .aggregate(pipeline)
                .into(new java.util.ArrayList<>());
            
            // Count total with same filters
            List<Document> countPipeline = Arrays.asList(
                new Document("$match", matchCriteria),
                new Document("$count", "total")
            );
            
            List<Document> countResult = mongoTemplate.getCollection("reviews")
                .aggregate(countPipeline)
                .into(new java.util.ArrayList<>());
            
            long totalReviews = !countResult.isEmpty() ? countResult.get(0).getInteger("total") : 0;
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("reviews", reviews);
            response.put("total_count", totalReviews);
            response.put("returned_count", reviews.size());
            
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("error", "Advanced filtering error: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * Get advanced analytics for a property's reviews
     */
    public Map<String, Object> getReviewAnalytics(Integer propertyId) {
        try {
            // Analytics aggregation pipeline
            List<Document> analyticsPipeline = Arrays.asList(
                new Document("$match", new Document("property_id", propertyId)),
                new Document("$group", new Document("_id", null)
                    .append("total_reviews", new Document("$sum", 1))
                    .append("avg_cleanliness", new Document("$avg", "$cleanliness_rating"))
                    .append("avg_satisfaction", new Document("$avg", "$guest_satisfaction"))
                    .append("max_cleanliness", new Document("$max", "$cleanliness_rating"))
                    .append("min_cleanliness", new Document("$min", "$cleanliness_rating"))
                    .append("max_satisfaction", new Document("$max", "$guest_satisfaction"))
                    .append("min_satisfaction", new Document("$min", "$guest_satisfaction"))
                )
            );
            
            List<Document> results = mongoTemplate.getCollection("reviews")
                .aggregate(analyticsPipeline)
                .into(new java.util.ArrayList<>());
            
            if (!results.isEmpty()) {
                Document analytics = results.get(0);
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", true);
                response.put("property_id", propertyId);
                response.put("total_reviews", analytics.getInteger("total_reviews"));
                response.put("avg_cleanliness_rating", analytics.getDouble("avg_cleanliness"));
                response.put("avg_satisfaction_rating", analytics.getDouble("avg_satisfaction"));
                response.put("rating_ranges", Map.of(
                    "cleanliness", Map.of("min", analytics.getInteger("min_cleanliness"), "max", analytics.getInteger("max_cleanliness")),
                    "satisfaction", Map.of("min", analytics.getInteger("min_satisfaction"), "max", analytics.getInteger("max_satisfaction"))
                ));
                return response;
            } else {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", false);
                response.put("property_id", propertyId);
                response.put("message", "No reviews found for analytics");
                return response;
            }
            
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("property_id", propertyId);
            response.put("error", "Analytics generation error: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * Get system-wide review summary statistics
     */
    public Map<String, Object> getReviewSummary() {
        try {
            // System-wide statistics aggregation pipeline
            List<Document> summaryPipeline = Arrays.asList(
                new Document("$group", new Document("_id", null)
                    .append("total_reviews", new Document("$sum", 1))
                    .append("unique_properties", new Document("$addToSet", "$property_id"))
                    .append("avg_cleanliness_overall", new Document("$avg", "$cleanliness_rating"))
                    .append("avg_satisfaction_overall", new Document("$avg", "$guest_satisfaction"))
                ),
                new Document("$addFields", new Document()
                    .append("unique_property_count", new Document("$size", "$unique_properties"))
                    .append("avg_reviews_per_property", new Document("$divide", Arrays.asList("$total_reviews", new Document("$size", "$unique_properties"))))
                )
            );
            
            List<Document> results = mongoTemplate.getCollection("reviews")
                .aggregate(summaryPipeline)
                .into(new java.util.ArrayList<>());
            
            if (!results.isEmpty()) {
                Document summary = results.get(0);
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", true);
                response.put("total_reviews", summary.getInteger("total_reviews"));
                response.put("unique_properties", summary.getInteger("unique_property_count"));
                response.put("avg_reviews_per_property", summary.getDouble("avg_reviews_per_property"));
                response.put("overall_avg_cleanliness", summary.getDouble("avg_cleanliness_overall"));
                response.put("overall_avg_satisfaction", summary.getDouble("avg_satisfaction_overall"));
                return response;
            } else {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", false);
                response.put("message", "No review data available");
                return response;
            }
            
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("error", "Summary generation error: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * Validate sort field to prevent injection
     */
    private String validateSortField(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "created_at":
            case "cleanliness_rating":
            case "guest_satisfaction":
            case "overall_rating":
            case "review_age_days":
                return sortBy;
            default:
                return "created_at";
        }
    }

    public List<Map<String, Object>> getReviewsByBookingIds(List<Integer> bookingIds) {
        Query query = new Query(Criteria.where("booking_id").in(bookingIds));
        List<org.bson.Document> documents = mongoTemplate.find(query, org.bson.Document.class, "reviews");

        return documents.stream()
                .map(doc -> (Map<String, Object>) new HashMap<>(doc)) // Cast to Map explicitly
                .toList();
    }



} 