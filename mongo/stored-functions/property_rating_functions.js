// MongoDB Stored Functions for Property Ratings
// These functions act as stored procedures in MongoDB

// Function to calculate average ratings for a specific property
db.system.js.save({
    _id: "calculatePropertyRating",
    value: function(propertyId) {
        var result = db.reviews.aggregate([
            { $match: { property_id: propertyId } },
            {
                $group: {
                    _id: "$property_id",
                    avgCleanlinessRating: { $avg: "$cleanliness_rating" },
                    avgSatisfactionRating: { $avg: "$guest_satisfaction" },
                    totalReviews: { $sum: 1 }
                }
            }
        ]).toArray();
        
        if (result.length > 0) {
            var rating = result[0];
            
            // Upsert the property rating
            db.property_ratings.replaceOne(
                { property_id: propertyId },
                {
                    property_id: propertyId,
                    avg_cleanliness_rating: rating.avgCleanlinessRating,
                    avg_satisfaction_rating: rating.avgSatisfactionRating,
                    total_reviews: rating.totalReviews,
                    last_updated: new Date()
                },
                { upsert: true }
            );
            
            return {
                success: true,
                property_id: propertyId,
                avg_cleanliness_rating: rating.avgCleanlinessRating,
                avg_satisfaction_rating: rating.avgSatisfactionRating,
                total_reviews: rating.totalReviews
            };
        } else {
            return {
                success: false,
                message: "No reviews found for property " + propertyId
            };
        }
    }
});

// Function to recalculate all property ratings
db.system.js.save({
    _id: "recalculateAllPropertyRatings",
    value: function() {
        var propertyIds = db.reviews.distinct("property_id");
        var processed = 0;
        var errors = [];
        
        propertyIds.forEach(function(propertyId) {
            try {
                calculatePropertyRating(propertyId);
                processed++;
            } catch (e) {
                errors.push({ property_id: propertyId, error: e.message });
            }
        });
        
        return {
            success: true,
            total_properties: propertyIds.length,
            processed: processed,
            errors: errors
        };
    }
});

// Function to get property rating
db.system.js.save({
    _id: "getPropertyRating",
    value: function(propertyId) {
        var rating = db.property_ratings.findOne({ property_id: propertyId });
        if (rating) {
            return {
                success: true,
                data: rating
            };
        } else {
            return {
                success: false,
                message: "No rating found for property " + propertyId
            };
        }
    }
});

// Function to get all property ratings with pagination
db.system.js.save({
    _id: "getAllPropertyRatings",
    value: function(limit, skip) {
        limit = limit || 100;
        skip = skip || 0;
        
        var ratings = db.property_ratings.find()
            .sort({ avg_satisfaction_rating: -1 })
            .skip(skip)
            .limit(limit)
            .toArray();
            
        var total = db.property_ratings.countDocuments();
        
        return {
            success: true,
            data: ratings,
            total: total,
            limit: limit,
            skip: skip
        };
    }
});

// Function to get top rated properties
db.system.js.save({
    _id: "getTopRatedProperties",
    value: function(limit, ratingType) {
        limit = limit || 10;
        ratingType = ratingType || "satisfaction"; // "satisfaction" or "cleanliness"
        
        var sortField = ratingType === "cleanliness" 
            ? { avg_cleanliness_rating: -1 }
            : { avg_satisfaction_rating: -1 };
        
        var ratings = db.property_ratings.find()
            .sort(sortField)
            .limit(limit)
            .toArray();
        
        return {
            success: true,
            rating_type: ratingType,
            data: ratings
        };
    }
});

print("Property rating stored functions created successfully!"); 