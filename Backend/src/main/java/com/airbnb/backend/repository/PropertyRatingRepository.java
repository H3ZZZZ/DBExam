package com.airbnb.backend.repository;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Date;

@Repository
public class PropertyRatingRepository {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    /**
     * Calculate and update ratings for a specific property
     */
    public Map<String, Object> calculatePropertyRating(Integer propertyId) {
        try {
            // Aggregation pipeline to calculate averages for the property
            List<Document> storedProcedurePipeline = Arrays.asList(
                new Document("$match", new Document("property_id", propertyId)),
                new Document("$group", new Document()
                    .append("_id", "$property_id")
                    .append("avgCleanlinessRating", new Document("$avg", "$cleanliness_rating"))
                    .append("avgSatisfactionRating", new Document("$avg", "$guest_satisfaction"))
                    .append("totalReviews", new Document("$sum", 1))
                ),
                new Document("$project", new Document()
                    .append("property_id", "$_id")
                    .append("avg_cleanliness_rating", new Document("$round", Arrays.asList("$avgCleanlinessRating", 2)))
                    .append("avg_satisfaction_rating", new Document("$round", Arrays.asList("$avgSatisfactionRating", 2)))
                    .append("total_reviews", "$totalReviews")
                    .append("last_updated", new Document("$literal", new java.util.Date()))
                    .append("_id", 0)
                )
            );
            
            List<Document> results = mongoTemplate.getCollection("reviews")
                .aggregate(storedProcedurePipeline)
                .into(new java.util.ArrayList<>());
            
            if (!results.isEmpty()) {
                Document ratingData = results.get(0);
                
                // Update or insert the property rating
                Query query = new Query(Criteria.where("property_id").is(propertyId));
                Update update = new Update()
                    .set("property_id", propertyId)
                    .set("avg_cleanliness_rating", ratingData.getDouble("avg_cleanliness_rating"))
                    .set("avg_satisfaction_rating", ratingData.getDouble("avg_satisfaction_rating"))
                    .set("total_reviews", ratingData.getInteger("total_reviews"))
                    .set("last_updated", LocalDateTime.now());
                
                mongoTemplate.upsert(query, update, "property_ratings");
                
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", true);
                response.put("property_id", propertyId);
                response.put("avg_cleanliness_rating", ratingData.getDouble("avg_cleanliness_rating"));
                response.put("avg_satisfaction_rating", ratingData.getDouble("avg_satisfaction_rating"));
                response.put("total_reviews", ratingData.getInteger("total_reviews"));
                response.put("message", "Rating calculated successfully");
                return response;
            } else {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", false);
                response.put("property_id", propertyId);
                response.put("message", "No reviews found for property");
                return response;
            }
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("property_id", propertyId);
            response.put("error", "Rating calculation error: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * Recalculate all property ratings
     */
    public Map<String, Object> recalculateAllPropertyRatings() {
        try {
            // Get all unique property IDs using aggregation
            List<Document> distinctPipeline = Arrays.asList(
                new Document("$group", new Document("_id", "$property_id")),
                new Document("$project", new Document("property_id", "$_id").append("_id", 0))
            );
            
            List<Document> propertyResults = mongoTemplate.getCollection("reviews")
                .aggregate(distinctPipeline)
                .into(new java.util.ArrayList<>());
            
            int processed = 0;
            for (Document doc : propertyResults) {
                Integer propertyId = doc.getInteger("property_id");
                Map<String, Object> result = calculatePropertyRating(propertyId);
                if ((Boolean) result.get("success")) {
                    processed++;
                }
            }
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("total_properties", propertyResults.size());
            response.put("processed_successfully", processed);
            response.put("message", "Batch rating recalculation completed");
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("error", "Batch processing error: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * Get property rating by ID
     */
    public Map<String, Object> getPropertyRating(Integer propertyId) {
        try {
            Query query = new Query(Criteria.where("property_id").is(propertyId));
            Document rating = mongoTemplate.findOne(query, Document.class, "property_ratings");
            
            if (rating != null) {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", true);
                response.put("property_id", propertyId);
                
                // Safely convert cleanliness rating to Double regardless of original type
                Object cleanlinessRating = rating.get("avg_cleanliness_rating");
                if (cleanlinessRating instanceof Integer) {
                    response.put("avg_cleanliness_rating", ((Integer) cleanlinessRating).doubleValue());
                } else {
                    response.put("avg_cleanliness_rating", rating.getDouble("avg_cleanliness_rating"));
                }
                
                // Safely convert satisfaction rating to Double regardless of original type
                Object satisfactionRating = rating.get("avg_satisfaction_rating");
                if (satisfactionRating instanceof Integer) {
                    response.put("avg_satisfaction_rating", ((Integer) satisfactionRating).doubleValue());
                } else {
                    response.put("avg_satisfaction_rating", rating.getDouble("avg_satisfaction_rating"));
                }
                
                response.put("total_reviews", rating.getInteger("total_reviews"));
                response.put("last_updated", rating.getDate("last_updated"));
                return response;
            } else {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", false);
                response.put("property_id", propertyId);
                response.put("message", "No rating found for property");
                return response;
            }
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("property_id", propertyId);
            response.put("error", "Rating retrieval error: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * Get all property ratings with pagination
     */
    public Map<String, Object> getAllPropertyRatings(Integer limit, Integer skip) {
        try {
            limit = limit != null ? limit : 100;
            skip = skip != null ? skip : 0;
            
            // Aggregation pipeline for pagination and sorting
            List<Document> paginationPipeline = Arrays.asList(
                new Document("$sort", new Document("avg_satisfaction_rating", -1)),
                new Document("$skip", skip),
                new Document("$limit", limit)
            );
            
            List<Document> ratings = mongoTemplate.getCollection("property_ratings")
                .aggregate(paginationPipeline)
                .into(new java.util.ArrayList<>());
            
            long total = mongoTemplate.count(new Query(), "property_ratings");
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("ratings", ratings);
            response.put("total_count", total);
            response.put("returned_count", ratings.size());
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("error", "Pagination error: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * Get top rated properties with sorting and filtering
     */
    public Map<String, Object> getTopRatedProperties(Integer limit, String ratingType) {
        try {
            limit = limit != null ? limit : 10;
            ratingType = ratingType != null ? ratingType : "satisfaction";
            
            String sortField = ratingType.equals("cleanliness") 
                ? "avg_cleanliness_rating" 
                : "avg_satisfaction_rating";
            
            List<Document> topRatedPipeline = Arrays.asList(
                new Document("$match", new Document(sortField, new Document("$exists", true))),
                new Document("$sort", new Document(sortField, -1)),
                new Document("$limit", limit)
            );
            
            List<Document> ratings = mongoTemplate.getCollection("property_ratings")
                .aggregate(topRatedPipeline)
                .into(new java.util.ArrayList<>());
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("rating_type", ratingType);
            response.put("top_properties", ratings);
            response.put("returned_count", ratings.size());
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("error", "Top rated query error: " + e.getMessage());
            return response;
        }
    }
} 