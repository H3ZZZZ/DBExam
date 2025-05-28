// MongoDB Cluster Test Script
// Run with: docker exec mongo1 mongosh test-cluster.js

// Test 1: Check replica set status
print("=== MongoDB Cluster Status ===");
try {
    const status = rs.status();
    print("Replica Set Name:", status.set);
    print("Active Members:", status.members.length);
    
    status.members.forEach(member => {
        print(`- ${member.name}: ${member.stateStr} (health: ${member.health})`);
    });
} catch (e) {
    print("Error checking replica set status:", e);
}

print("\n=== Testing Write Operations ===");

// Test 2: Insert data on primary
use('airbnb_analytics');
const testData = [
    { property_id: 1, name: "Cozy Apartment", price: 120, location: "Copenhagen" },
    { property_id: 2, name: "Modern Loft", price: 200, location: "Aarhus" },
    { property_id: 3, name: "Beach House", price: 350, location: "Skagen" }
];

try {
    const result = db.properties.insertMany(testData);
    print("Inserted", result.insertedIds.length, "documents on primary");
} catch (e) {
    print("Error inserting data:", e);
}

// Test 3: Check data replication
print("\n=== Testing Read Operations ===");
try {
    const count = db.properties.countDocuments();
    print("Documents in properties collection:", count);
    
    const sample = db.properties.findOne();
    print("Sample document:", JSON.stringify(sample, null, 2));
} catch (e) {
    print("Error reading data:", e);
}

// Test 4: Show cluster configuration for scaling demo
print("\n=== Scaling Information ===");
print("Current cluster has 3 nodes (mongo1, mongo2, mongo3)");
print("To add mongo4: Add to docker-compose.yml and run:");
print("rs.add('mongo4:27017')");

print("\n=== Connection Examples ===");
print("Primary (read/write): mongodb://localhost:27017/airbnb_analytics?replicaSet=rs0");
print("Secondary1 (read): mongodb://localhost:27018/airbnb_analytics?replicaSet=rs0&readPreference=secondary");
print("Secondary2 (read): mongodb://localhost:27019/airbnb_analytics?replicaSet=rs0&readPreference=secondary");

print("\nCluster test complete!"); 