# Software Requirements Specification: PQC-VStego

Version: 1.7  
Date: 2025-11-01

---

## 1. Introduction

### 1.1 Purpose

This document provides a detailed Software Requirements Specification (SRS) for "PQC-VStego," a full-stack web application. The system's primary purpose is to embed a secret binary file within a carrier video file (MP4) using steganography.

The system will enforce **user management via GitHub OAuth2**, allowing users to manage their Post-Quantum Cryptography (PQC) keys and discover the public keys of other users. All secret data will be secured using a **hybrid PQC scheme** (Key Encapsulation + Symmetric Encryption) and authenticated using a **PQC Digital Signature**.

This version (1.7) reflects a consolidated architecture where all cryptographic operations (PQC and AES) are handled by a single **Cryptography Service**. The Orchestration Service coordinates all workflows.

All large files will be stored in **MongoDB GridFS** and processed using **InputStreams** to minimize memory load. The system will be built as a microservice-based backend on **Java 25**, **Spring Boot**, and **Kafka**, with a **React** front-end. This architecture will be managed by a **Config Server** and **Discovery Service**.

### 1.2 Scope

This project covers the complete full-stack system.

*   **In-Scope:**
    *   A client-side Single Page Application (SPA) built in **React**.
    *   User authentication via **GitHub OAuth2**.
    *   A user-discoverable public key directory.
    *   Unified cryptographic operations (PQC KEM, PQC DSA, AES-GCM) provided by a single Cryptography Service.
    *   Video capacity estimation.
    *   Asynchronous encoding (hiding) and decoding (extracting) of binary files.
    *   **MongoDB GridFS** for all large file storage.
    *   **Configurable, scheduled file cleanup** from GridFS.
    *   A microservice architecture with asynchronous job processing via Kafka, managed by a **Config Service** and **Discovery Service**.
    *   Streaming processing of file uploads (via InputStream).
*   **Out-of-Scope:**
    *   Any user authentication provider *other than* GitHub.
    *   Support for any video format other than MP4 (H.264 codec).
    *   Direct embedding of raw text (users must provide a .txt file).

### 1.3 Definitions, Acronyms, and Abbreviations

| Term                 | Definition                                                                                            |
|:---------------------|:------------------------------------------------------------------------------------------------------|
| **PQC**              | Post-Quantum Cryptography. Cryptographic algorithms secure against quantum computers.                 |
| **AES-GCM**          | Advanced Encryption Standard in Galois/Counter Mode. A modern symmetric authenticated encryption cipher. |
| **KEM**              | Key Encapsulation Mechanism. An algorithm used to securely transport a symmetric key (e.g., CRYSTALS-Kyber). |
| **DSA**              | Digital Signature Algorithm. An algorithm to verify data authenticity (e.g., CRYSTALS-Dilithium).     |
| **LSB-1**            | Least Significant Bit. The steganography method using the lowest-order bit of each byte.           |
| **GridFS**           | A specification for MongoDB for storing and retrieving large files, such as videos.                   |
| **OAuth2**           | An open standard for access delegation, used here for GitHub login.                                   |
| **API**              | Application Programming Interface.                                                                    |
| **SPA**              | Single Page Application.                                                                              |
| **SRS**              | Software Requirements Specification.                                                                  |

---

## 2. Overall Description

### 2.1 Product Perspective

PQC-VStego is a full-stack web application. It consists of a React SPA (client) that provides the user interface and a headless backend (server) built on a microservice architecture. The React client communicates with the backend's API Gateway to perform all operations, which are tied to authenticated user accounts.

### 2.2 System Architecture (Updated for v1.7)

The system is composed of several distinct microservices, each with its own dedicated database, adhering to the **database-per-service** pattern. The entire ecosystem is managed by a Config Service and a Discovery Service.

1.  **Config Service (Spring Cloud Config):**
    *   Provides centralized, externalized configuration for all other microservices.
2.  **Discovery Service (Spring Cloud Netflix Eureka):**
    *   Provides service registration and discovery, allowing services to find each other dynamically.
3.  **React Client (Front-End):**
    *   The user-facing SPA. Handles UI, state management, and API calls to the API Gateway.
4.  **API Gateway (Spring Cloud Gateway):**
    *   The single, authenticated entry point for all client requests. Handles GitHub OAuth2 login, issues internal JWTs, and routes requests.
