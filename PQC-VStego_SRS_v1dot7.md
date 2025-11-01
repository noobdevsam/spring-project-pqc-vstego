# Software Requirements Specification: PQC-VStego

Version: 1.7  
Date: January 26, 2025

---

## 1. Introduction

### 1.1 Purpose

This document provides the updated Software Requirements Specification (SRS) for "PQC-VStego". It reflects a consolidated architecture where the previously separate PQC and AES (symmetric) cryptographic operations are combined into a single **Cryptography Service**. The Orchestration Service coordinates the operations of the Cryptography Service, File Service, and Video Processing Service to perform the end-to-end encode/decode workflows.

**Important architectural changes:**
- The existing `pqc-service` will be **converted** to `cryptography-service` and extended to provide AES-256-GCM operations in addition to the existing PQC operations (CRYSTALS-Kyber KEM and CRYSTALS-Dilithium DSA).
- The Video Processing Service remains the steganography worker and continues to perform **LSB-1 embedding/extraction only**; it has no cryptography responsibility.
- The Orchestration Service now calls the unified Cryptography Service for all cryptographic operations.

### 1.2 Scope

This SRS update covers the architecture and API-level changes necessary to merge PQC and AES functionality into the Cryptography Service and to update orchestration flows accordingly. It does not change the front-end contract nor the steganography implementation responsibility in the Video Processing Service.

**In-Scope:**
- Consolidation of PQC (KEM, DSA) and AES-GCM operations into a single Cryptography Service.
- Updated Orchestration Service workflows that call Cryptography Service for all encryption, signing, and key management operations.
- Preservation of Video Processing Service responsibility (LSB-1 steganography only).
- All other functionality remains as defined in previous versions.

**Out-of-Scope:**
- Changes to authentication mechanisms (GitHub OAuth2 remains).
- Changes to video format support (MP4/H.264 only).
- Changes to storage mechanisms (MongoDB GridFS remains).

## 2. Overall Description

### 2.1 Product Perspective

PQC-VStego is a microservice-based web application. The key architectural change in v1.7 is the consolidation of cryptographic operations:

**Previous Architecture (v1.6):**
- Separate PQC Service (for post-quantum operations)
- Separate AES Service (for symmetric encryption)
- Orchestration Service coordinated both services

**New Architecture (v1.7):**
- **Unified Cryptography Service** (conversion of pqc-service) handles:
  - PQC Key Generation (Kyber KEM + Dilithium DSA)
  - PQC Encapsulation/Decapsulation (Kyber)
  - PQC Digital Signatures (Dilithium)
  - AES-256-GCM Encryption/Decryption
  - AES-256-GCM Key Generation
- Orchestration Service calls only the Cryptography Service for all crypto operations
- Video Processing Service remains unchanged (LSB-1 steganography only)

### 2.2 System Architecture (Updated)

The system consists of the following microservices:

1. **Config Service** (Spring Cloud Config)
2. **Discovery Service** (Spring Cloud Netflix Eureka)
3. **React Client** (Frontend SPA)
4. **API Gateway** (Spring Cloud Gateway)
5. **User Service** (Spring Boot)
6. **Cryptography Service** (Spring Boot) - **CONSOLIDATED SERVICE**
   - Replaces both pqc-service and aes-service
   - Handles all cryptographic operations:
     - PQC: CRYSTALS-Kyber (KEM) and CRYSTALS-Dilithium (DSA)
     - Symmetric: AES-256-GCM
   - Stateless service
   - Stores only public key records (if persistence is required)
7. **Orchestration Service** (Spring Boot) - **UPDATED**
   - Coordinates calls to Cryptography Service, File Service, and Video Processing Service
   - Assembles/disassembles payloads
   - Publishes job messages to Kafka
8. **File Service** (Spring Boot)
   - GridFS-backed file storage
9. **Video Processing Service** (Spring Boot)
   - Steganography worker only (LSB-1 embedding/extraction using ffmpeg)
   - No cryptographic operations
10. **Kafka** (Asynchronous job bus)

### 2.3 Product Functions (High-Level)

