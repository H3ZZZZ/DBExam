// MongoDB setup for Airbnb reviews
print("Setting up MongoDB for reviews with English schema!");

db = db.getSiblingDB('airbnb_reviews');

db.createCollection("reviews");

print("Creating indexes for optimal performance...");

db.reviews.createIndex({ "property_id": 1 });
db.reviews.createIndex({ "cleanliness_rating": 1 });
db.reviews.createIndex({ "guest_satisfaction": 1 });
db.reviews.createIndex({ "created_at": 1 });

print("Inserting sample reviews data...");

db.reviews.insertMany([
    {
        property_id: 1,
        cleanliness_rating: 5,
        guest_satisfaction: 4,
        text_comment: "Amazing apartment in the heart of London! Host was super helpful and the location was perfect. Walking distance to all major attractions.",
        created_at: new Date("2024-01-18")
    },
    {
        property_id: 2,
        cleanliness_rating: 4,
        guest_satisfaction: 5,
        text_comment: "Really good value for money in Berlin. Room was clean and the host gave excellent local recommendations.",
        created_at: new Date("2024-01-23")
    },
    {
        property_id: 3,
        cleanliness_rating: 5,
        guest_satisfaction: 5,
        text_comment: "Luxury apartment with fantastic views. Everything was perfect - will definitely book again!",
        created_at: new Date("2024-02-04")
    },
    {
        property_id: 4,
        cleanliness_rating: 3,
        guest_satisfaction: 3,
        text_comment: "Okay stay, but a bit far from the center. Host was friendly enough.",
        created_at: new Date("2024-02-13")
    },
    {
        property_id: 5,
        cleanliness_rating: 4,
        guest_satisfaction: 4,
        text_comment: "Nice apartment in Barcelona. Good space and great facilities. Only downside was street noise.",
        created_at: new Date("2024-02-18")
    },
    {
        property_id: 1,
        cleanliness_rating: 5,
        guest_satisfaction: 5,
        text_comment: "Another fantastic stay! This apartment is truly a gem.",
        created_at: new Date("2024-03-04")
    },
    {
        property_id: 2,
        cleanliness_rating: 4,
        guest_satisfaction: 4,
        text_comment: "Cozy and clean. Host responded quickly to all questions.",
        created_at: new Date("2024-03-18")
    },
    {
        property_id: 6,
        cleanliness_rating: 3,
        guest_satisfaction: 4,
        text_comment: "Budget room, but you get what you pay for. Fine for a short trip.",
        created_at: new Date("2024-03-23")
    }
]);

print("MongoDB reviews setup complete!");
print("Created reviews collection with fields: property_id, cleanliness_rating, guest_satisfaction, text_comment");
print("Sample data inserted - ready for use with SQL database!");