5.  **User Service (Spring Boot):**
    *   Handles user profile creation and retrieval based on GitHub authentication.
    *   **Database:** `users_db` (MongoDB). Stores user profiles (GitHub ID, username, avatar).
6.  **Cryptography Service (Spring Boot) - REPLACES PQC & AES Services:**
    *   Handles all cryptographic operations: PQC key generation, PQC signing/verification, PQC encapsulation/decapsulation, and AES-GCM encryption/decryption. This service is a conversion of the former `pqc-service`.
    *   **Database:** `cryptography_db` (MongoDB). Stores user public keys.
7.  **Orchestration Service (Spring Boot) - UPDATED:**
    *   Handles the primary business logic. It coordinates calls to the `cryptography-service` and `file-service` to prepare payloads, then publishes jobs to Kafka for the `video-processing-service`.
    *   **Database:** `jobs_db` (MongoDB). Stores job metadata and status.
8.  **File Service (Spring Boot):**
    *   Manages all file persistence. Provides an internal API for uploading/downloading file streams to/from GridFS.
    *   **Database:** `files_db` (MongoDB with **GridFS**).
9.  **Video Processing Service (Spring Boot) - UPDATED:**
    *   The specialized steganography worker. It consumes jobs from Kafka, embeds a given binary payload into a video (or extracts it), and communicates with the File Service. It performs **no cryptographic operations**.

### 2.3 Product Functions

*   **User Authentication:** Allow users to log in/out via GitHub.
*   **Key Management:** Allow users to generate PQC key pairs (KEM + DSA) and associate their public keys with their profile via the Cryptography Service.
*   **Public Key Directory:** Allow users to browse a list of all registered users and copy their public keys.
*   **Encoding:** Allow an authenticated user to hide a secret file within a video, destined for another registered user.
*   **Decoding:** Allow a user to extract and verify a secret file from a stego-video.
*   **File Management:** Allow users to view and delete their job-related files.

### 2.4 Constraints

*   **C-1 (Platform):** Backend: **Java 25** / **Spring Boot 3.x+**. Front-end: **React**.
*   **C-2 (Cryptography):** Must use **Bouncy Castle**. KEM: **CRYSTALS-Kyber**. DSA: **CRYSTALS-Dilithium**. Symmetric: **AES-256-GCM**. All operations are consolidated in the `cryptography-service`.
*   **C-3 (Video Tooling):** Must use the **ffmpeg** CLI tool for video processing (only in the `video-processing-service`).
*   **C-4 (Database):** Must use a **database-per-service** pattern. All databases shall be **MongoDB**.
*   **C-5 (Messaging):** All asynchronous inter-service communication *must* use **Kafka**.
*   **C-6 (Storage):** All large binary files *must* be stored in **MongoDB GridFS**.
*   **C-7 (Streaming):** All video file uploads *must* be processed as **InputStreams** to minimize memory.
*   **C-8 (File Format):** Shall only accept **MP4 (H.264 codec)**.
*   **C-9 (Secret Format):** Shall only accept a **binary file** as the secret.
*   **C-10 (Configuration):** All microservices (except Discovery) must be stateless and retrieve their configuration from the central **Config Service**.
*   **C-11 (Discovery):** All microservices must register with the **Discovery Service** on startup.

### 2.5 Assumptions and Dependencies

*   **A-1:** The **Config Service** and **Discovery Service** are running and accessible.
*   **A-2:** A Kafka cluster and all MongoDB instances are running and accessible.
*   **A-3:** ffmpeg CLI is installed on all `video-processing-service` instances.
*   **A-4:** A GitHub OAuth2 application (Client ID, Client Secret) has been configured.

---

## 3. Backend Specific Requirements

### 3.1 Functional Requirements (Backend)

#### FR-B-1: User Authentication & Management

*   **FR-B-1.1:** The User Service must handle user profile creation/updates based on authenticated GitHub data forwarded by the API Gateway.
*   **FR-B-1.2:** The API Gateway must implement the "Login with GitHub" OAuth2 flow and generate internal JWTs for authenticated sessions.
*   **FR-B-1.3:** The User Service shall provide an endpoint `GET /api/v1/users` to list all registered users (username, avatar, userId).

#### FR-B-2: Cryptography Service (Replaces PQC & AES services)

