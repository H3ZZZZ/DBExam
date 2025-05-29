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
  print("Database already has", existingCount, "reviews - clearing for fresh CSV data import");
  db.reviews.deleteMany({});
}

print("Generating reviews for all properties from CSV data...");

// Create indexes
db.reviews.createIndex({ "property_id": 1 });
db.reviews.createIndex({ "cleanliness_rating": 1 });
db.reviews.createIndex({ "guest_satisfaction": 1 });
db.reviews.createIndex({ "created_at": 1 });

// Define 5 cycling review comments
var cyclingComments = [
  "Excellent property with great amenities! The location was perfect and the host was very responsive.",
  "Good value for money. The place was clean and comfortable. Would definitely stay again.",
  "Amazing experience! Everything was exactly as described. Highly recommend this property.",
  "Nice place but could use some minor improvements. Overall a pleasant stay with good facilities.",
  "Outstanding property! Beautiful location and well-maintained. Perfect for our vacation needs."
];

// Function to read and parse CSV file
function loadCSVProperties() {
  try {
    print("Loading properties from CSV file...");
    
    // Use fs module to read the CSV file
    const fs = require('fs');
    var csvContent = fs.readFileSync("/data/cleaned_airbnb_data.csv", "utf8");
    var lines = csvContent.split('\n');
    var dataLines = lines.slice(1).filter(function(line) { return line.trim().length > 0; });
    var properties = [];
    
    dataLines.forEach(function(line, index) {
      var columns = line.split(',');
      if (columns.length < 11) return;
      
      try {
        var property = {
          ID: parseInt(columns[0]),
          cleanliness_rating: parseInt(columns[4]),
          guest_satisfaction_overall: parseInt(columns[5])
        };
        
        if (!isNaN(property.ID) && !isNaN(property.cleanliness_rating) && !isNaN(property.guest_satisfaction_overall)) {
          properties.push(property);
        }
      } catch (e) {
        print("Skipping line " + (index + 2) + " due to parsing error");
      }
    });
    
    print("Successfully loaded", properties.length, "properties from CSV");
    return properties;
    
  } catch (e) {
    print("Error loading CSV file:", e.message);
    print("Falling back to sample data...");
    return [
      {ID: 1, cleanliness_rating: 100, guest_satisfaction_overall: 93},
      {ID: 2, cleanliness_rating: 80, guest_satisfaction_overall: 85},
      {ID: 3, cleanliness_rating: 90, guest_satisfaction_overall: 87},
      {ID: 4, cleanliness_rating: 90, guest_satisfaction_overall: 90},
      {ID: 5, cleanliness_rating: 100, guest_satisfaction_overall: 98}
    ];
  }
}

var csvProperties = loadCSVProperties();
print("Processing", csvProperties.length, "properties from CSV data...");

// Generate reviews for each property
var totalInserted = 0;
var baseDate = new Date("2024-01-01");

csvProperties.forEach(function(property, propertyIndex) {
  var commentIndex = propertyIndex % cyclingComments.length;
  var cleanlinessRating = property.cleanliness_rating;
  var guestSatisfaction = property.guest_satisfaction_overall;
  var reviewDate = new Date(baseDate.getTime() + propertyIndex * 24 * 60 * 60 * 1000);
  
  var review = {
    property_id: property.ID,
    cleanliness_rating: cleanlinessRating,
    guest_satisfaction: guestSatisfaction,
    text_comment: cyclingComments[commentIndex],
    created_at: reviewDate
  };
  
  db.reviews.insertOne(review);
  totalInserted++;
  
  if ((propertyIndex + 1) % 1000 === 0) {
    print("Processed", propertyIndex + 1, "properties -", totalInserted, "reviews inserted");
  }
});

print("Review generation complete!");
print("Total reviews inserted:", totalInserted);
print("Properties covered:", csvProperties.length);
print("Reviews per property: 1 (cycling through 5 comment types)");

print("\nMongoDB initialization complete");
print("Database: airbnb_reviews");
print("Collection: reviews");
print("Total documents:", db.reviews.countDocuments());
print("Replica set: rs0 ready");