- **User Authentication:** GitHub OAuth2 (unchanged)
- **Key Management:** Provided by Cryptography Service (PQC key pair generation and storage)
- **Cryptographic Operations:** All encryption, signing, and key management handled by Cryptography Service
- **Encoding/Decoding Orchestration:** Orchestration Service uses Cryptography Service to assemble payloads (ENCODE) and disassemble (DECODE)
- **Video Steganography:** Video Processing Service embeds/extracts binary payloads
- **Video Capacity Estimation:** Provided by Video Processing Service (unchanged)
- **File Management:** File Service handles GridFS storage (unchanged)

### 2.4 Constraints (Relevant Updates)

- **C-2 (Cryptography):** Continue to use Bouncy Castle. All cryptographic operations (PQC and symmetric) are now provided by the Cryptography Service:
  - PQC KEM: CRYSTALS-Kyber
  - PQC DSA: CRYSTALS-Dilithium
  - Symmetric: AES-256-GCM
- **C-3 (Video Tooling):** ffmpeg remains required by Video Processing Service only.
- **C-6 (Storage):** Large files stored in GridFS. Carrier videos should not be persisted by Orchestration or Cryptography services; Video Processing Service streams from File Service as needed.
- **C-10 (Configuration):** Cryptography Service must be stateless and use Config Service for configuration.
- **C-12 (Security):** All inter-service communication must be over HTTPS with JWT authentication.

### 2.5 Assumptions and Dependencies

- **A-1:** Config Service and Discovery Service are running.
- **A-2:** Kafka and MongoDB instances are running.
- **A-3:** ffmpeg is installed on Video Processing Service instances.
- **A-4:** GitHub OAuth2 application is configured.
- **A-5:** Valid keystores for HTTPS/TLS are available.
- **A-6 (NEW):** Cryptography Service has Bouncy Castle dependencies configured for both PQC and AES algorithms.

---

## 3. Backend Specific Requirements (Updated)

### 3.1 Functional Requirements (Backend)

#### FR-B-1: User Authentication & Authorization (Unchanged)

- **FR-B-1.1 - FR-B-1.5:** Remain as previously defined (Gateway + User Service).

#### FR-B-2: Cryptography Service (REPLACES PQC & AES Services)

The Cryptography Service is the **consolidated service** that provides all cryptographic operations. It replaces both the pqc-service and aes-service from v1.6.

##### PQC Key Management Operations:

- **FR-B-2.1:** The Cryptography Service shall provide `POST /api/v1/keys/generate` to generate a PQC key pair (both KEM and DSA key pairs). The response includes:
  - `kemPublicKey`, `kemPrivateKey` (CRYSTALS-Kyber)
  - `dsaPublicKey`, `dsaPrivateKey` (CRYSTALS-Dilithium)
  - **Note:** Private keys are returned only in the POST response; they must never be persisted by the service.

- **FR-B-2.2:** The Cryptography Service shall provide `POST /api/v1/keys/set` for an authenticated user to associate their **public** KEM and DSA keys with their user profile. Only public keys are persisted; private keys are never stored.

- **FR-B-2.3:** The Cryptography Service shall provide `GET /api/v1/users/{userId}/keys` to fetch the active public keys (KEM and DSA) for a specific user.

##### PQC KEM Operations:

- **FR-B-2.4:** The Cryptography Service shall provide KEM encapsulation/decapsulation endpoints:
  - `POST /api/v1/kem/encapsulate` — Accepts recipient's public KEM key (or recipient userId to fetch their key) and returns:
    - `encapsulatedKey` (byte array/Base64): The encapsulated key data
    - `sharedSecret` (optional, depending on design): The derived shared symmetric key
  - `POST /api/v1/kem/decapsulate` — Accepts:
    - `encapsulatedKey` (byte array/Base64)
    - `recipientPrivateKey` (byte array/Base64)
    - Returns the decapsulated shared secret (AES key)

##### PQC Digital Signature Operations:

