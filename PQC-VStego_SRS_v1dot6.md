---

## **Software Requirements Specification: PQC-VStego**

Version: 1.6
Date: November 01, 2025

---

### **1\. Introduction**

#### **1.1 Purpose**

This document provides a detailed Software Requirements Specification (SRS) for "PQC-VStego," a full-stack web
application. The system's primary purpose is to embed a secret binary file within a carrier video file (MP4) using
steganography.

The system will enforce **user management via GitHub OAuth2**, allowing users to manage their PQC keys and discover the
public keys of other users. All secret data will be secured using a **hybrid Post-Quantum Cryptography (PQC) scheme** (
Key Encapsulation \+ Symmetric Encryption) and authenticated using a **PQC Digital Signature**.

All large files will be stored in **MongoDB GridFS** and processed using **InputStreams** to minimize memory load. The
system will be built as a microservice-based backend on **Java 25**, **Spring Boot**, and **Kafka**, with a **React**
front-end. This architecture will be managed by a **Config Server**, a **Discovery Service**, and will enforce **HTTPS**
for all communications.

#### **1.2 Scope**

This project covers the complete full-stack system.

* **In-Scope:**
    * A client-side Single Page Application (SPA) built in **React**.
    * User authentication via **GitHub OAuth2**.
    * Secure, **HTTPS-only** inter-service communication.
    * A user-discoverable public key directory.
    * PQC key generation (KEM & DSA).
    * PQC digital signatures (e.g., **CRYSTALS-Dilithium**) for payload authenticity.
    * Video capacity estimation.
    * Asynchronous encoding (hiding) and decoding (extracting) of binary files.
    * **MongoDB GridFS** for necessary file storage (e.g., output files, temporary secrets).
    * **Configurable, scheduled file cleanup** from GridFS.
    * A microservice architecture with asynchronous job processing via Kafka, managed by a **Config Service** and **Discovery Service**.
    * Streaming processing of all file uploads.

* **Out-of-Scope:**
    * Any user authentication provider *other than* GitHub.
    * Support for any video format other than MP4 (H.264).
    * Direct embedding of raw text (users must provide a .txt file).
    * HTTP communication; only HTTPS is permitted.

#### **1.3 Definitions, Acronyms, and Abbreviations**

| Term        | Definition                                                                                            |
|:------------|:------------------------------------------------------------------------------------------------------|
| **PQC**     | Post-Quantum Cryptography. Cryptographic algorithms secure against quantum computers.                 |
| **AES**     | Advanced Encryption Standard. A widely-used symmetric encryption algorithm.                           |
| **KEM**     | Key Encapsulation Mechanism. An algorithm used to secure a symmetric key (e.g., CRYSTALS-Kyber).      |
| **DSA**     | Digital Signature Algorithm. An algorithm to verify data authenticity (e.g., CRYSTALS-Dilithium).     |
| **LSB**     | Least Significant Bit. The lowest-order bit in a binary representation, often used for steganography. |
| **GridFS**  | A specification for MongoDB for storing and retrieving large files, such as videos.                   |
| **OAuth2**  | An open standard for access delegation, used here for GitHub login.                                   |
| **API**     | Application Programming Interface.                                                                    |
| **SPA**     | Single Page Application.                                                                              |
| **SRS**     | Software Requirements Specification.                                                                  |
| **TLS**     | Transport Layer Security. The protocol that provides HTTPS.                                           |
| **keytool** | A Java command-line tool to manage keystores (certificates and private keys).                         |

---

### **2\. Overall Description**

#### **2.1 Product Perspective**

PQC-VStego is a full-stack web application. It consists of a React SPA (client) that provides the user interface and a
headless backend (server) built on a microservice architecture. The React client communicates with the backend's API
Gateway over HTTPS to perform all operations, which are tied to authenticated user accounts.

#### **2.2 System Architecture**

The system will be composed of several distinct microservices, each with its own dedicated database, adhering to the **database-per-service** pattern. The entire ecosystem is managed by a Config Service and a Discovery Service, with all communication secured via HTTPS.

1.  **Config Service (e.g., Spring Cloud Config):**
    *   Provides centralized, externalized configuration for all other microservices (except the Discovery Service).
2.  **Discovery Service (e.g., Spring Cloud Netflix Eureka):**
    *   Provides service registration and discovery for all microservices.
3.  **React Client (Front-End):**
    *   The user-facing SPA. Handles UI, state management, and API calls to the API Gateway.
4.  **API Gateway (e.g., Spring Cloud Gateway):**
    *   The single entry point for all client requests. It handles user authentication via GitHub OAuth2, issues internal
        JWTs for service-to-service communication, and routes requests to the appropriate microservices over HTTPS.
