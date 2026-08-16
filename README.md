# LinguaQuest Backend

LinguaQuest is a gamified language-learning platform that turns vocabulary acquisition into an interactive adventure. This repository contains the backend REST API that powers both the Android and iOS mobile applications, handling authentication, game logic, AI-powered features, push notifications, and more.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [CI/CD Pipeline](#cicd-pipeline)
- [API Documentation](#api-documentation)
- [Contributors](#contributors)
- [License](#license)

---

## Features

- Email/password registration with OTP verification and password reset
- Google and Apple OAuth via Firebase Authentication
- JWT access tokens with database-backed refresh token rotation
- World-based game progression with leveled vocabulary challenges
- AI-powered image verification (multimodal) and multilingual hint generation
- Daily missions and daily reward system with streak tracking
- Achievement system with pluggable criteria resolvers
- Leaderboard rankings (daily, weekly, all-time)
- Push notifications via Firebase Cloud Messaging (FCM) with localized broadcasts
- User profile management with Cloudinary image upload
- In-app wallet and coin economy
- Server-side localization for 5 languages (EN, AR, FR, ES, DE)
- Database-driven content translation for in-game entities

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 4.1.0 |
| Build Tool | Apache Maven (with Maven Wrapper) |
| Database | MySQL (Aiven Cloud) |
| ORM | Spring Data JPA / Hibernate |
| Caching | Redis (Aiven Cloud, SSL) |
| Security | Spring Security 6, JWT (JJWT 0.13.0), BCrypt |
| OAuth | Firebase Admin SDK 9.4.3 (Google + Apple) |
| AI | Spring AI 2.0.0 (OpenCode, TripleAI / ITI Gateway) |
| Push Notifications | Firebase Cloud Messaging (FCM) |
| Email | Spring Mail (SMTP) + Gmail REST API (OAuth 2.0) |
| Media Storage | Cloudinary 1.39.0 |
| Containerization | Docker (multi-stage), Docker Compose |
| Reverse Proxy | Traefik |
| CI/CD | GitHub Actions |
| Cloud | AWS EC2 |
| Monitoring | Spring Boot Actuator, SLF4J/Logback, Dozzle |
| Validation | Jakarta Bean Validation |
| Code Generation | Lombok |

---

## Architecture

The application follows a **Layered Architecture** pattern with clear separation of concerns:

```
Client Request
    |
    v
[Traefik Reverse Proxy]
    |
    v
[Spring Security - JWT Filter]
    |
    v
[Controller Layer]  -  REST endpoints, request validation
    |
    v
[Service Layer]     -  Business logic, AI calls, notifications
    |
    v
[Repository Layer]  -  Data access via Spring Data JPA
    |
    v
[MySQL Database]
```

Cross-cutting concerns (security, exception handling, validation, async processing, logging) are applied across all layers through Spring mechanisms.

---

## Project Structure

```
src/main/java/gov/jets/iti/LinguaQuest/
|-- config/              # Configuration classes (Security, Redis, Firebase, Cloudinary, Async, Versioning)
|-- controller/          # REST controllers organized by feature
|   |-- auth/            # AuthController, OAuthController, OtpController, ForgetPasswordController
|-- service/             # Business logic services
|   |-- achievement/     # Achievement resolvers (Strategy pattern)
|   |-- mail/            # Mail providers (SMTP, Gmail API - Strategy + Template Method)
|   |-- notification/    # FCM push sender and notification lifecycle
|-- repository/          # Spring Data JPA repository interfaces
|-- entity/              # JPA entity classes (domain model)
|-- dto/                 # Data Transfer Objects organized by feature
|-- enums/               # Application enumerations
|-- exception/           # Custom exceptions and GlobalExceptionHandler
|-- security/            # JWT authentication provider and filters
|-- util/                # Utility classes (JwtUtil, constants, validators)
|-- helper/              # Application startup runners

src/main/resources/
|-- application.properties
|-- firebase-service-account.json
|-- messages/            # i18n property files (EN, AR, FR, ES, DE)
|-- images/              # Static assets (email templates)
```

---

## Prerequisites

- Java 21 (JDK)
- Maven 3.9+ (or use the included Maven Wrapper)
- MySQL 8.0+
- Redis 7+
- Docker and Docker Compose (for containerized deployment)
- Firebase project with service account credentials
- Cloudinary account
- AI provider API keys (OpenCode and/or TripleAI)

---

## Getting Started

### Local Development

1. **Clone the repository**

```bash
git clone https://github.com/<your-org>/LinguaQuest-Backend.git
cd LinguaQuest-Backend
```

2. **Set up environment variables**

Copy the `.env` file and fill in your credentials (see [Environment Variables](#environment-variables)):

```bash
cp .env.example .env
```

3. **Run the application**

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

### Docker Deployment

1. **Build and run with Docker Compose**

```bash
docker compose up -d --build
```

This starts the backend service and the Dozzle log viewer.

2. **Verify the deployment**

```bash
curl http://localhost:8080/actuator/health
```

---

## Environment Variables

Create a `.env` file in the project root with the following variables:

| Variable | Description |
|---|---|
| `DB_URL` | MySQL JDBC connection URL |
| `DB_USER` | MySQL username |
| `DB_PASSWORD` | MySQL password |
| `JWT_SECRET_KEY` | Secret key for signing JWT tokens |
| `JWT_EXPIRATION` | Access token expiration in milliseconds |
| `REFRESH_TOKEN_EXPIRATION` | Refresh token expiration in milliseconds |
| `MAIL_USERNAME` | SMTP email address |
| `MAIL_PASSWORD` | SMTP app password |
| `MAIL_PROVIDER` | Mail transport: `smtp` or `gmail-api` (default: `gmail-api`) |
| `GMAIL_CLIENT_ID` | Gmail API OAuth client ID |
| `GMAIL_CLIENT_SECRET` | Gmail API OAuth client secret |
| `GMAIL_REFRESH_TOKEN` | Gmail API OAuth refresh token |
| `GMAIL_SENDER_EMAIL` | Sender email address |
| `REDIS_URL` | Redis host |
| `REDIS_PORT` | Redis port (default: 6379) |
| `REDIS_PASSWORD` | Redis password |
| `REDIS_SSL` | Enable Redis SSL (default: false) |
| `FIREBASE_CREDENTIALS` | Firebase service account JSON (as a string) |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret |
| `OPENCODE_API_KEY` | OpenCode AI API key |
| `TRIPLEAI_API_KEY` | TripleAI / ITI Gateway API key |

---

## API Endpoints

All endpoints are prefixed with `/api/v1`. Protected routes require a `Bearer` JWT token in the `Authorization` header.

### Authentication

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/auth/register` | Register a new account | No |
| POST | `/auth/login` | Login with email/password | No |
| POST | `/auth/refresh-token` | Refresh access token | No |
| POST | `/auth/logout` | Logout and revoke refresh token | No |
| GET | `/auth/languages` | Get available languages | No |

### OAuth

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/auth/oauth/firebase` | Login with Firebase ID token (Google/Apple) | No |
| POST | `/auth/oauth/complete-profile` | Complete OAuth user profile | Yes |

### OTP and Password Reset

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/auth/otp/send` | Send OTP to email | No |
| POST | `/auth/otp/verify` | Verify OTP code | No |
| POST | `/auth/forgot-password` | Request password reset | No |

### Worlds and Game

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/worlds?difficulty=` | Get all worlds by difficulty | Yes |
| GET | `/worlds/{id}/levels` | Get levels of a world | Yes |
| POST | `/worlds/{id}/levels/{id}/start` | Start a level | Yes |
| PUT | `/worlds/{id}/levels/{id}/change-word` | Change current word | Yes |
| POST | `/worlds/{id}/levels/{id}/verify` | Verify image (multipart) | Yes |
| GET | `/worlds/{id}/levels/{id}/hint` | Get AI-generated hint | Yes |
| GET | `/worlds/continue-level` | Get continue target | Yes |

### Profile

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/profile` | Get user profile | Yes |
| PUT | `/profile` | Update profile | Yes |
| PUT | `/profile/photo` | Upload profile photo | Yes |
| PUT | `/profile/password` | Change password | Yes |
| DELETE | `/profile/photo` | Remove profile photo | Yes |

### Languages

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/languages` | Get user languages | Yes |
| POST | `/languages` | Add a target language | Yes |
| PUT | `/languages/{id}/activate` | Switch active language | Yes |
| DELETE | `/languages/{id}` | Remove a language | Yes |

### Daily Missions and Rewards

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/daily-missions` | Get today's mission | Yes |
| POST | `/daily-missions/verify` | Verify mission image | Yes |
| GET | `/daily-rewards` | Get reward tiers and status | Yes |
| POST | `/daily-rewards/claim` | Claim daily reward | Yes |

### Notifications

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/notifications` | List notifications (paginated) | Yes |
| GET | `/notifications/unread-count` | Get unread count | Yes |
| PUT | `/notifications/{id}/read` | Mark as read | Yes |
| DELETE | `/notifications/{id}` | Delete a notification | Yes |
| DELETE | `/notifications` | Delete all notifications | Yes |

### Other

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/home` | Get home screen data | Yes |
| GET | `/leaderboard` | Get leaderboard rankings | Yes |
| GET | `/achievements` | Get user achievements | Yes |
| GET | `/wallet` | Get wallet balance | Yes |
| POST | `/devices` | Register device token (FCM) | Yes |
| GET | `/gallery` | Get user photo gallery | Yes |

---

## Testing

Run the test suite:

```bash
./mvnw test
```

The project includes test starters for:
- JPA layer testing (`spring-boot-starter-data-jpa-test`)
- Security layer testing (`spring-boot-starter-security-test`)
- Web MVC layer testing (`spring-boot-starter-webmvc-test`)

---

## CI/CD Pipeline

The project uses **GitHub Actions** for continuous deployment:

1. A push to the `main` branch triggers the workflow
2. The workflow SSHs into the **AWS EC2** instance
3. Pulls the latest code from GitHub
4. Rebuilds and restarts Docker containers (`docker compose up -d --build`)
5. Prunes unused Docker images

The workflow file is located at `.github/workflows/deploy.yml`.

---

## API Documentation

The API is documented using the **OpenAPI** specification and tested with **Postman** for manual API testing and team collaboration.

---

## Contributors

<!-- Add team members below -->

| Name | GitHub |
|---|---|
| Adham Khaled | [@adhamkhaled312](https://github.com/adhamkhaled312) |
| Ahmed Abdelrahman | [@iiiAhmed](https://github.com/iiiAhmed) |
| Ahmed Khaled | [@Ahmed-Khaled-Abdelmaksod](https://github.com/Ahmed-Khaled-Abdelmaksod) |

---

## License

This project is licensed under the [MIT License](LICENSE).