- **FR-B-2.5:** The Cryptography Service shall provide digital signature endpoints:
  - `POST /api/v1/signature/sign` — Signs arbitrary byte payloads with a provided private signing key (Dilithium). Accepts:
    - `data` (byte array/Base64): Data to sign
    - `privateKey` (byte array/Base64): Sender's private DSA key
    - Returns: `signature` (byte array/Base64)
  - `POST /api/v1/signature/verify` — Verifies signatures using a public DSA key. Accepts:
    - `data` (byte array/Base64): Original data
    - `signature` (byte array/Base64): Signature to verify
    - `publicKey` (byte array/Base64): Sender's public DSA key
    - Returns: `boolean` (true if valid, false otherwise)

##### AES-256-GCM Operations:

- **FR-B-2.6:** The Cryptography Service shall provide symmetric encryption operations:
  - `POST /api/v1/aes/generate-key` — Generates a random AES-256-GCM key. Returns:
    - `aesKey` (byte array/Base64): 256-bit AES key
  - `POST /api/v1/aes/encrypt` — Encrypts data using AES-256-GCM. Accepts:
    - `data` (byte array/Base64 or InputStream): Data to encrypt
    - `aesKey` (byte array/Base64): AES key
    - Returns:
      - `ciphertext` (byte array/Base64)
      - `iv` (byte array/Base64): Initialization Vector
      - `authTag` (byte array/Base64): Authentication tag
  - `POST /api/v1/aes/decrypt` — Decrypts AES-GCM ciphertext. Accepts:
    - `ciphertext` (byte array/Base64)
    - `aesKey` (byte array/Base64)
    - `iv` (byte array/Base64)
    - `authTag` (byte array/Base64)
    - Returns: `plaintext` (byte array/Base64 or OutputStream)

##### Service Properties:

- **FR-B-2.7:** The Cryptography Service shall be **stateless** and must never persist private keys. The service must validate all inputs and return appropriate HTTP status codes on errors.

- **FR-B-2.8:** The Cryptography Service may optionally persist public keys in a database for user key management:
  - Database: `cryptography_db` (MongoDB)
  - Collection: `public_keys`
  - Schema: `{_id, userId, kemPublicKey, dsaPublicKey, isActive, createdAt}`

**Migration Notes:**
- The existing `pqc-service` repository will be renamed/refactored to `cryptography-service`
- Existing PQC endpoints will be preserved under their current paths
- New AES endpoints will be added under `/api/v1/aes/*`
- The service will continue to use Bouncy Castle for all cryptographic operations

#### FR-B-3: Capacity Estimation (Unchanged)

- Orchestration Service proxies capacity estimation requests to Video Processing Service.
- Video Processing Service uses ffprobe to calculate capacity based on video dimensions and frame count.

#### FR-B-4: Encoding Job (UPDATED: Use Cryptography Service)

**Input:** (Unchanged)
- POST /api/v1/encode (multipart/form-data)
- Fields: `carrierFile` (MP4 InputStream), `secretFile` (binary InputStream), `recipientUserId`, `senderPrivateKey` (.txt file with private key)

**FR-B-4.2 (Cryptographic Orchestration — UPDATED):**

The Orchestration Service performs the following **synchronous** steps when creating an encode job:

1. **Generate AES Key:**
   - Call `POST /api/v1/aes/generate-key` on Cryptography Service
   - Receive `aesKey`

2. **Encrypt Secret File:**
   - Call `POST /api/v1/aes/encrypt` on Cryptography Service
   - Input: `secretFile` stream, `aesKey`
   - Receive: `ciphertext`, `iv`, `authTag`
   - Combine as `encryptedData = ciphertext + iv + authTag`

3. **Sign Encrypted Data:**
   - Call `POST /api/v1/signature/sign` on Cryptography Service
   - Input: `encryptedData`, `senderPrivateKey` (from request)
   - Receive: `signature`

