# Project Brief: Airbnb Analytics Platform

## Project Overview
Database exam project implementing a multi-database analytics platform for Airbnb property data analysis using MongoDB replica sets and MySQL.

## Core Requirements
- **Multi-database architecture**: MongoDB (reviews/NoSQL) + MySQL (structured data)
- **MongoDB replica set**: 3-node cluster with automatic failover
- **Data population**: ~25,500 Airbnb properties from CSV with generated reviews
- **Review system**: Cycling comment patterns to demonstrate rating consistency
- **Container orchestration**: Docker Compose setup with health checks
- **Web interfaces**: Mongo Express + phpMyAdmin for database management

## Key Components
1. **MongoDB Cluster**: 3-node replica set (mongo1, mongo2, mongo3)
2. **MySQL Database**: Structured property and host data
3. **Initialization Services**: Automated data loading from CSV
4. **Management UIs**: Web-based database administration tools

## Data Sources
- `cleaned_airbnb_data.csv`: 25,500+ Airbnb properties with ratings and metadata
- Generated reviews: One per property, cycling through 5 comment types

## Success Criteria
- Functional MongoDB replica set with automated initialization
- All CSV properties loaded with corresponding reviews
- Consistent data structure across databases
- Accessible via web management interfaces
- Demonstrable rating analysis capabilities 