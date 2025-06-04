package com.airbnb.backend.repository;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

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
                        new Document("$subtract", Arrays.asList(new Date(), "$created_at")),
                        86400000
                    )))
                    .append("overall_rating", new Document("$avg", Arrays.asList("$cleanliness_rating", "$guest_satisfaction")))
                    .append("rating_category", new Document("$switch", new Document()
                        .append("branches", Arrays.asList(
                            new Document("case", new Document("$gte", Arrays.asList("$guest_satisfaction", 4)))
                                .append("then", "Excellent"),
                            new Document("case", new Document("$gte", Arrays.asList("$guest_satisfaction", 3)))
                                .append("then", "Good"),
                            new Document("case", new Document("$gte", Arrays.asList("$guest_satisfaction", 2)))
                                .append("then", "Fair")
                        ))
                        .append("default", "Poor")
                    ))
                ),
                new Document("$project", new Document()
                    .append("property_id", 1)
                    .append("booking_id", 1)
                    .append("cleanliness_rating", 1)
                    .append("guest_satisfaction", 1)
                    .append("text_comment", 1)
                    .append("created_at", 1)
                    .append("review_age_days", new Document("$round", Arrays.asList("$review_age_days", 1)))
                    .append("overall_rating", new Document("$round", Arrays.asList("$overall_rating", 2)))
                    .append("rating_category", 1)
                ),
                new Document("$sort", new Document(validatedSortBy, sortDirection)),
                new Document("$skip", skip),
                new Document("$limit", limit)
            );
            
            List<Document> reviews = mongoTemplate.getCollection("reviews")
                .aggregate(reviewRetrievalPipeline)
                .into(new java.util.ArrayList<>());
            
            // Get total count for pagination
            List<Document> countPipeline = Arrays.asList(
                new Document("$match", new Document("property_id", propertyId)),
                new Document("$count", "total")
            );
            
            List<Document> countResult = mongoTemplate.getCollection("reviews")
                .aggregate(countPipeline)
                .into(new java.util.ArrayList<>());
            
            long totalReviews = !countResult.isEmpty() ? countResult.get(0).getInteger("total") : 0;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("property_id", propertyId);
            response.put("reviews", reviews);
            
            // Pagination metadata
            Map<String, Object> paginationInfo = new HashMap<>();
            paginationInfo.put("total_reviews", totalReviews);
            paginationInfo.put("returned_count", reviews.size());
            paginationInfo.put("limit", limit);
            paginationInfo.put("skip", skip);
            paginationInfo.put("has_next", (skip + limit) < totalReviews);
            paginationInfo.put("has_previous", skip > 0);
            response.put("pagination", paginationInfo);
            
            // Sorting metadata
            Map<String, Object> sortingInfo = new HashMap<>();
            sortingInfo.put("sort_by", validatedSortBy);
            sortingInfo.put("sort_order", sortOrder);
            response.put("sorting", sortingInfo);
            
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
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
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Review added successfully");
            response.put("property_id", propertyId);
            response.put("booking_id", bookingId);
            response.put("review_data", review);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
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
                    .append("review_length", new Document("$strLenCP", "$text_comment"))
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
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reviews", reviews);
            response.put("total_count", totalReviews);
            response.put("returned_count", reviews.size());
            
            // Filters applied metadata
            Map<String, Object> filtersApplied = new HashMap<>();
            filtersApplied.put("min_rating", minRating);
            filtersApplied.put("max_rating", maxRating);
            filtersApplied.put("sort_by", validatedSortBy);
            filtersApplied.put("sort_order", sortOrder);
            response.put("filters_applied", filtersApplied);
            
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
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
                    .append("reviews", new Document("$push", "$$ROOT"))
                ),
                new Document("$addFields", new Document()
                    .append("rating_distribution", new Document("$map", new Document()
                        .append("input", Arrays.asList(1, 2, 3, 4, 5))
                        .append("as", "rating")
                        .append("in", new Document()
                            .append("rating", "$$rating")
                            .append("count", new Document("$size", new Document("$filter", new Document()
                                .append("input", "$reviews")
                                .append("cond", new Document("$eq", Arrays.asList("$$this.guest_satisfaction", "$$rating")))
                            )))
                        )
                    ))
                    .append("recent_trend", new Document("$slice", Arrays.asList(
                        new Document("$sortArray", new Document()
                            .append("input", "$reviews")
                            .append("sortBy", new Document("created_at", -1))
                        ),
                        5
                    )))
                )
            );
            
            List<Document> results = mongoTemplate.getCollection("reviews")
                .aggregate(analyticsPipeline)
                .into(new java.util.ArrayList<>());
            
            Map<String, Object> response = new HashMap<>();
            if (!results.isEmpty()) {
                Document analytics = results.get(0);
                response.put("success", true);
                response.put("property_id", propertyId);
                response.put("analytics", analytics);
            } else {
                response.put("success", false);
                response.put("message", "No reviews found for analytics");
            }
            
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
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
            
            Map<String, Object> response = new HashMap<>();
            if (!results.isEmpty()) {
                response.put("success", true);
                response.put("summary", results.get(0));
            } else {
                response.put("success", false);
                response.put("message", "No review data available");
            }
            
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
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

    public List<Map<String, Object>> getReviewsByPropertyIds(List<Integer> propertyIds) {
        Query query = new Query(Criteria.where("property_id").in(propertyIds));
        List<Document> rawResults = mongoTemplate.find(query, Document.class, "reviews");

        // Convert Document to Map<String, Object>
        return rawResults.stream()
                .map(doc -> new HashMap<String, Object>(doc))
                .collect(Collectors.toList());
    }



} 