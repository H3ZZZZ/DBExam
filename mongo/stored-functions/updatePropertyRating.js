// MongoDB Stored Function: Update Property Rating
// This function recalculates and updates the rating for a specific property

db.system.js.save({
    _id: "updatePropertyRating",
    value: function(propertyId) {
        try {
            // Calculate new averages for the specific property
            const ratingData = db.reviews.aggregate([
                { $match: { property_id: propertyId } },
                {
                    $group: {
                        _id: "$property_id",
                        avg_cleanliness_rating: { $avg: "$cleanliness_rating" },
                        avg_satisfaction_rating: { $avg: "$guest_satisfaction" },
                        total_reviews: { $sum: 1 },
                        last_review_date: { $max: "$created_at" }
                    }
                },
                {
                    $project: {
                        property_id: "$_id",
                        avg_cleanliness_rating: { $round: ["$avg_cleanliness_rating", 2] },
                        avg_satisfaction_rating: { $round: ["$avg_satisfaction_rating", 2] },
                        total_reviews: 1,
                        last_updated: new Date(),
                        last_review_date: { $ifNull: ["$last_review_date", new Date()] },
                        _id: 0
                    }
                }
            ]).next();

            if (ratingData) {
                // Update or insert the property rating (upsert)
                const result = db.property_ratings.replaceOne(
                    { property_id: propertyId },
                    ratingData,
                    { upsert: true }
                );

                return {
                    success: true,
                    property_id: propertyId,
                    modified: result.modifiedCount > 0,
                    upserted: result.upsertedCount > 0,
                    data: ratingData
                };
            } else {
                // No reviews found for this property - remove from ratings if exists
                const deleteResult = db.property_ratings.deleteOne({ property_id: propertyId });
                
                return {
                    success: true,
                    property_id: propertyId,
                    message: "No reviews found - rating removed",
                    deleted: deleteResult.deletedCount > 0
                };
            }
        } catch (error) {
            return {
                success: false,
                property_id: propertyId,
                error: error.message
            };
        }
    }
});

print("✅ Stored function 'updatePropertyRating' created successfully"); 