*   **FR-B-2.1 (Key Generation):** The Cryptography Service shall provide `POST /api/v1/keys/generate` to generate a new PQC key pair (both Kyber KEM and Dilithium DSA keys).
*   **FR-B-2.2 (Set Public Key):** It shall provide `POST /api/v1/keys/set` for an authenticated user to associate their public keys with their profile. This is stored in `cryptography_db`.
*   **FR-B-2.3 (Get Public Key):** It shall provide `GET /api/v1/users/{userId}/keys` to fetch the active public keys for a specific user.
*   **FR-B-2.4 (AES & PQC Operations):** It shall provide internal, secure endpoints for the Orchestration Service to perform:
    *   AES-GCM encryption/decryption.
    *   PQC (Dilithium) signing/verification.
    *   PQC (Kyber) encapsulation/decapsulation.

#### FR-B-3: Capacity Estimation

*   **FR-B-3.1:** The Orchestration Service shall provide `POST /api/v1/estimate` that accepts an MP4 file InputStream.
*   **FR-B-3.2:** This request shall be proxied to the **Video Processing Service**, which will pipe the stream to `ffprobe` to get frame count and dimensions without saving a temporary file.
*   **FR-B-3.3:** The endpoint shall return the calculated capacity in bytes.

#### FR-B-4: Encoding Job (Updated Architecture)

*   **FR-B-4.1 (Input):** The Orchestration Service shall provide `POST /api/v1/encode` (multipart/form-data) that accepts:
    1.  `carrierFile` (MP4 InputStream).
    2.  `secretFile` (binary InputStream).
    3.  `recipientUserId`.
    4.  `senderPrivateKey` (as a `.txt` file containing the Base64 private key).
*   **FR-B-4.2 (Cryptographic Orchestration):** The **Orchestration Service** shall perform the following synchronous steps:
    1.  Generate a random, single-use AES-256-GCM key.
    2.  Call the **Cryptography Service** to encrypt the `secretFile` stream using the AES key, receiving the `encryptedData`.
    3.  Call the **Cryptography Service** to create a digital signature of the `encryptedData` using the sender's private DSA key.
    4.  Call the **Cryptography Service** to encapsulate the AES key using the recipient's public KEM key.
    5.  Assemble the final binary payload:
        `[Metadata Header] + [Sender's Public DSA Key] + [Encapsulated AES Key] + [PQC Signature] + [Encrypted Data]`
        (The header must define the length of each subsequent section).
*   **FR-B-4.3 (Job Creation & Dispatch):**
    1.  Create a job document in `jobs_db` (status: "PENDING").
    2.  Return a `jobId` (HTTP 202 Accepted).
    3.  Publish a `job.request.encode` message to Kafka. The message will contain the `jobId`, the `carrierFile` stream/ID, and the final binary `payload` created in the previous step.
*   **FR-B-4.4 (Steganography):** The **Video Processing Service**, upon consuming the job, shall:
    1.  Fetch the carrier video stream (from the message or File Service).
    2.  Pipe the stream to `ffmpeg` to extract frames.
    3.  Embed the binary `payload` from the Kafka message into the LSB-1 of the frames.
    4.  Pipe the modified frames to `ffmpeg` to create the final stego-video, streaming the output *directly* to the File Service.
*   **FR-B-4.5 (Completion):** The Video Processing service publishes a `job.completion` message. The Orchestration Service consumes this, updates the job in `jobs_db` to "COMPLETED", and stores the `outputFileGridFsId`.

#### FR-B-5: Decoding Job (Updated Architecture)

*   **FR-B-5.1 (Input):** The Orchestration Service shall provide `POST /api/v1/decode` that accepts:
    1.  `stegoFile` (MP4 InputStream).
    2.  `recipientPrivateKey` (as a `.txt` file with the Base64 private key).
*   **FR-B-5.2 (Job Creation & Extraction):**
    1.  The Orchestration Service creates a job in `jobs_db`, stores the `stegoFile` in GridFS, returns a `jobId` (HTTP 202), and publishes a `job.request.decode` message to Kafka.
    2.  The **Video Processing Service** consumes the job, streams the stego-video to `ffmpeg` to extract the LSBs, re-assembles the full binary payload, and publishes a `job.completion` message to Kafka containing the `jobId` and the raw `extractedPayload`.