4. **Encapsulate AES Key:**
   - Call `POST /api/v1/kem/encapsulate` on Cryptography Service
   - Input: `recipientUserId` (to fetch recipient's public KEM key) or `recipientPublicKemKey`
   - Receive: `encapsulatedKey`

5. **Assemble Final Payload:**
   ```
   Final Payload Structure:
   [Metadata Header]
   + [Sender's Public DSA Key]
   + [Encapsulated AES Key]
   + [PQC Signature]
   + [Encrypted Data (ciphertext + iv + authTag)]
   ```

**FR-B-4.3 (Job Creation & Dispatch — UPDATED):**

- Create job record in `jobs_db` with status: `PENDING`
- Return `jobId` immediately (HTTP 202 Accepted)
- Publish Kafka message to topic `job.request.encode` containing:
  - `jobId`
  - `carrierFileGridFsId` (or streaming indication)
  - `finalBinaryPayload` (assembled payload from step 5)

**FR-B-4.4 (Steganography — Unchanged):**

Video Processing Service consumes the encode job:
1. Streams carrier video from File Service
2. Embeds the provided `finalBinaryPayload` into video LSBs (LSB-1) using ffmpeg + streaming
3. Streams final stego-video to File Service
4. Publishes `job.completion` message with `outputFileGridFsId`

**FR-B-4.5 (Completion — Unchanged):**

Orchestration Service updates job status to `COMPLETED` with `outputFileGridFsId`, or `FAILED` with error message.

#### FR-B-5: Decoding Job (UPDATED: Use Cryptography Service)

**FR-B-5.1 (Input):** (Unchanged)
- POST /api/v1/decode (multipart/form-data)
- Fields: `stegoFile` (MP4 InputStream), `recipientPrivateKey` (.txt file with private key)

**FR-B-5.2 (Job Creation & Extraction — Unchanged):**

- Orchestration Service creates job in `jobs_db` with status: `PENDING`
- Returns `jobId` (HTTP 202 Accepted)
- Publishes `job.request.decode` to Kafka containing `jobId` and `stegoFileGridFsId`
- Video Processing Service extracts raw binary payload from video using LSB-1 extraction
- Publishes `job.completion` to Kafka with `jobId` and `extractedPayload`

**FR-B-5.3 (Verification & Decryption — UPDATED):**

Upon receiving completion message with `extractedPayload`, Orchestration Service:

1. **Parse Payload:**
   ```
   Parse extractedPayload into:
   - Metadata Header
   - Sender's Public DSA Key
   - Encapsulated AES Key
   - PQC Signature
   - Encrypted Data (ciphertext + iv + authTag)
   ```

2. **Verify Signature:**
   - Call `POST /api/v1/signature/verify` on Cryptography Service
   - Input: `encryptedData`, `signature`, `senderPublicDsaKey`
   - If verification fails → Mark job as `FAILED` (SIGNATURE_INVALID)

3. **Decapsulate AES Key:**
   - Call `POST /api/v1/kem/decapsulate` on Cryptography Service
   - Input: `encapsulatedKey`, `recipientPrivateKey` (from decode request)
   - Receive: `aesKey`
   - If decapsulation fails → Mark job as `FAILED` (DECRYPTION_FAILED)

4. **Decrypt Secret File:**
   - Call `POST /api/v1/aes/decrypt` on Cryptography Service
   - Input: `ciphertext`, `iv`, `authTag`, `aesKey`
   - Receive: `decryptedSecretFile` stream

5. **Store Result:**
   - Stream `decryptedSecretFile` to File Service (GridFS)
   - Receive `outputFileGridFsId`

**FR-B-5.4 (Completion — Unchanged):**

Update job to status: `COMPLETED` with `outputFileGridFsId`, or `FAILED` with error message.

#### FR-B-6: File Management (Unchanged)

- **FR-B-6.1:** GET /api/v1/job/{jobId}/status returns job status
- **FR-B-6.2:** GET /api/v1/job/{jobId}/download streams output file from File Service
- **FR-B-6.3:** GET /api/v1/files lists all output files owned by the user
- **FR-B-6.4:** DELETE /api/v1/files/{fileId} deletes an output file from GridFS

### 3.2 Non-Functional Requirements (Backend)

**NFR-B-1 (Performance):** All encode/decode APIs must be asynchronous, returning a jobId in < 200ms.

**NFR-B-2 (Concurrency):** All Spring Boot services must use Java 25 Virtual Threads.

**NFR-B-3 (Streaming):** All file handling must use streams to minimize memory usage.

**NFR-B-4 (Security - UPDATED):**
- Cryptography Service must **never persist private keys**
- Private keys must only be accepted via ephemeral, secure API calls for signing/decapsulation operations
- All inter-service calls must be performed over **HTTPS** with JWT authentication
- The Orchestration Service must handle private keys only in memory for the duration of the cryptographic operation

**NFR-B-5 (Scalability):** Video Processing Service and Cryptography Service must be horizontally scalable.

**NFR-B-6 (File Cleanup):** Configurable scheduled job shall periodically delete old files from GridFS.

**NFR-B-7 (Configuration):** All services must retrieve configuration from central Config Service.

**NFR-B-8 (Resilience - UPDATED):**
- Services must use Discovery Service for service location
- Orchestration and other services should implement retry logic for transient errors when calling Cryptography Service
- Care must be taken to avoid exposing sensitive cryptographic material in logs or error messages

### 3.3 Data Requirements (Updated)

**cryptography_db (Optional - if public keys are persisted):**
- Collection: `public_keys`
- Schema:
  ```
  {
    _id: ObjectId,
    userId: String (indexed),
    kemPublicKey: String (Base64),
    dsaPublicKey: String (Base64),
    isActive: Boolean,
    createdAt: Timestamp
  }
  ```
- **Note:** Private keys are NEVER stored. The Cryptography Service may maintain this collection or delegate public key storage to User Service depending on deployment choices.

**jobs_db (Unchanged):**
- Collection: `jobs`
- Schema:
  ```
  {
    _id: ObjectId,
    jobId: String (UUID, indexed),
    jobType: String (ENCODE|DECODE),
    status: String (PENDING|PROCESSING|COMPLETED|FAILED),
    senderUserId: String,
    recipientUserId: String (for ENCODE),
    createdAt: Timestamp,
    completedAt: Timestamp,
    storage: {
      inputFileGridFsId: String,
      secretFileGridFsId: String (ENCODE only),
      outputFileGridFsId: String
    },
    errorMessage: String
  }
  ```

**files_db (Unchanged):**
- GridFS collections: `fs.files`, `fs.chunks`
- Metadata includes: `ownerUserId`, `uploadDate`, `_contentType`

---

## 4. Front-End Specific Requirements (Unchanged)

The front-end interface remains unchanged from v1.6. The consolidation of cryptographic services is transparent to the client.

### 4.1 Functional Requirements (Front-End)

- **FR-F-1:** Authentication (Login/Logout with GitHub, display user info)
- **FR-F-2:** Key Management Page (Generate, download, copy, and set public keys)
- **FR-F-3:** Public Key Directory Page (List and search users to get their public keys)
- **FR-F-4:** Encoding Page (Upload carrier, secret, and private key files; select recipient; see capacity; submit job)
- **FR-F-5:** Decoding Page (Upload stego-video and private key file; submit job)
- **FR-F-6:** Job Status Page (Poll for job status, provide download/error messages)
- **FR-F-7:** File Management Page (List and delete user-owned output files)

### 4.2 Non-Functional Requirements (Front-End)

- **NFR-F-1 (Technology):** React v18+ SPA
- **NFR-F-2 (Security):** Private keys must **never** be stored in persistent browser storage (localStorage, sessionStorage, cookies)
- **NFR-F-3 (Usability):** Clear visual feedback for all operations
- **NFR-F-4 (Warning):** UI must warn users that private keys will be transmitted once to Orchestration Service for immediate use and will not be stored

---

## 5. Interface Requirements

### 5.1 REST API Endpoints (External - Unchanged from Client Perspective)

| Method | Endpoint                     | Description                                            |
|:-------|:-----------------------------|:-------------------------------------------------------|
| GET    | /login/github                | Initiates GitHub OAuth2 flow                           |
| GET    | /api/v1/users                | Lists all registered users                             |
| POST   | /api/v1/keys/generate        | Generates PQC key pairs (KEM + DSA)                    |
| POST   | /api/v1/keys/set             | (Auth) Associates public keys with user profile        |
| GET    | /api/v1/users/{userId}/keys  | Gets active public keys for a user                     |
| POST   | /api/v1/estimate             | (Auth) Accepts MP4 stream, returns capacity            |
| POST   | /api/v1/encode               | (Auth) Submits encoding job, returns jobId             |
| POST   | /api/v1/decode               | (Auth) Submits decoding job, returns jobId             |
| GET    | /api/v1/job/{jobId}/status   | (Auth) Returns job status                              |
| GET    | /api/v1/job/{jobId}/download | (Auth) Downloads output file for completed job         |
| GET    | /api/v1/files                | (Auth) Lists all output files owned by user            |
| DELETE | /api/v1/files/{fileId}       | (Auth) Deletes output file from GridFS                 |

### 5.2 Internal REST API Endpoints (Cryptography Service)

**PQC Key Management:**
```
POST   /api/v1/keys/generate        → Returns KEM & DSA key pairs (public + private)
POST   /api/v1/keys/set             → Stores public keys for authenticated user
GET    /api/v1/users/{userId}/keys  → Fetches public keys for a user
```

**PQC KEM Operations:**
```
POST   /api/v1/kem/encapsulate      → Encapsulates AES key with recipient's public KEM key
POST   /api/v1/kem/decapsulate      → Decapsulates to recover AES key
```

**PQC Digital Signature Operations:**
```
POST   /api/v1/signature/sign       → Signs data with private DSA key
POST   /api/v1/signature/verify     → Verifies signature with public DSA key
```

**AES-256-GCM Operations:**
```
POST   /api/v1/aes/generate-key     → Generates random AES-256 key
POST   /api/v1/aes/encrypt          → Encrypts data with AES-GCM
POST   /api/v1/aes/decrypt          → Decrypts AES-GCM ciphertext
```

### 5.3 Kafka Topics (Internal Communication)

**Topic: `job.request.encode`**
- Published by: Orchestration Service
- Consumed by: Video Processing Service
- Payload:
  ```json
  {
    "jobId": "uuid",
    "carrierFileGridFsId": "gridfs-id",
    "finalBinaryPayload": "base64-encoded-payload"
  }
  ```

**Topic: `job.request.decode`**
- Published by: Orchestration Service
- Consumed by: Video Processing Service
- Payload:
  ```json
  {
    "jobId": "uuid",
    "stegoFileGridFsId": "gridfs-id"
  }
  ```

**Topic: `job.completion`**
- Published by: Video Processing Service
- Consumed by: Orchestration Service
- Payload:
  ```json
  {
    "jobId": "uuid",
    "status": "COMPLETED|FAILED",
    "outputFileGridFsId": "gridfs-id",
    "extractedPayload": "base64-encoded-payload (for DECODE)",
    "errorMessage": "error-description (if FAILED)"
  }
  ```

### 5.4 External Interfaces

- **ffmpeg CLI:** Required by Video Processing Service for video frame extraction/assembly
- **ffprobe CLI:** Required by Video Processing Service for video metadata extraction
- **GitHub OAuth2 API:** Required by API Gateway for authentication
- **MongoDB:** Required by all services for data persistence
- **Kafka:** Required for asynchronous job messaging
- **Redis:** Required by API Gateway for session management

---

## 6. Migration Guide (v1.6 → v1.7)

### 6.1 Service Changes

**Cryptography Service (formerly pqc-service):**

1. **Rename/Refactor:**
   - Rename module from `pqc-service` to `cryptography-service`
   - Update `spring.application.name` to `cryptography-service`

2. **Add AES Dependencies:**
   - Ensure Bouncy Castle includes AES-GCM support
   - Add AES key generation utilities

3. **Implement New AES Endpoints:**
   - `/api/v1/aes/generate-key`
   - `/api/v1/aes/encrypt`
   - `/api/v1/aes/decrypt`

4. **Preserve Existing PQC Endpoints:**
   - Keep all existing `/api/v1/keys/*` endpoints
   - Keep all existing `/api/v1/kem/*` endpoints (if implemented)
   - Keep all existing `/api/v1/signature/*` endpoints (if implemented)

5. **Update Configuration:**
   - Rename config file from `pqc-service.yml` to `cryptography-service.yml`
   - Ensure database name is updated (if using dedicated DB): `cryptography_db` or `pqc_keys_db`

**Orchestration Service:**

1. **Update Service Client:**
   - Replace separate `pqcServiceRestClient` and `aesServiceRestClient` beans with single `cryptographyServiceRestClient`
   - Update service URL configuration: `services.cryptography-service-url`

2. **Update Encode Workflow:**
   - Refactor to call Cryptography Service for all crypto operations
   - Update method signatures to use unified crypto client

3. **Update Decode Workflow:**
   - Refactor to call Cryptography Service for all crypto operations
   - Update method signatures to use unified crypto client

**Video Processing Service:**
- No changes required (steganography operations remain unchanged)

**Discovery Service:**
- Update service registry to recognize `cryptography-service` instead of `pqc-service` and `aes-service`

**API Gateway:**
- Update routes (if any direct routes to crypto service):
  ```yaml
  - id: cryptography-service-route
    uri: "lb://CRYPTOGRAPHY-SERVICE"
    predicates:
      - Path=/api/v1/keys/**, /api/v1/kem/**, /api/v1/signature/**, /api/v1/aes/**
  ```

### 6.2 Configuration Updates

**config-server-service/src/main/resources/configurations/cryptography-service.yml:**
```yaml
server:
    port: 8083

spring:
    threads:
        virtual:
            enabled: true

    data:
        mongodb:
            uri: mongodb://${CRYPTOGRAPHY_SERVICE_MONGO_HOST:localhost}:${CRYPTOGRAPHY_SERVICE_MONGO_PORT:27017}
            database: ${CRYPTOGRAPHY_SERVICE_MONGO_DB:cryptography_db}

    security:
        oauth2:
            resourceserver:
                jwt:
                    jwk-set-uri: http://${GATEWAY_SERVICE_HOST:localhost}:${GATEWAY_SERVICE_PORT:8080}/.well-known/jwks.json

eureka:
    client:
        service-url:
            defaultZone: http://${EUREKA_SERVER_HOST:localhost}:8761/eureka/
```

**orchestration-service.yml (Update):**
```yaml
services:
    file-service-url: "http://file-service"
    video-service-url: "http://video-processing-service"
    cryptography-service-url: "http://cryptography-service"  # NEW
```

### 6.3 Testing Checklist

- [ ] Cryptography Service starts successfully with new name
- [ ] All existing PQC endpoints still function (generate keys, set keys, get keys)
- [ ] New AES endpoints function correctly (generate key, encrypt, decrypt)
- [ ] Orchestration Service successfully calls Cryptography Service for all operations
- [ ] Encode workflow completes successfully end-to-end
- [ ] Decode workflow completes successfully end-to-end
- [ ] Video Processing Service steganography operations unchanged
- [ ] No private keys are persisted anywhere in the system
- [ ] All services register correctly with Discovery Service
- [ ] API Gateway routes requests correctly to Cryptography Service

---

## 7. Appendix

### 7.1 Payload Structure (Detailed)

**Final Payload for Embedding (v1.7):**
```
[4 bytes: Payload Length]
[4 bytes: Metadata Version]
[256 bytes: Sender's Public DSA Key (Dilithium)]
[Variable: Encapsulated AES Key (Kyber)]
[Variable: PQC Signature (Dilithium)]
[Variable: Encrypted Data (ciphertext)]
[12 bytes: AES-GCM IV]
[16 bytes: AES-GCM Auth Tag]
```

### 7.2 Error Codes

| Code | Description                          |
|:-----|:-------------------------------------|
| 1001 | Invalid video format                 |
| 1002 | Insufficient carrier capacity        |
| 1003 | Invalid recipient user ID            |
| 1004 | Signature verification failed        |
| 1005 | Decapsulation failed                 |
| 1006 | Decryption failed                    |
| 1007 | Payload extraction failed            |
| 1008 | Cryptography service unavailable     |
| 1009 | Invalid private key format           |
| 1010 | Invalid public key format            |

### 7.3 Glossary

**Cryptography Service:** The consolidated microservice that handles all cryptographic operations (PQC and symmetric) in the PQC-VStego system.

**LSB-1:** Least Significant Bit steganography method using the lowest bit of each byte for data embedding.

**Hybrid Encryption:** Cryptographic scheme combining asymmetric (PQC KEM) and symmetric (AES-GCM) encryption for performance and security.

---

**End of SRS v1.7**
