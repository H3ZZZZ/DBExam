# System Patterns

## Architecture Pattern
**Multi-Database Architecture** with NoSQL + Relational database combination for different data types and access patterns.

## MongoDB Patterns

### Replica Set Configuration
```javascript
rs.initiate({
  _id: "rs0",
  members: [
    { _id: 0, host: "mongo1:27017", priority: 2 },
    { _id: 1, host: "mongo2:27017", priority: 1 },
    { _id: 2, host: "mongo3:27017", priority: 1 }
  ]
});
```

### Review Document Schema
```javascript
{
  property_id: Number,
  cleanliness_rating: Number,    // 0-100 scale
  guest_satisfaction: Number,    // 0-100 scale  
  text_comment: String,          // Cycling through 5 predefined comments
  created_at: Date              // Sequential dates starting 2024-01-01
}
```

### Data Loading Pattern
1. **CSV Parsing**: Read entire file using `fs.readFileSync()`
2. **Validation**: Skip malformed lines, validate numeric fields
3. **Batch Processing**: Individual inserts with progress tracking
4. **Error Handling**: Graceful fallback to sample data

## Comment Cycling Pattern
Five predefined comments rotate across properties:
1. "Excellent property with great amenities!..."
2. "Good value for money. The place was clean..."
3. "Amazing experience! Everything was exactly..."
4. "Nice place but could use some minor improvements..."
5. "Outstanding property! Beautiful location..."

**Formula**: `commentIndex = propertyIndex % 5`

## Docker Orchestration Patterns

### Health Check Strategy
- **MongoDB**: `echo 'db.runCommand({ ping: 1 }).ok' | mongosh --quiet`
- **MySQL**: `mysqladmin ping -h localhost -u airbnb_user -pairbnb_pass`
- **Retry Logic**: 30 retries with 5s intervals

### Initialization Sequence
1. Wait for all database nodes to be healthy
2. Initialize replica set (with duplicate handling)
3. Wait for primary election and write capability
4. Load data from CSV
5. Create indexes for query optimization

## Data Consistency Patterns
- **Exact Ratings**: No random variation, preserves CSV accuracy
- **Date Progression**: Sequential dates (1 day apart per property)
- **Comment Determinism**: Same property always gets same comment type 