*   **FR-B-5.3 (Decryption & Verification):** The **Orchestration Service**, upon consuming the completion message, shall:
    1.  Parse the `extractedPayload` into its components (Header, Sender's Public Key, EncapsulatedKey, Signature, EncryptedData).
    2.  Call the **Cryptography Service** to **verify** the Signature against the EncryptedData. If it fails, set job status to FAILED (SIGNATURE_INVALID).
    3.  Call the **Cryptography Service** to **decapsulate** the AES key using the recipient's private KEM key. If it fails, set job status to FAILED (DECRYPTION_FAILED).
    4.  If both succeed, call the **Cryptography Service** to **decrypt** the EncryptedData with the recovered AES key.
*   **FR-B-5.4 (Completion):** Stream the final decrypted file to the File Service (GridFS) and update the job to "COMPLETED".

#### FR-B-6: File Management

*   **FR-B-6.1 (Status):** `GET /api/v1/job/{jobId}/status` shall return the job status from `jobs_db`.
*   **FR-B-6.2 (Download):** `GET /api/v1/job/{jobId}/download` shall retrieve the `outputFileGridFsId` from the job and stream the file from the File Service.
*   **FR-B-6.3 (List):** `GET /api/v1/files` shall return a list of all files in GridFS associated with the authenticated user.
*   **FR-B-6.4 (Delete):** `DELETE /api/v1/files/{fileId}` shall delete a file from GridFS, provided the authenticated user is the owner.

### 3.2 Non-Functional Requirements (Backend)

*   **NFR-B-1 (Performance):** All encode/decode APIs must be asynchronous, returning a `jobId` in < 200ms.
*   **NFR-B-2 (Concurrency):** All Spring Boot services must use **Java 25 Virtual Threads** (`spring.threads.virtual.enabled=true`).
*   **NFR-B-3 (Streaming):** All file handling *must* use `InputStream` and `OutputStream` to process data as streams, never loading a full file into service memory.
*   **NFR-B-4 (Security):** All API endpoints must be secured. PQC private keys must never be stored by any backend service.
*   **NFR-B-5 (Scalability):** The Video Processing Service and Cryptography Service must be horizontally scalable.
*   **NFR-B-6 (File Cleanup):**
    *   **NFR-B-6.1:** A scheduled job (`@Scheduled`) shall run periodically to delete files from GridFS.
    *   **NFR-B-6.2:** The job shall delete files older than a configurable retention period (e.g., `pqcstego.cleanup.retention-days=30`).
    *   **NFR-B-6.3:** The cleanup job must be toggleable via a configuration property (e.g., `pqcstego.cleanup.enabled=false`).
*   **NFR-B-7 (Configuration):** All service-specific configurations must be managed in the central **Config Service**.
*   **NFR-B-8 (Resilience):** Services must use the **Discovery Service** to find and communicate with each other.

### 3.3 Data Requirements (Backend)

*   **`users_db` (users collection):**
    *   `_id`, `githubId` (String, Indexed), `username` (String), `avatarUrl` (String), `createdAt` (ISODate).
*   **`cryptography_db` (public_keys collection):**
    *   `_id`, `userId` (String, Indexed), `kemPublicKey` (String), `dsaPublicKey` (String), `isActive` (Boolean), `createdAt` (ISODate).
*   **`jobs_db` (jobs collection):**
    *   `_id`, `jobId` (String, Indexed), `jobType` (String), `status` (String), `senderUserId` (String), `recipientUserId` (String), `createdAt` (ISODate), `completedAt` (ISODate), `storage { inputFileGridFsId, secretFileGridFsId, outputFileGridFsId }`, `errorMessage` (String).
*   **`files_db` (GridFS):**
    *   `fs.files` (Managed by GridFS, with custom metadata like `ownerUserId`).
    *   `fs.chunks` (Managed by GridFS).

---

### 4. Front-End Specific Requirements

#### 4.1 Functional Requirements (Front-End)

##### FR-F-1: Authentication

*   **FR-F-1.1:** The app shall have a single "Login with GitHub" button.
*   **FR-F-1.2:** A "Logout" button must be available in the main navigation.
*   **FR-F-1.3:** The user's GitHub username and avatar shall be displayed when logged in.

##### FR-F-2: Key Management Page

*   **FR-F-2.1:** This page shall allow a user to "Generate Key Pair" (calls `POST /api/v1/keys/generate`).
*   **FR-F-2.2:** The user can "Download" the full key pair (.json) and "Copy" the public keys.
*   **FR-F-2.3:** A "Set as My Public Key" button shall call `POST /api/v1/keys/set`.
*   **FR-F-2.4:** The page shall display the user's currently active public key stored on the server.

##### FR-F-3: Public Key Directory Page

*   **FR-F-3.1:** A "Contacts" or "Users" page shall call `GET /api/v1/users` to list all registered users.
*   **FR-F-3.2:** The UI shall display a searchable list of users (avatar, username).
*   **FR-F-3.3:** Clicking a user shall display their active public keys with a "Copy Key" button.

##### FR-F-4: Encoding Page

*   **FR-F-4.1:** The page shall provide file upload components for:
    1.  The Carrier Video (MP4).
    2.  The Secret File.
    3.  The User's **Private Key File** (for signing).
*   **FR-F-4.2:** The page shall have a "Recipient" dropdown/search box, populated from the user list.
*   **FR-F-4.3:** A clear warning shall state that the private key is not stored by the server.
*   **FR-F-4.4 (Capacity):** Uploading a carrier video shall automatically trigger the `POST /api/v1/estimate` endpoint and display the result.
*   **FR-F-4.5 (Submission):** The "Start Encoding" button shall call `POST /api/v1/encode` and redirect to the Job Status page.

##### FR-F-5: Decoding Page

*   **FR-F-5.1:** The page shall provide file upload components for:
    1.  The Stego-Video (MP4).
    2.  The User's **Private Key File** (for decryption).
*   **FR-F-5.2 (Submission):** The "Start Decoding" button shall call `POST /api/v1/decode` and redirect to the Job Status page.

##### FR-F-6: Job Status Page

*   **FR-F-6.1:** This page (`/{jobId}`) shall poll `GET /api/v1/job/{jobId}/status` every few seconds.
*   **FR-F-6.2 (Completion):** On status "COMPLETED", polling stops, and a "Download File" button appears.
*   **FR-F-6.3 (Failure):** On status "FAILED", polling stops. If the `errorMessage` is `SIGNATURE_INVALID`, a specific warning must be displayed (e.g., "⚠️ **Authentication Failed:** This file's signature is invalid. It may be tampered with or from an impostor.").

##### FR-F-7: File Management Page

*   **FR-F-7.1:** A "My Files" page shall call `GET /api/v1/files` to list all files owned by the user.
*   **FR-F-7.2:** Each file entry must have a "Delete" button (calls `DELETE /api/v1/files/{fileId}`).

#### 4.2 Non-Functional Requirements (Front-End)

*   **NFR-F-1 (Technology):** Must be a **React (v18+)** SPA.
*   **NFR-F-2 (Security):** Private keys must **never** be stored in `localStorage` or any persistent browser storage. They must only exist in component memory during an operation.
*   **NFR-F-3 (Usability):** The app must provide clear visual feedback (loading spinners, progress bars, error notifications).

---

### 5. Interface Requirements

#### 5.1 REST API (External API Contract)

| Method | Endpoint                     | Description                                            |
|:-------|:-----------------------------|:-------------------------------------------------------|
| GET    | /login/github                | Initiates GitHub OAuth2 flow.                          |
| GET    | /api/v1/users                | Lists all registered users and their basic info.       |
| POST   | /api/v1/keys/generate        | Generates a new PQC KEM/DSA key pair.                  |
| POST   | /api/v1/keys/set             | (Auth) Associates public keys with the user's profile. |
| GET    | /api/v1/users/{userId}/keys  | Gets the active public keys for a specific user.       |
| POST   | /api/v1/estimate             | (Auth) Accepts an MP4 stream, returns capacity.        |
| POST   | /api/v1/encode               | (Auth) Submits an encoding job. Returns `jobId`.       |
| POST   | /api/v1/decode               | (Auth) Submits a decoding job. Returns `jobId`.        |
| GET    | /api/v1/job/{jobId}/status   | (Auth) Returns the current status of a job.            |
| GET    | /api/v1/job/{jobId}/download | (Auth) Downloads the output file for a completed job.  |
| GET    | /api/v1/files                | (Auth) Lists all files in GridFS owned by the user.    |
| DELETE | /api/v1/files/{fileId}       | (Auth) Deletes a file from GridFS.                     |

#### 5.2 Kafka Topics (Internal to Backend)

*   `job.request.encode`: Sent from Orchestration to Video Processing. **Payload now contains the final encrypted binary data.**
*   `job.request.decode`: Sent from Orchestration to Video Processing.
*   `job.completion`: Sent from Video Processing to Orchestration. **Payload for decode jobs now contains the raw extracted binary data.**

#### 5.3 External Interfaces (Backend)

*   **ffmpeg CLI:** Required by the **Video Processing Service**.
*   **GitHub OAuth2 API:** Required by the **API Gateway** for authentication.