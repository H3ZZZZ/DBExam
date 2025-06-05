# DBExam

## Application Stack

### Overview
The DBExam project is a comprehensive multi-database analytics platform designed for Airbnb property data analysis. It utilizes both MongoDB and MySQL to manage diverse data types and operations, all orchestrated through Docker.

For a quick start guide [click here](DOCKER_README.md).

### Technologies Used
- **MongoDB 6.0**: Handles reviews and property ratings with a 3-node replica set for high availability.
- **MySQL 8.0**: Manages structured property data with stored procedures for business logic.
- **Spring Boot 3.x**: Backend API that integrates with MongoDB for data operations.
- **Docker & Docker Compose**: Facilitates container orchestration for consistent environments and easy deployment.
- **Swagger/OpenAPI 3**: Provides API documentation and testing interface.
- **Maven**: Manages build automation and dependencies.

### Architecture
- **Dual Database Architecture**: Integrates MongoDB for NoSQL operations and MySQL for structured data, with cross-service integration.
- **REST API**: Offers endpoints for CRUD operations on reviews, properties, bookings, and user management, documented via Swagger UI.
- **Containerized Environment**: All services are containerized and managed by Docker Compose.

### Key Components
- **MongoDB Cluster**: A replica set with three nodes ensuring data consistency and availability.
- **MySQL Database**: Stores structured data with support for complex queries and stored procedures.
- **Spring Boot API**: Facilitates interaction with the databases, providing endpoints for data manipulation and retrieval.

### Development Environment
- **OS**: Windows 10/11 & MacOS
- **Java**: JDK 17

### Deployment
- **Docker Compose**: Used for setting up and managing the application stack, including database initialization and service orchestration.