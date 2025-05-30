// Airbnb Analytics Platform - Optimized MongoDB Initialization
// High-performance initialization script with batch processing

print("Starting MongoDB replica set initialization...");

// Performance tracking
var startTime = new Date();
var timingMarks = {};

function markTime(label) {
  timingMarks[label] = new Date();
  var elapsed = timingMarks[label] - startTime;
  print(label + ": " + elapsed + "ms");
}

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

markTime("Replica set setup");

// Wait for replica set to elect primary and be ready for writes
print("Waiting for replica set primary election...");
var timeout = 60;
var primaryReady = false;

while (timeout > 0 && !primaryReady) {
  try {
    var status = rs.status();
    if (status.ok === 1) {
      var primary = status.members.find(m => m.stateStr === 'PRIMARY');
      if (primary) {
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

markTime("Primary election");

// Setup reviews database
print("Setting up reviews database...");
db = db.getSiblingDB('airbnb_reviews');

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
  var deleteStart = new Date();
  db.reviews.deleteMany({});
  var deleteTime = new Date() - deleteStart;
  print("Cleared", existingCount, "documents in", deleteTime, "ms");
}

print("Generating reviews with optimized batch processing...");

// Create indexes before data loading
print("Creating indexes...");
var indexStart = new Date();
db.reviews.createIndex({ "property_id": 1 });
db.reviews.createIndex({ "cleanliness_rating": 1 });
db.reviews.createIndex({ "guest_satisfaction": 1 });
db.reviews.createIndex({ "created_at": 1 });
var indexTime = new Date() - indexStart;
print("Indexes created in", indexTime, "ms");

markTime("Database setup");

// Generic comments for reviews
var cyclingComments = [
  "Excellent property with great amenities! The location was perfect and the host was very responsive.",
  "Good value for money. The place was clean and comfortable. Would definitely stay again.",
  "Amazing experience! Everything was exactly as described. Highly recommend this property.",
  "Nice place but could use some minor improvements. Overall a pleasant stay with good facilities.",
  "Outstanding property! Beautiful location and well-maintained. Perfect for our vacation needs."
];

var commentsLength = cyclingComments.length;
var baseDate = new Date("2024-01-01");
var baseDateMs = baseDate.getTime();
var dayInMs = 24 * 60 * 60 * 1000;

// Load properties from CSV file - previously used insertOne but this is more efficient
function loadCSVPropertiesOptimized() {
  try {
    print("Loading properties from CSV file...");
    var loadStart = new Date();
    
    const fs = require('fs');
    var csvContent = fs.readFileSync("/data/cleaned_airbnb_data.csv", "utf8");
    var lines = csvContent.split('\n');
    var dataLines = lines.slice(1).filter(function(line) { return line.trim().length > 0; });
    var properties = [];
    
    properties.length = dataLines.length;
    var validCount = 0;
    
    dataLines.forEach(function(line) {
      var columns = line.split(',');
      if (columns.length < 11) return;
      
      try {
        var id = parseInt(columns[0]);
        var cleanliness = parseInt(columns[4]);
        var satisfaction = parseInt(columns[5]);
        
        if (!isNaN(id) && !isNaN(cleanliness) && !isNaN(satisfaction)) {
          properties[validCount] = {
            ID: id,
            cleanliness_rating: cleanliness,
            guest_satisfaction_overall: satisfaction
          };
          validCount++;
        }
      } catch (e) {
        print("Error processing line:", e.message);
      }
    });
    
    properties.length = validCount;
    
    var loadTime = new Date() - loadStart;
    print("Successfully loaded", validCount, "properties from CSV in", loadTime, "ms");
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

var csvProperties = loadCSVPropertiesOptimized();
markTime("CSV loading");

print("Processing", csvProperties.length, "properties with batch inserts...");

// Optimized batch processing
var batchSize = 1000;
var batch = [];
var totalInserted = 0;
var processingStart = new Date();
var totalDbInsertTime = 0; // Track actual DB insertion time separately

batch.length = batchSize;
var batchIndex = 0;

csvProperties.forEach(function(property, propertyIndex) {
  var review = {
    property_id: property.ID,
    cleanliness_rating: property.cleanliness_rating,
    guest_satisfaction: property.guest_satisfaction_overall,
    text_comment: cyclingComments[propertyIndex % commentsLength],
    created_at: new Date(baseDateMs + propertyIndex * dayInMs)
  };
  
  batch[batchIndex] = review;
  batchIndex++;
  
  // Insert batch when full
  if (batchIndex >= batchSize) {
    batch.length = batchIndex;
    
    var batchStart = new Date();
    db.reviews.insertMany(batch, {ordered: false, writeConcern: {w: 1, j: false}});
    var batchTime = new Date() - batchStart;
    totalDbInsertTime += batchTime;
    
    totalInserted += batchIndex;
    
    print("Inserted batch:", totalInserted, "total reviews (" + batchTime + "ms for", batchIndex, "docs,", 
          Math.round(batchIndex / (batchTime / 1000)), "docs/sec)");
    
    // Reset batch
    batch = [];
    batch.length = batchSize;
    batchIndex = 0;
  }
});

// Insert remaining records
if (batchIndex > 0) {
  batch.length = batchIndex;
  var finalBatchStart = new Date();
  db.reviews.insertMany(batch, {ordered: false, writeConcern: {w: 1, j: false}});
  var finalBatchTime = new Date() - finalBatchStart;
  totalDbInsertTime += finalBatchTime;
  
  totalInserted += batchIndex;
  print("Inserted final batch:", totalInserted, "total reviews (" + finalBatchTime + "ms for", batchIndex, "docs)");
}

var totalProcessingTime = new Date() - processingStart;
print("Data processing completed in:", totalProcessingTime, "ms");
print("Pure DB insertion time:", totalDbInsertTime, "ms");
print("Object creation overhead:", (totalProcessingTime - totalDbInsertTime), "ms");

// Performance summary
print("\nPERFORMANCE SUMMARY:");
print("Total reviews inserted:", totalInserted);
print("Properties covered:", csvProperties.length);
print("Reviews per property: 1 (cycling through", commentsLength, "comment types)");
print("Total processing time:", totalProcessingTime, "ms");
print("Pure DB insertion time:", totalDbInsertTime, "ms");
print("Average insertion rate (pure DB):", Math.round(totalInserted / (totalDbInsertTime / 1000)), "docs/sec");
print("Average processing rate (overall):", Math.round(totalInserted / (totalProcessingTime / 1000)), "docs/sec");
print("Total script runtime:", new Date() - startTime, "ms");

// Verify final count
var finalCount = db.reviews.countDocuments();
print("Final document count verification:", finalCount);

if (finalCount === totalInserted) {
  print("SUCCESS: All documents inserted correctly!");
} else {
  print("WARNING: Count mismatch - expected", totalInserted, "but found", finalCount);
}

print("\nMongoDB initialization complete");
print("Database: airbnb_reviews");
print("Collection: reviews");
print("Replica set: rs0 ready"); 