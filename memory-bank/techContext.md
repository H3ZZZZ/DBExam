# Technical Context

## Technology Stack
- **MongoDB 6.0**: NoSQL database for reviews and unstructured data
- **MySQL 8.0**: Relational database for structured property data
- **Docker & Docker Compose**: Container orchestration
- **Mongo Express**: MongoDB web interface
- **phpMyAdmin**: MySQL web interface

## Development Environment
- **OS**: Windows 10/11
- **Shell**: PowerShell
- **Container Platform**: Docker Desktop

## Database Architecture

### MongoDB Setup
- **Replica Set**: `rs0` with 3 nodes
- **Ports**: 27017 (mongo1), 27018 (mongo2), 27019 (mongo3)
- **Health Checks**: MongoDB ping commands with retry logic
- **Initialization**: Automated via `mongo_init` service

### MySQL Setup
- **Port**: 3306
- **Authentication**: mysql_native_password
- **Data Loading**: CSV mounted to `/var/lib/mysql-files/`

## Container Configuration
- **Network**: `airbnb_network` (bridge driver)
- **Volumes**: Persistent data storage for each database
- **Dependencies**: Health check-based service dependencies
- **Restart Policies**: `always` for databases, `no` for init services

## File Structure
```
├── docker-compose.yml
├── cleaned_airbnb_data.csv
├── mongo/init/init.js
├── sql/init/
└── memory-bank/
```

## Key Technical Decisions
1. **Replica Set Priority**: mongo1 (priority=2) as preferred primary
2. **CSV Mounting**: Direct file access for both MongoDB and MySQL
3. **Error Handling**: Fallback data if CSV loading fails
4. **Batch Processing**: Progress tracking every 1000 records 