5.  **User Service (Spring Boot):**
    * Manages user profiles. Consumes JWTs to identify users.
    * **Database:** users\_db (MongoDB).
6.  **PQC Service (Spring Boot):**
    * Handles PQC-specific operations: key generation, key encapsulation (wrapping a key), and digital signatures.
    * **Database:** pqc\_keys\_db (MongoDB). Stores user public keys.
7. **AES Service (Spring Boot):**
    * **NEW:** Handles symmetric cryptography. Provides endpoints to encrypt or decrypt data using AES-GCM.
    * This service is stateless and has no database.
8. **Orchestration Service (Spring Boot):**
    * **UPDATED:** The central coordinator. It calls the `pqc-service` and `aes-service` to perform the full
      cryptographic chain (encryption, encapsulation, signing). It then publishes a job to Kafka containing the final
      binary payload for the `video-processing-service`. For decoding, it does the reverse.
    *   **Database:** jobs\_db (MongoDB). Stores job metadata.
9. **File Service (Spring Boot):**
    * Manages all file persistence in MongoDB GridFS. Provides an internal HTTPS API for file operations.
    *   **Database:** files\_db (MongoDB with **GridFS**).
10. **Video Processing Service (Spring Boot):**
    * **UPDATED:** A specialized worker that only performs steganography. It consumes jobs from Kafka, embeds a given
      binary payload into a video, or extracts a binary payload from a video. It has no knowledge of cryptography.

#### **2.3 Product Functions**

*   **User Authentication:** Allow users to log in/out via GitHub.
*   **Key Management:** Allow users to generate PQC key pairs (KEM \+ DSA) and associate their public keys with their profile.
*   **Public Key Directory:** Allow users to browse a list of all registered users and copy their public keys.
* **Encoding:** Allow an authenticated user to hide a secret file within a video, destined for another registered user.
*   **Decoding:** Allow a user to extract and verify a secret file from a stego-video.
* **File Management:** Allow users to view and delete their job-related output files.

#### **2.4 Constraints**

*   **C-1 (Platform):** Backend: **Java 25** / **Spring Boot 3.x+**. Front-end: **React**.
* **C-2 (Cryptography):** Must use **Bouncy Castle**. PQC KEM: **CRYSTALS-Kyber**. PQC DSA: **CRYSTALS-Dilithium**.
  Symmetric: **AES-256-GCM**.
*   **C-3 (Video Tooling):** Must use the **ffmpeg** CLI tool for video processing.
*   **C-4 (Database):** Must use a **database-per-service** pattern. All databases shall be **MongoDB**.
*   **C-5 (Messaging):** All asynchronous inter-service communication *must* use **Kafka**.
*   **C-6 (Storage):** All large binary files *must* be stored in **MongoDB GridFS**. Carrier video files must not be persisted.
* **C-7 (Streaming):** All video file uploads *must* be processed as **InputStreams**.
*   **C-8 (File Format):** Shall only accept **MP4 (H.264 codec)**.
*   **C-9 (Secret Format):** Shall only accept a **binary file** as the secret.
* **C-10 (Configuration):** All microservices must be stateless and retrieve their configuration from the central *
  *Config Service**.
* **C-11 (Discovery):** All microservices must register with the **Discovery Service**.
* **C-12 (Security Protocol):** All inter-service communication must be over **HTTPS**.

#### **2.5 Assumptions and Dependencies**

* **A-1:** The **Config Service** and **Discovery Service** are running.
* **A-2:** Kafka and all MongoDB instances are running.
*   **A-3:** ffmpeg CLI is installed on all Video Processing Service instances.
* **A-4:** A GitHub OAuth2 application has been configured.
* **A-5:** Valid Java Keystore (`.jks`) and Truststore files for TLS are created.

---

### **3\. Backend Specific Requirements**

#### **3.1 Functional Requirements (Backend)**

##### **FR-B-1: User Authentication & Authorization**

*   **FR-B-1.1:** The API Gateway must implement the "Login with GitHub" OAuth2 flow.
* **FR-B-1.2:** Upon successful login, the User Service shall create or update a user profile.
* **FR-B-1.3:** The API Gateway must enforce authentication for all endpoints. All inter-service communication must be
  secured via HTTPS and authenticated with a JWT.
* **FR-B-1.4:** The User Service shall provide an endpoint GET /api/v1/users to list all registered users.
* **FR-B-1.5 (Exception Handling):** All services must return appropriate HTTP status codes and error messages.

##### **FR-B-2: PQC Key Management**

