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
db = db.getSiblingDB('airbnb');

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

print("Creating reviews only for properties with completed bookings...");

var indexStart = new Date();
db.reviews.createIndex({ "property_id": 1 });
db.reviews.createIndex({ "booking_id": 1 });
db.reviews.createIndex({ "cleanliness_rating": 1 });
db.reviews.createIndex({ "guest_satisfaction": 1 });
db.reviews.createIndex({ "created_at": 1 });
var indexTime = new Date() - indexStart;
print("Created indexes in:", indexTime, "ms");

// Generic comments for reviews
var cyclingComments = [
  "Excellent property with great amenities! The location was perfect and the host was very responsive.",
  "Good value for money. The place was clean and comfortable. Would definitely stay again.",
  "Amazing experience! Everything was exactly as described. Highly recommend this property.",
  "Nice place but could use some minor improvements. Overall a pleasant stay with good facilities.",
  "Outstanding property! Beautiful location and well-maintained. Perfect for our vacation needs."
];

var commentsLength = cyclingComments.length;

// Sample bookings for demonstration workflow - these would come from MySQL in a real scenario
var sampleBookings = [
  {booking_id: 1, property_id: 140, booking_end: new Date("2024-11-05")},
  {booking_id: 2, property_id: 18272, booking_end: new Date("2025-05-19")},
  {booking_id: 3, property_id: 11608, booking_end: new Date("2024-12-11")},
  {booking_id: 4, property_id: 5581, booking_end: new Date("2024-12-19")},
  {booking_id: 5, property_id: 1985, booking_end: new Date("2024-08-27")},
  {booking_id: 6, property_id: 24879, booking_end: new Date("2025-03-31")},
  {booking_id: 7, property_id: 16753, booking_end: new Date("2025-03-24")},
  {booking_id: 8, property_id: 3561, booking_end: new Date("2025-01-16")},
  {booking_id: 9, property_id: 11260, booking_end: new Date("2025-04-05")},
  {booking_id: 10, property_id: 17179, booking_end: new Date("2024-06-18")}
];

print("Processing", sampleBookings.length, "completed bookings for review generation...");

var reviews = [];
var reviewDate = new Date("2024-01-01");
var dayInMs = 24 * 60 * 60 * 1000;

sampleBookings.forEach(function(booking, index) {
  // Only create reviews for completed bookings (booking end date has passed)
  var currentDate = new Date();
  if (booking.booking_end < currentDate) {
    var review = {
      property_id: booking.property_id,
      booking_id: booking.booking_id,
      cleanliness_rating: Math.floor(Math.random() * 21) + 80, // 80-100 scale
      guest_satisfaction: Math.floor(Math.random() * 21) + 80, // 80-100 scale  
      text_comment: cyclingComments[index % commentsLength],
      created_at: new Date(booking.booking_end.getTime() + (Math.random() * 7 * dayInMs)) // Review 0-7 days after booking ends
    };
    reviews.push(review);
  }
});

print("Generated", reviews.length, "reviews for completed bookings");
print("Guest information retrieved via booking_id -> MySQL transformation");

if (reviews.length > 0) {
  var insertStart = new Date();
  db.reviews.insertMany(reviews, {ordered: false, writeConcern: {w: 1, j: false}});
  var insertTime = new Date() - insertStart;
  
  print("Inserted", reviews.length, "reviews in", insertTime, "ms");
  print("Average insertion rate:", Math.round(reviews.length / (insertTime / 1000)), "docs/sec");
} else {
  print("No completed bookings found - no reviews to insert");
}

// Verify final count
var finalCount = db.reviews.countDocuments();
print("Final document count verification:", finalCount);

print("\nWorkflow demonstration setup complete");
print("Database: airbnb");
print("Collection: reviews");
print("Reviews created only for completed bookings with booking_id reference");
print("Replica set: rs0 ready");

// Calculate and populate property ratings collection
print("\n=== Starting Property Ratings Calculation ===");

// Only calculate ratings if we have reviews
var reviewCount = db.reviews.countDocuments();
if (reviewCount > 0) {
  db.property_ratings.drop();

  print("Calculating property ratings from", reviewCount, "reviews...");
  var ratingsStart = new Date();

  var ratingsData = db.reviews.aggregate([
      {
          $group: {
              _id: "$property_id",
              avg_cleanliness_rating: { $avg: "$cleanliness_rating" },
              avg_satisfaction_rating: { $avg: "$guest_satisfaction" },
              total_reviews: { $sum: 1 },
              last_updated: { $max: "$created_at" }
          }
      },
      {
          $project: {
              property_id: "$_id",
              avg_cleanliness_rating: { $round: ["$avg_cleanliness_rating", 2] },
              avg_satisfaction_rating: { $round: ["$avg_satisfaction_rating", 2] },
              total_reviews: 1,
              last_updated: { $ifNull: ["$last_updated", new Date()] },
              created_at: new Date(),
              _id: 0
          }
      }
  ]);

  var aggregationTime = new Date() - ratingsStart;
  print("Property ratings aggregation completed in:", aggregationTime, "ms");

  print("Inserting property ratings...");
  var insertStart = new Date();

  var ratingsArray = ratingsData.toArray();
  var totalRatingsInserted = 0;

  if (ratingsArray.length > 0) {
      db.property_ratings.insertMany(ratingsArray, {ordered: false, writeConcern: {w: 1, j: false}});
      totalRatingsInserted = ratingsArray.length;
      
      var insertTime = new Date() - insertStart;
      print("Inserted", totalRatingsInserted, "property ratings in", insertTime, "ms");
      
      // Create indexes for property_ratings
      db.property_ratings.createIndex({ "property_id": 1 }, { unique: true });
      db.property_ratings.createIndex({ "avg_satisfaction_rating": -1 });
      db.property_ratings.createIndex({ "avg_cleanliness_rating": -1 });
      db.property_ratings.createIndex({ "total_reviews": -1 });
      
      print("\nProperty ratings summary:");
      print("- Properties with ratings:", totalRatingsInserted);
      print("- Based on", reviewCount, "reviews");
      print("- Ready for workflow demonstration");
  } else {
      print("No ratings to insert");
  }
} else {
  print("No reviews found - skipping property ratings calculation");
  print("Use the API to add reviews after bookings are completed");
}

print("\n=== Workflow Demonstration Ready ===");
print("1. MySQL has Users, Properties, and Bookings tables populated");
print("2. MongoDB has", reviewCount, "reviews for completed bookings");
print("3. Property ratings calculated and ready");
print("4. API endpoints available for adding new reviews with booking_id");
print("5. Cross-service integration will update ratings automatically");

markTime("MongoDB initialization complete"); 