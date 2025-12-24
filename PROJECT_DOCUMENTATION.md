# Huawei Health Connect - Project Documentation

## Overview

This is a **modern Compose Multiplatform health tracking application** that integrates with ** Android's Health Connect and Apple's HealthKit APIs ** to read device health data and synchronize it with a backend server. The app provides personalized health recommendations, comprehensive health metrics tracking, and a beautiful Material Design 3 user interface.

---

## Table of Contents

1. [Technology Stack](#technology-stack)
2. [Architecture](#architecture)
3. [Project Structure](#project-structure)
4. [Core Features](#core-features)
5. [Data Flow](#data-flow)
6. [Layer Details](#layer-details)
7. [Health Data Categories](#health-data-categories)
8. [State Management](#state-management)
9. [Navigation](#navigation)
10. [Theming & Design System](#theming--design-system)
11. [API Integration](#api-integration)
12. [Key Patterns](#key-patterns)

---

## Technology Stack

### Core
| Technology              | Purpose |
|-------------------------|---------|
| **Jetpack Compose**     | Modern declarative UI framework |
| **Material Design 3**   | Design system with dynamic theming |
| **Jetpack navigation3** | Type-safe navigation |
| **DataStore**           | Modern encrypted preferences storage |

### Dependency Injection
| Technology | |
|------------||
| **Koin**   |  |

### Networking
| Technology                | Purpose |
|---------------------------|---------|
| **Ktor**                  
| **Kotlinx-serialization** | JSON serialization/deserialization |

### Asynchronous Processing
| Technology | Purpose |
|------------|---------|
| **Kotlin Coroutines** | Async/await operations |
| **Kotlin Flow** | Reactive stream handling |

### Health & Location

## Architecture

The application follows **Clean Architecture** principles with **MVVM+ (MVI principle with ViewModel. Presentation layer sends action to viewModel and receives state from it)** pattern for the presentation layer:
### Key Principles

1. **Unidirectional Data Flow**: Data flows from external sources → data layer → domain layer → presentation layer
2. **Dependency Inversion**: Domain layer defines interfaces, data layer provides implementations
3. **Single Responsibility**: Each class has one reason to change
4. **Immutable State**: All UI states are immutable data classes or sealed classes

---

## Core Features

### 1. Authentication
- **Login**: Email/password authentication with validation
- **Registration**: Multi-field form with password strength indicator
- **Session Management**: Token-based auth stored in encrypted DataStore
- **Logout**: Secure session termination

### 2. Health Data Integration

### 3. Data Synchronization
- **Upload to Backend**: Sync health data to server
- **Progress Tracking**: Real-time sync status updates
- **Error Handling**: Comprehensive error recovery

### 4. Personalized Recommendations
- **AI-Powered**: Backend-generated health recommendations
- **Dynamic Refresh**: Pull new recommendations on demand

### 5. Location Services
- **GPS Integration**: Current location display
- **Permission Handling**: Graceful fallback if denied

---

## Data Flow (example)

### Login Flow
```
User Input → LoginScreen → AuthViewModel.login()
    → LoginUseCase (validates input)
    → AuthRepository.login()
    → AuthRemoteDataSource.login()
    → AuthApi.login() [HTTP POST]
    → Response Processing
    → TokenManager.saveUserData() [Local Storage]
    → StateFlow<LoginUiState> Update
    → UI Recomposition → Navigation to Dashboard
```

## API Integration
{
"schemes": [
"http",
"https"
],
"swagger": "2.0",
"info": {
"description": "API for Dockify backend.",
"title": "Dockify Backend API",
"contact": {

    },
    "version": "1.0"
},
"host": "",
"basePath": "",
"paths": {
"/api/v1/hospitals/nearest": {
"post": {
"description": "Returns a list of nearest hospitals to the provided location",
"consumes": [
"application/json"
],
"produces": [
"application/json"
],
"tags": [
"Hospitals"
],
"summary": "Get Nearest Hospitals",
"parameters": [
{
"description": "Nearest hospitals request",
"name": "request",
"in": "body",
"required": true,
"schema": {
"$ref": "#/definitions/entity.NearestHospitalsRequest"
}
}
],
"responses": {
"200": {
"description": "OK",
"schema": {
"type": "array",
"items": {
"$ref": "#/definitions/entity.Location"
}
}
},
"400": {
"description": "invalid request",
"schema": {
"$ref": "#/definitions/entity.ErrorMessage"
}
}
}
}
},
"/api/v1/location/nearest": {
"post": {
"description": "Retrieves users nearest to given coordinates within a radius.",
"consumes": [
"application/json"
],
"produces": [
"application/json"
],
"tags": [
"location"
],
"summary": "Get nearest users",
"parameters": [
{
"description": "Nearest users request",
"name": "request",
"in": "body",
"required": true,
"schema": {
"$ref": "#/definitions/entity.NearestUsersRequest"
}
}
],
"responses": {
"200": {
"description": "OK",
"schema": {
"type": "array",
"items": {
"$ref": "#/definitions/entity.NearestUsersResponse"
}
}
},
"204": {
"description": "no content"
},
"400": {
"description": "Bad Request",
"schema": {
"$ref": "#/definitions/entity.ErrorMessage"
}
},
"500": {
"description": "Internal Server Error",
"schema": {
"$ref": "#/definitions/entity.ErrorMessage"
}
}
}
}
},
"/api/v1/login": {
"post": {
"description": "Authenticate user and return user information",
"consumes": [
"application/json"
],
"produces": [
"application/json"
],
"tags": [
"User"
],
"summary": "User login",
"parameters": [
{
"description": "User login payload",
"name": "request",
"in": "body",
"required": true,
"schema": {
"$ref": "#/definitions/entity.UserLoginRequest"
}
}
],
"responses": {
"200": {
"description": "authenticated user",
"schema": {
"type": "object",
"additionalProperties": true
}
},
"400": {
"description": "invalid request",
"schema": {
"$ref": "#/definitions/entity.ErrorMessage"
}
},
"401": {
"description": "invalid email or password",
"schema": {
"$ref": "#/definitions/entity.ErrorMessage"
}
},
"500": {
"description": "internal server error",
"schema": {
"$ref": "#/definitions/entity.ErrorMessage"
}
}
}
}
},
"/api/v1/metrics": {
"get": {
"description": "Retrieve health metrics for a given user by query parameter user_id",
"consumes": [
"application/json"
],
"produces": [
"application/json"
],
"tags": [
"Metrics"
],
"summary": "Get health metrics",
"parameters": [
{
"type": "integer",
"description": "User ID",
"name": "user_id",
"in": "query",
"required": true
}
],
"responses": {
"200": {
"description": "list of health metrics",
"schema": {
"type": "array",
"items": {
"type": "object"
}
}
},
"400": {
"description": "invalid user_id or missing parameter",
"schema": {
"$ref": "#/definitions/entity.ErrorMessage"
}
},
"500": {
"description": "failed to get health metrics",
"schema": {
"$ref": "#/definitions/entity.ErrorMessage"
}
}
}
},
"post": {
"description": "Create health metrics for a user",
"consumes": [
"application/json"
],
"produces": [
"application/json"
],
"tags": [
"Metrics"
],
"summary": "Create health metrics",
"parameters": [
{
"description": "Health metrics payload",
"name": "request",
"in": "body",
"required": true,
"schema": {
"$ref": "#/definitions/entity.HealthMetricsRequest"
}
}
],
"responses": {
"201": {
"description": "status message",
"schema": {
"type": "object",
"additionalProperties": {
"type": "string"
}
}
},
"400": {
"description": "invalid request",
"schema": {
"$ref": "#/definitions/entity.ErrorMessage"
}
},
"500": {
"description": "failed to create health metrics",
"schema": {
"$ref": "#/definitions/entity.ErrorMessage"
}
}
}
}
},
"/api/v1/recommendation": {
"get": {
"description": "Returns a recommendation string",
"produces": [
"application/json"
],
"tags": [
"Recommendation"
],
"summary": "Get Recommendation",
"responses": {
"200": {
"description": "OK",
"schema": {
"$ref": "#/definitions/entity.RecommendationResponse"
}
}
}
}
},
"/api/v1/register": {
"post": {
"description": "Create a new user account",
"consumes": [
"application/json"
],
"produces": [
"application/json"
],
"tags": [
"User"
],
"summary": "Register a new user",
"parameters": [
{
"description": "User registration payload",
"name": "request",
"in": "body",
"required": true,
"schema": {
"$ref": "#/definitions/entity.UserRegisterRequest"
}
}
],
"responses": {
"201": {
"description": "created user id",
"schema": {
"$ref": "#/definitions/entity.CreatedUserResponse"
}
},
"400": {
"description": "invalid request",
"schema": {
"$ref": "#/definitions/entity.ErrorMessage"
}
},
"500": {
"description": "failed to register user",
"schema": {
"$ref": "#/definitions/entity.ErrorMessage"
}
}
}
}
},
"/health": {
"get": {
"description": "Returns the live status of the service",
"produces": [
"application/json"
],
"tags": [
"Health"
],
"summary": "Health Check (Live)",
"responses": {
"200": {
"description": "health",
"schema": {
"type": "string"
}
}
}
}
}
},
"definitions": {
"entity.CreatedUserResponse": {
"type": "object",
"properties": {
"user_id": {
"type": "integer"
}
}
},
"entity.ErrorMessage": {
"type": "object",
"properties": {
"message": {
"type": "string"
}
}
},
"entity.HealthMetric": {
"type": "object",
"properties": {
"metric_type": {
"type": "string"
},
"metric_value": {
"type": "string"
}
}
},
"entity.HealthMetricsRequest": {
"type": "object",
"properties": {
"location": {
"$ref": "#/definitions/entity.Location"
},
"metrics": {
"type": "array",
"items": {
"$ref": "#/definitions/entity.HealthMetric"
}
},
"user_id": {
"type": "integer"
}
}
},
"entity.Location": {
"type": "object",
"properties": {
"latitude": {
"type": "number",
"example": 55.755825
},
"longitude": {
"type": "number",
"example": 37.617396
}
}
},
"entity.NearestHospitalsRequest": {
"type": "object",
"properties": {
"latitude": {
"type": "number",
"example": 55.755825
},
"longitude": {
"type": "number",
"example": 37.617396
},
"radius": {
"description": "in meters",
"type": "integer",
"example": 5000
}
}
},
"entity.NearestUsersRequest": {
"type": "object",
"properties": {
"latitude": {
"type": "number",
"example": 55.755825
},
"longitude": {
"type": "number",
"example": 37.617396
},
"radius": {
"description": "in meters",
"type": "integer",
"example": 5000
},
"user_id": {
"type": "integer"
}
}
},
"entity.NearestUsersResponse": {
"type": "object",
"properties": {
"location": {
"$ref": "#/definitions/entity.Location"
},
"user_id": {
"type": "integer"
}
}
},
"entity.RecommendationResponse": {
"type": "object",
"properties": {
"recommendation": {
"type": "string"
}
}
},
"entity.UserLoginRequest": {
"type": "object",
"properties": {
"email": {
"type": "string"
},
"password": {
"type": "string"
}
}
},
"entity.UserRegisterRequest": {
"type": "object",
"properties": {
"email": {
"type": "string"
},
"first_name": {
"type": "string"
},
"last_name": {
"type": "string"
},
"password": {
"type": "string"
},
"username": {
"type": "string"
}
}
}
}
}