* **FR-B-2.1:** The PQC Service shall provide POST /api/v1/keys/generate to generate a new PQC key pair.
* **FR-B-2.2:** The PQC Service shall provide POST /api/v1/keys/set for an authenticated user to associate their public
  keys with their profile.
*   **FR-B-2.3:** The PQC Service shall provide GET /api/v1/users/{userId}/keys to fetch the active public keys for a specific user.

##### **FR-B-3: Capacity Estimation**

*   **FR-B-3.1:** The Orchestration Service shall provide POST /api/v1/estimate that accepts an MP4 file InputStream.
* **FR-B-3.2:** This request will be proxied to the **Video Processing Service**, which will use ffprobe to get frame
  count and dimensions.
*   **FR-B-3.3:** The endpoint shall return the calculated capacity in bytes.

##### **FR-B-4: Encoding Job (NEW ARCHITECTURE)**

*   **FR-B-4.1 (Input):** The Orchestration Service shall provide POST /api/v1/encode (multipart-form) that accepts:
    1.  Carrier MP4 file (InputStream).
    2.  Secret binary file (InputStream).
    3.  Recipient's userId.
    4.  Sender's PQC Private Key as a `.txt` file (InputStream).
* **FR-B-4.2 (Cryptographic Orchestration):** The Orchestration Service shall perform the following synchronous steps:
    1. Generate a random, single-use AES-256 GCM key.
    2. Call the **AES Service** to encrypt the secret file stream using the AES key, receiving the `encryptedData`.
    3. Call the **PQC Service** to create a digital signature of the `encryptedData` using the *sender's private key*.
    4. Call the **PQC Service** to encapsulate the AES key using the *recipient's public key*.
    5. Assemble the final binary payload:
       `[Metadata Header] + [Sender's Public DSA Key] + [Encapsulated AES Key] + [PQC Signature] + [Encrypted Data]`.
* **FR-B-4.3 (Job Creation & Dispatch):** After successfully creating the payload, the Orchestration Service shall:
    1. Create a job document in jobs\_db (status: "PENDING").
    2. Return a `jobId` (HTTP 202).
    3. Publish an `ENCODE_JOB_REQUEST` to Kafka. The Kafka message will contain the `jobId`, the `carrierFileId` (if
       stored, otherwise streamed), and the final binary `payload`.
* **FR-B-4.4 (Steganography):** The **Video Processing Service**, upon consuming the job, shall:
    1. Fetch the original carrier video stream.
    2. Pipe the stream to `ffmpeg` to extract frames.
    3. Embed the binary `payload` from the Kafka message into the LSBs of the frames.
    4. Pipe the modified frames to `ffmpeg` to create the final stego-video stream, streaming it directly to the File
       Service.
* **FR-B-4.5 (Completion):** Upon receiving a `JOB_COMPLETION` message from Kafka, the Orchestration Service shall
  update the job status to "COMPLETED" and store the `outputFileGridFsId`.

##### **FR-B-5: Decoding Job (NEW ARCHITECTURE)**

*   **FR-B-5.1 (Input):** The Orchestration Service shall provide POST /api/v1/decode that accepts:
    1.  Stego-Video file (InputStream).
    2.  Recipient's PQC Private Key as a `.txt` file (InputStream).
* **FR-B-5.2 (Job Creation & Extraction):**
    1. Create a job in jobs\_db, return a `jobId` (HTTP 202).
    2. Publish a `DECODE_JOB_REQUEST` to Kafka containing the `jobId` and `stegoFileId`.
    3. The **Video Processing Service** will consume the job, extract the raw binary payload from the video, and publish
       a `JOB_COMPLETION` message back to Kafka containing the `jobId` and the `extractedPayload`.
* **FR-B-5.3 (Verification & Decryption):** The **Orchestration Service**, upon consuming the completion message with
  the `extractedPayload`, shall:
    1. Parse the payload into its components.
    2. Call the **PQC Service** to verify the signature against the `EncryptedData`. If it fails, the job is marked as
       FAILED (SIGNATURE\_INVALID).
    3. Call the **PQC Service** to decapsulate the AES key using the *recipient's private key*. If it fails, the job is
       marked as FAILED (DECRYPTION\_FAILED).
    4. If both succeed, call the **AES Service** with the recovered AES key and the `EncryptedData` to get the final
       decrypted file stream.
* **FR-B-5.4 (Completion):** Stream the final decrypted secret file to the File Service and update the job to status: "
  COMPLETED".

##### **FR-B-6: File Management**

* **FR-B-6.1 (Status):** GET /api/v1/job/{jobId}/status shall return the job status.
* **FR-B-6.2 (Download):** GET /api/v1/job/{jobId}/download shall stream the output file from the File Service.
* **FR-B-6.3 (List):** GET /api/v1/files shall list all output files owned by the user.
* **FR-B-6.4 (Delete):** DELETE /api/v1/files/{fileId} shall delete an output file.

