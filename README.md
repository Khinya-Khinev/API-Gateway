# API Gateway

Single entry point for the WMS frontend. Routes incoming requests to backend
services, handles the OAuth2 Authorization Code flow as an OAuth2 Client, and
relays access tokens to downstream services.

## Tech Stack

**Core**
- Java 25
- Spring Boot 4
- Spring Cloud Gateway

**Security**
- Spring Security + OAuth2 Client

**Infrastructure**
- Redis (Session storage)


## Project Structure

```
src/main/java/com/waregang/api_gateway/
├── config/           # Route definitions, CORS, filters
├── docs/             # Swagger
└── properties/
```