# Docker Deployment Guide

This guide explains how to run the complete Airbnb Analytics Platform using Docker.

## Complete Application Stack

The Docker setup includes:

### Databases
- **MySQL 8.0**: Relational data (users, properties, bookings) with stored procedures
- **MongoDB Replica Set**: 3-node cluster (mongo1, mongo2, mongo3) for reviews and analytics

### Application Services  
- **Spring Boot Backend**: REST API with cross-database integration
- **Mongo Express**: MongoDB web interface (port 8081)
- **phpMyAdmin**: MySQL web interface (port 8082)

### Architecture Features
- **Cross-database stored procedures**: MySQL validation + MongoDB analytics
- **Replica set with automatic failover**: High availability MongoDB cluster
- **Health checks**: All services monitored with health endpoints
- **Auto-initialization**: Databases populated with 25,500+ properties

## Quick Start

### Prerequisites
- Docker & Docker Compose installed
- At least 4GB RAM available for containers
- Ports 3306, 8080, 8081, 8082, 27017-27019 available

### 1. Start the Complete Stack
Open a terminal in the root of the project and run either:
```bash
# Build and start all services
docker-compose up --build

# Or run in detached mode
docker-compose up --build -d
```

### 2. Wait for Initialization
The system will automatically:
1. Start MySQL and MongoDB replica set
2. Initialize data from `cleaned_airbnb_data.csv`
3. Create stored procedures and indexes
4. Build and start the Spring Boot backend

**Total startup time**: ~2-3 minutes

### 3. Access the Services

#### Spring Boot API
- **Base URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

#### Database Interfaces
- **phpMyAdmin**: http://localhost:8082 (MySQL)
- **Mongo Express**: http://localhost:8081 (MongoDB)

### 4. Test the Integration

#### Example API Calls
```bash
# Get reviews for a property with cross-database guest enrichment
curl "http://localhost:8080/api/reviews/property/1/with-guest-info"

# Validate booking using MySQL stored procedures
curl "http://localhost:8080/api/reviews/validate-booking/1/1"

# Add review with automatic rating recalculation
curl -X POST "http://localhost:8080/api/reviews/add-with-rating-update" \
  -d "propertyId=1&bookingId=1&cleanlinessRating=95&satisfactionRating=90&comment=Great property!"

# Cross-database architecture demonstration
curl "http://localhost:8080/api/reviews/cross-database-demo/1"
```

## Service Details

### Spring Boot Backend Configuration
- **Profile**: `docker` (uses Docker service names)
- **MySQL**: Connects to `mysql:3306` 
- **MongoDB**: Connects to replica set `mongo1:27017,mongo2:27017,mongo3:27017`
- **Health Checks**: Actuator endpoints enabled

### Database Credentials
```
MySQL:
- Host: localhost:3306
- Database: airbnb_analytics  
- User: airbnb_user
- Password: airbnb_pass

MongoDB:
- Host: localhost:27017-27019
- Database: airbnb_reviews
- Replica Set: rs0
- No authentication
```

## Development Workflow

### Rebuilding Only the Backend
```bash
# Rebuild just the Spring Boot service
docker-compose up --build backend

# Or rebuild without cache
docker-compose build --no-cache backend
docker-compose up backend
```

### Viewing Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f mysql
docker-compose logs -f mongo1
```

### Stopping the Stack
```bash
# Stop all services
docker-compose down

# Stop and remove volumes (deletes all data)
docker-compose down -v
```

## Monitoring & Health

### Service Health Status
```bash
# Check all container status
docker-compose ps

# Backend health endpoint
curl http://localhost:8080/actuator/health

# MongoDB replica set status
docker exec mongo1 mongosh --eval "rs.status()"

# MySQL status
docker exec airbnb_mysql mysqladmin status -u airbnb_user -pairbnb_pass
```

### Resource Usage
```bash
# Monitor resource usage
docker stats

# View container details
docker-compose top
```

## Troubleshooting

### Common Issues

**Backend fails to start**: 
- Check if databases are healthy: `docker-compose ps`
- View backend logs: `docker-compose logs backend`
- Ensure ports 3306 and 27017 are accessible

**MongoDB replica set issues**:
- Wait for `mongo_init` to complete successfully
- Check mongo1 logs: `docker-compose logs mongo1`
- Verify replica set: `docker exec mongo1 mongosh --eval "rs.status()"`

**MySQL connection errors**:
- Verify MySQL health: `docker-compose logs mysql`
- Check credentials in `application-docker.properties`

### Reset Everything
```bash
# Nuclear option - removes all containers, networks, and volumes
docker-compose down -v --remove-orphans
docker system prune -f
docker-compose up --build
```

## Production Considerations

For production deployment, consider:
- **Security**: Add authentication, change default passwords
- **Persistence**: Use named volumes or external storage
- **Load Balancing**: Add reverse proxy (nginx/traefik)
- **Monitoring**: Add Prometheus/Grafana stack
- **Backup**: Automated database backups
- **SSL**: Enable HTTPS with certificates

## Architecture Benefits

This Docker setup demonstrates:
- **Polyglot Persistence**: MySQL + MongoDB working together
- **Microservices**: Containerized, independently scalable services  
- **High Availability**: MongoDB replica set with automatic failover
- **Modern DevOps**: Infrastructure as code with docker-compose
- **Cross-Database Integration**: Real stored procedure architecture
- **Academic Value**: Complete demonstration of modern database architectures 