#### **3.2 Non-Functional Requirements (Backend)**

* **NFR-B-1 (Performance):** All encode/decode APIs must be asynchronous, returning a jobId in < 200ms.
* **NFR-B-2 (Concurrency):** All Spring Boot services must use **Java 25 Virtual Threads**.
* **NFR-B-3 (Streaming):** All file handling *must* use streams.
* **NFR-B-4 (Security):** All APIs must use HTTPS/TLS. Communication must be authenticated via JWTs. Private keys must
  never be stored.
* **NFR-B-5 (Scalability):** The Video Processing Service must be horizontally scalable.
* **NFR-B-6 (File Cleanup):** A configurable, toggleable, scheduled job shall periodically delete old files from GridFS.
* **NFR-B-7 (Configuration):** All configurations must be managed in the central **Config Service**.
* **NFR-B-8 (Resilience):** Services must use the **Discovery Service** for communication.

#### **3.3 Data Requirements (Backend)**

* **users\_db (users):** \_id, githubId, username, avatarUrl, createdAt.
* **pqc\_keys\_db (public\_keys):** \_id, userId, kemPublicKey, dsaPublicKey, isActive, createdAt.
* **jobs\_db (jobs):** \_id, jobId, jobType, status, senderUserId, recipientUserId, createdAt, completedAt, storage {
  inputFile, outputFile }, errorMessage.
* **files\_db (GridFS):** fs.files, fs.chunks.

---

### **4\. Front-End Specific Requirements**

_(No changes in this section, as the user-facing functionality remains the same)_

#### **4.1 Functional Requirements (Front-End)**

* **FR-F-1: Authentication:** "Login with GitHub", "Logout", display user info.
* **FR-F-2: Key Management Page:** Generate, download, copy, and set public keys.
* **FR-F-3: Public Key Directory Page:** List and search for users to get their public keys.
* **FR-F-4: Encoding Page:** Upload carrier, secret, and private key files; select recipient; see capacity; submit job.
* **FR-F-5: Decoding Page:** Upload stego-video and private key file; submit job.
* **FR-F-6: Job Status Page:** Poll for job status and provide download/error messages.
* **FR-F-7: File Management Page:** List and delete user-owned output files.

#### **4.2 Non-Functional Requirements (Front-End)**
*   **NFR-F-1 (Technology):** Must be a **React (v18+)** SPA.
* **NFR-F-2 (Security):** Private keys must **never** be stored in persistent browser storage.
* **NFR-F-3 (Usability):** App must provide clear visual feedback for all operations.

---

### **5\. Interface Requirements**

#### **5.1 REST API (Provided by Backend)**

_(No changes in this section, as the external API contract remains the same)_

| Method | Endpoint                     | Description                                            |
|:-------|:-----------------------------|:-------------------------------------------------------|
| GET    | /login/github                | Initiates GitHub OAuth2 flow.                          |
| GET    | /api/v1/users                | Lists all registered users and their basic info.       |
| POST   | /api/v1/keys/generate        | Generates a new PQC KEM/DSA key pair.                  |
| POST   | /api/v1/keys/set             | (Auth) Associates public keys with the user's profile. |
| GET    | /api/v1/users/{userId}/keys  | Gets the active public keys for a specific user.       |
| POST   | /api/v1/estimate             | (Auth) Accepts an MP4 stream, returns capacity.        |
| POST   | /api/v1/encode               | (Auth) Submits an encoding job. Returns jobId.         |
| POST   | /api/v1/decode               | (Auth) Submits a decoding job. Returns jobId.          |
| GET    | /api/v1/job/{jobId}/status   | (Auth) Returns the current status of a job.            |
| GET    | /api/v1/job/{jobId}/download | (Auth) Downloads the output file for a completed job.  |
| GET    | /api/v1/files                | (Auth) Lists all output files owned by the user.       |
| DELETE | /api/v1/files/{fileId}       | (Auth) Deletes an output file from GridFS.             |

#### **5.2 Kafka Topics (Internal to Backend)**

* `job.request.encode`: Sent from Orchestration to Video Processing. **Payload now contains the final encrypted binary
  data.**
* `job.request.decode`: Sent from Orchestration to Video Processing.
* `job.completion`: Sent from Video Processing to Orchestration. **Payload for decode jobs now contains the raw
  extracted binary data.**

#### **5.3 External Interface (Backend)**

*   **ffmpeg CLI:** Required by the Video Processing Service.
*   **GitHub OAuth2 API:** Required by the API Gateway for authentication.