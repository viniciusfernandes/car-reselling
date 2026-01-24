# Project Overview

I am a used car reseller in Brazil, and my daily routine consists of researching cars with higher liquidity in the 
market and negotiating a price below the current market value so that I can have a profit margin when reselling. 
My research sources are the internet and personal contacts. After purchasing a car, I carry out a personal inspection, 
take it to a mechanic for a check-up, and perform any necessary maintenance so that it is in full working condition 
before resale. Once all repairs are completed, I send it to a multi-brand partner dealership so they can showcase the 
car and start negotiating with potential buyers. If the negotiation is successful and the sale is completed, 
the partner dealer will receive a commission at the end of the month.
------------------------------------------------------------------------

# Tech Stack Constraints

## Backend
- **Java 21** (already installed)
- **Gradle 8.7** (compatible with Spring 3.x and already installed)
- **Spring Boot 3.x**
    - Enable **Java Virtual Threads** for handling and response to HTTP request faster
- **Maven Central** as the primary repository
- **MySQL** database
- **Unit Testing**: JUnit 5 + Jupiter + Mockito
- **Docker** version 27.5.1
- **Docker Compose** version 2.29.2
- **Liquibase** change log to track all the database updates

## Frontend
- **Node.js** (already installed)
- **npm** (already installed)
- **React 18** + **TailwindCSS**
- **TypeScript**
- **Axios** for API communication

------------------------------------------------------------------------

# Architectural Rules
## Launch Guidelines
- Create a docker-compose with 2 services:
  - car-reselling: to run the frontend and backend in a single one application
  - mysql: to run the database

## Backend Guidelines
- Java components have to fit SOLID principles
- Component layers have to respect, whenever it is possible, the Clean Architecture patterns
- Avoid cyclical components at all costs
- All repository interface must follow the naming method convention:
  - public User saveUser(...){} -> to saving an entity user
  - public User findUserByXXX(...){} -> when searching user by some parameters, just in case as XXX
  - public User updateUser(...){} -> to update an entity user
  - public void deleteUser(...){} -> to user an entity user
- Any repository component which connects to a database by JDBC driver must implements native query to speed up performance
- Service Classes:
  - Any service class must implement a service interface according to Clean Architecture and each interface must have a uppercase letter I, for example:
  - ```{=java}
    public interface IUserService{...}
    ```
- All Java classes must reside under the base package:
    `br.com.carreselling`
- Follow **Domain-Driven Design (DDD)** principles:
    - No anemic models
    - Use repositories, root entities, and aggregates appropriately
- Domain-related elements:
    - All **model classes** and **repository interfaces** under
        `/domain`
- Business logic placement:
    - All **business rules** must be encapsulated in **service
        components**
- Use case structure:
    - Example: For a domain entity `User` with the feature **save new
        user**, the folder hierarchy must be:

```{=html}
<!-- -->
```
    br/com/carreselling/usecase/user/save/
        endpoint   -> all endpoints here
        contract   -> request and response DTO classes
        mapping    -> mapping utilities (request → domain, domain → response)

- Endpoint naming convention:
    - Example: `SaveUserEndpoint` →
        `br/com/carreselling/usecase/user/save/endpoint`
- HTTP responses must follow the structure:

``` json
{ "data": <response JSON> }
```

- Supports both objects and arrays
- **Global error handling**:
    - All errors must be managed by a centralized handler
    - Error responses must return a **list of error messages**
- **Database Creation or Update (DLL/DML)**: any database script like create or update any table structure must be 
added and versioned in the change log file according to the rules:
  - Each id must match the patter author-yyyyMMdd-sequence number -> vinicius-20251120-01
    - The script must be under a SQL tag or section
      - ``` yaml
        changeSet:
        id: vinicius-20250910-01
        author: vinicius
        changes:
          - sql:
              sql: |
                -- Create users table with inline indexes/constraints
                CREATE TABLE IF NOT EXISTS users (
                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                  email VARCHAR(255) NOT NULL UNIQUE,
                  password VARCHAR(255) NULL,
                  full_name VARCHAR(255) NOT NULL,
                  phone VARCHAR(50) NULL,
                  profile_picture VARCHAR(512) NULL,
                  status VARCHAR(32) NOT NULL,
                  email_verified BOOLEAN NOT NULL DEFAULT FALSE,
                  email_verification_token VARCHAR(255) NULL,
                  email_verification_token_expiry DATETIME NULL,
                  password_reset_token VARCHAR(255) NULL,
                  password_reset_token_expiry DATETIME NULL,
                  external_provider_id VARCHAR(255) NULL,
                  external_provider VARCHAR(32) NULL,
                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  updated_at DATETIME NULL,
                  KEY idx_users_email (email)
                );
      ```


## Frontend Guidelines

- Respect the folder hierarchy:

```{=html}
<!-- -->
```
    /app
        /image
        /component        -> general reusable components
            /label        -> main components + CSS files
            /input
            /date
            /table
        /page
            /login
            /user
            /order

------------------------------------------------------------------------

# Summary

This project enforces **clean architecture**, **DDD principles**, and
**strict folder conventions** for both backend and frontend.
The goal is to ensure scalability, maintainability, and consistency
across the entire authentication ecosystem.
