// 🌟 Airbnb Analytics Platform - MongoDB Initialization
// Smart initialization script that handles fresh deployments and existing data

print("Starting MongoDB replica set initialization...");

try {
  // Initialize replica set
  const result = rs.initiate({
    _id: "rs0",
    members: [
      { _id: 0, host: "mongo1:27017", priority: 2 },
      { _id: 1, host: "mongo2:27017", priority: 1 },
      { _id: 2, host: "mongo3:27017", priority: 1 }
    ]
  });
  print("Replica set initiated:", JSON.stringify(result));
  
} catch (e) {
  if (e.message.includes("already initialized") || e.codeName === "AlreadyInitialized") {
    print("Replica set already initialized");
  } else {
    print("Replica set initiation error:", e.message);
  }
}

// Wait for replica set to elect primary and be ready for writes
print("Waiting for replica set primary election...");
var timeout = 60; // 1 minute timeout
var primaryReady = false;

while (timeout > 0 && !primaryReady) {
  try {
    var status = rs.status();
    if (status.ok === 1) {
      var primary = status.members.find(m => m.stateStr === 'PRIMARY');
      if (primary) {
        // Test write capability
        try {
          db.getSiblingDB('test').testWrite.insertOne({test: new Date()});
          db.getSiblingDB('test').testWrite.drop();
          primaryReady = true;
          print("Primary elected and ready:", primary.name);
        } catch (writeError) {
          print("Primary not ready for writes, waiting...");
        }
      }
    }
  } catch (e) {
    // Replica set not ready yet
  }
  
  if (!primaryReady) {
    sleep(2000);
    timeout -= 2;
  }
}

if (!primaryReady) {
  print("ERROR: Timeout waiting for primary to be ready");
  quit(1);
}

// Setup reviews database
print("Setting up reviews database...");
db = db.getSiblingDB('airbnb_reviews');

// Create collection if needed
try {
  db.createCollection('reviews');
  print("Created reviews collection");
} catch (e) {
  if (e.codeName === 'NamespaceExists') {
    print("Reviews collection already exists");
  }
}

// Check existing data
var existingCount = db.reviews.countDocuments();
if (existingCount > 0) {
  print("Database already has", existingCount, "reviews - skipping data insertion");
} else {
  print("Inserting sample data...");
  
  // Create indexes
  db.reviews.createIndex({ "property_id": 1 });
  db.reviews.createIndex({ "cleanliness_rating": 1 });
  db.reviews.createIndex({ "guest_satisfaction": 1 });
  db.reviews.createIndex({ "created_at": 1 });
  
  // Insert sample reviews
  var reviews = [
    {
      property_id: 1,
      cleanliness_rating: 5,
      guest_satisfaction: 5,
      text_comment: "Amazing waterfront property! The views were breathtaking and the amenities were top-notch.",
      created_at: new Date("2024-01-15")
    },
    {
      property_id: 2,
      cleanliness_rating: 4,
      guest_satisfaction: 4,
      text_comment: "Great downtown location, but a bit noisy at night. Overall a good stay.",
      created_at: new Date("2024-01-20")
    },
    {
      property_id: 3,
      cleanliness_rating: 5,
      guest_satisfaction: 5,
      text_comment: "Perfect mountain retreat! Peaceful and well-equipped for a relaxing getaway.",
      created_at: new Date("2024-02-01")
    },
    {
      property_id: 4,
      cleanliness_rating: 3,
      guest_satisfaction: 3,
      text_comment: "The location was good but the property needed some maintenance updates.",
      created_at: new Date("2024-02-10")
    },
    {
      property_id: 5,
      cleanliness_rating: 5,
      guest_satisfaction: 5,
      text_comment: "Stunning beachfront location! Could hear the waves from the bedroom. Highly recommend!",
      created_at: new Date("2024-02-15")
    }
  ];
  
  var insertResult = db.reviews.insertMany(reviews);
  print("Inserted", insertResult.insertedIds.length, "sample reviews");
}

print("MongoDB initialization complete");
print("Database: airbnb_reviews");
print("Collection: reviews");
print("Documents:", db.reviews.countDocuments());
print("Replica set: rs0 ready");

