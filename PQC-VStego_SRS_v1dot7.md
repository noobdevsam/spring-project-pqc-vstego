---

## **Software Requirements Specification: PQC-VStego**

Version: 1.7  
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
    * A microservice architecture with asynchronous job processing via Kafka, managed by a **Config Service** and *
      *Discovery Service**.
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

The system will be composed of several distinct microservices, each with its own dedicated database, adhering to the *
*database-per-service** pattern. The entire ecosystem is managed by a Config Service and a Discovery Service, with all
communication secured via HTTPS.

1. **Config Service (e.g., Spring Cloud Config):**
    * Provides centralized, externalized configuration for all other microservices (except the Discovery Service).
    * Pulls configuration from a central repository (e.g., a Git repository).
2. **Discovery Service (e.g., Spring Cloud Netflix Eureka):**
    * Provides service registration and discovery for all microservices.
    * Allows services to find and communicate with each other dynamically using secure HTTPS URLs.
3.  **React Client (Front-End):**
    *   The user-facing SPA. Handles UI, state management, and API calls to the API Gateway.
4. **API Gateway (e.g., Spring Cloud Gateway):**
    * The single entry point for all client requests. It handles user authentication via GitHub OAuth2, issues internal
      JWTs for service-to-service communication, and routes requests to the appropriate microservices over HTTPS.
    * It discovers the location of other services via the **Discovery Service** and pulls its configuration from the *
      *Config Service**.
5.  **User Service (Spring Boot):**
    * Manages user profiles. It consumes the JWT from the API Gateway to identify the user.
    * **Database:** users\_db (MongoDB). Stores user profiles (GitHub ID, username, avatar).
    * It registers with the **Discovery Service** and pulls its configuration (e.g., database credentials) from the *
      *Config Service**.
6. **PQC Service (Spring Boot):**
    * Handles all cryptographic operations: key generation, signing, and verification.
    * Manages the user-public key association.
    * **Database:** pqc\_keys\_db (MongoDB). Stores user public keys linked to userIds.
    * It registers with the **Discovery Service** and pulls its configuration from the **Config Service**.
7. **Orchestration Service (Spring Boot):**
    * Handles the primary business logic for encoding/decoding jobs.
    * Validates requests and publishes jobs to Kafka.
    * Tracks job status and handles all API-level exception handling.
    * **Database:** jobs\_db (MongoDB). Stores job metadata.
    * It registers with the **Discovery Service** and pulls its configuration from the **Config Service**.
8.  **File Service (Spring Boot):**
    * Manages all file persistence. Provides an internal HTTPS API for uploading/downloading file streams to/from
      GridFS.
    * **Database:** files\_db (MongoDB with **GridFS**).
    * It registers with the **Discovery Service** and pulls its configuration from the **Config Service**.
9. **Video Processing Service (Spring Boot):**
    * The worker service. Consumes jobs from Kafka.
    * Fetches files from the File Service.
    * Executes ffmpeg for frame extraction/re-assembly.
    * Performs LSB steganography.
    * Streams resulting files back to the File Service.
    * It registers with the **Discovery Service** and pulls its configuration from the **Config Service**.

#### **2.3 Product Functions**

*   **User Authentication:** Allow users to log in/out via GitHub.
* **Key Management:** Allow users to generate PQC key pairs (KEM \+ DSA) and associate their public keys with their
  profile.
*   **Public Key Directory:** Allow users to browse a list of all registered users and copy their public keys.
* **Encoding:** Allow an authenticated user to hide a secret file within a video, destined for another registered user.
  The payload will be encrypted and digitally signed.
*   **Decoding:** Allow a user to extract and verify a secret file from a stego-video.
* **File Management:** Allow users to view and delete their job-related output files (generated stego-videos, extracted
  secrets).

#### **2.4 Constraints**

*   **C-1 (Platform):** Backend: **Java 25** / **Spring Boot 3.x+**. Front-end: **React**.
* **C-2 (Cryptography):** Must use **Bouncy Castle**. KEM: **CRYSTALS-Kyber**. DSA: **CRYSTALS-Dilithium**.
* **C-3 (Video Tooling):** Must use the **ffmpeg** CLI tool for video processing.
*   **C-4 (Database):** Must use a **database-per-service** pattern. All databases shall be **MongoDB**.
*   **C-5 (Messaging):** All asynchronous inter-service communication *must* use **Kafka**.
* **C-6 (Storage):** All large binary files *must* be stored in **MongoDB GridFS**. Carrier video files must not be
  persisted.
* **C-7 (Streaming):** All video file uploads *must* be processed as **InputStreams** to minimize memory. They should be
  streamed directly to ffmpeg's stdin, not saved as temporary files on the service's local disk.
*   **C-8 (File Format):** Shall only accept **MP4 (H.264 codec)**.
*   **C-9 (Secret Format):** Shall only accept a **binary file** as the secret.
* **C-10 (Configuration):** All microservices (except the Discovery Service) must be stateless and retrieve their
  configuration from the central **Config Service** at startup.
* **C-11 (Discovery):** All microservices must register with the **Discovery Service** on startup and use it to find
  other services via their secure HTTPS URLs.
* **C-12 (Security Protocol):** All inter-service communication must be over **HTTPS**, secured with TLS certificates
  managed via Java keystores (`keytool`).

#### **2.5 Assumptions and Dependencies**

*   **A-1:** The **Config Service** and **Discovery Service** are running and accessible.
* **A-2:** A Kafka cluster and all MongoDB instances are running and accessible (with connection details provided by the
  Config Service).
* **A-3:** ffmpeg CLI is installed on all Video Processing Service instances.
*   **A-4:** A GitHub OAuth2 application (Client ID, Client Secret) has been configured.
* **A-5:** Valid Java Keystore (`.jks`) and Truststore files for TLS are created and their paths/passwords are available
  to be managed by the Config Service.

---

### **3\. Backend Specific Requirements**

#### **3.1 Functional Requirements (Backend)**

##### **FR-B-1: User Authentication & Authorization**

The system employs a centralized authentication model using the API Gateway, which is responsible for both
authenticating users via GitHub OAuth2 and issuing internal JSON Web Tokens (JWTs) for secure inter-service
communication over **HTTPS**.

1. **User Authentication via GitHub (API Gateway)**
    * A user initiates login by accessing a specific endpoint on the `API Gateway` (e.g.,
      `/oauth2/authorization/github`).
    * The `API Gateway` redirects the user to GitHub's OAuth2 authorization screen.
    * Upon user approval, GitHub redirects back to the `API Gateway` with an authorization code.
    * The `API Gateway` exchanges this code with GitHub for an access token.

2. **Internal JWT Generation (API Gateway)**
    * After successfully obtaining a GitHub access token, the `API Gateway` generates a new, internal JWT.
    * This JWT contains claims about the user, including their username, roles, and permissions (scopes).
    * The JWT is signed using a private RSA key held exclusively by the `API Gateway`.

3. **Secure Downstream Requests**
    * For every subsequent request made by the user to the backend, the `API Gateway` attaches the generated JWT to the
      `Authorization` header (e.g., `Authorization: Bearer <token>`).
    * This JWT, not the GitHub token, is forwarded to the downstream microservices over **HTTPS**.

4. **Service-to-Service Authorization (Resource Servers)**
    * All downstream microservices (`user-service`, `pqc-service`, etc.) are configured as OAuth2 Resource Servers and
      must enforce HTTPS.
    * On receiving a request, each service inspects the `Authorization` header for the JWT.
    * The service validates the JWT's signature by fetching the public key from the `API Gateway`'s public JWK Set
      endpoint (`/.well-known/jwks.json`) over HTTPS.
    * If the token is valid and not expired, the service extracts the user's identity and permissions from the claims to
      make authorization decisions.

* **FR-B-1.1:** The API Gateway must implement the "Login with GitHub" OAuth2 flow.
* **FR-B-1.2:** Upon successful login, the User Service shall create or update a user profile in the users\_db (storing
  GitHub ID, username, avatar URL).
* **FR-B-1.3:** The API Gateway must enforce authentication for all endpoints. All inter-service communication must be
  secured via HTTPS and authenticated with a JWT issued by the API Gateway.
* **FR-B-1.4:** The User Service shall provide an endpoint GET /api/v1/users to list all registered users (username,
  avatar, userId).
* **FR-B-1.5 (Exception Handling):** All services must implement robust exception handling to return appropriate HTTP
  status codes (e.g., 400, 401, 403, 404, 500) and clear error messages in the response body.

##### **FR-B-2: PQC Key Management**

* **FR-B-2.1:** The PQC Service shall provide POST /api/v1/keys/generate to generate a new PQC key pair (both Kyber KEM
  and Dilithium DSA keys).
* **FR-B-2.2:** The PQC Service shall provide POST /api/v1/keys/set for an authenticated user to associate their public
  keys (KEM \+ DSA) with their profile. This is stored in pqc\_keys\_db.
* **FR-B-2.3:** The PQC Service shall provide GET /api/v1/users/{userId}/keys to fetch the active public keys for a
  specific user.

##### **FR-B-3: Capacity Estimation**

* **FR-B-3.1:** The Orchestration Service shall provide POST /api/v1/estimate that accepts an MP4 file InputStream.
* **FR-B-3.2:** The service shall pipe this InputStream directly to ffprobe \-i pipe:0 to get frame count and dimensions
  without saving a temporary file.
*   **FR-B-3.3:** The endpoint shall return the calculated capacity in bytes.

##### **FR-B-4: Encoding Job**

* **FR-B-4.1 (Input):** The Orchestration Service shall provide POST /api/v1/encode (multipart-form) that accepts:
    1. Carrier MP4 file (InputStream).
    2. Secret binary file (InputStream).
    3. Recipient's userId.
    4. Sender's PQC Private Key as a `.txt` file (InputStream).
* **FR-B-4.2 (Job Creation):** The Orchestration Service shall:
    1. Stream the **secret file** to the File Service (GridFS) and receive its `secretFileId`. The carrier video is *
       *not** stored.
    2. Create a job document in jobs\_db (status: "PENDING", jobType: "ENCODE", senderUserId, recipientUserId,
       `secretFileId`).
    3. Return a jobId (HTTP 202\) and publish an `ENCODE_JOB_REQUEST` to Kafka. The Kafka message will contain the job
       details and the `secretFileId`.
* **FR-B-4.3 (Hybrid Encryption & Signing):** The Video Processing Service, upon consuming the job, shall:
    1. Fetch the recipient's public keys (KEM) and the sender's private key (DSA) from the job message.
    2. Fetch the secret file stream from the File Service using the `secretFileId`.
    3. Generate a random, single-use AES-256 GCM key.
    4. Encrypt the secret file stream using the AES key.
    5. **Sign:** Create a PQC (Dilithium) signature of the `encryptedData` using the *sender's private key*.
    6. **Encapsulate:** Encapsulate the AES key using the *recipient's public key* (Kyber).
    7. Create Payload: Create a single binary payload:
       \[Metadata Header\] \+ \[Sender's Public DSA Key\] \+ \[Encapsulated AES Key\] \+ \[PQC Signature\] \+
       \[Encrypted Data\]
        (The header must define the length of each subsequent section).
* **FR-B-4.4 (Steganography):** The Video Processing Service shall:
    1. Fetch the original carrier video stream by making an HTTPS request back to the Orchestration Service, which will
       proxy the stream from the initial user request.
    2. Pipe the stream to `ffmpeg -i pipe:0 ...` to extract frames.
    3. Embed the binary payload (from FR-B-4.3) into the LSBs of the frames.
    4. Pipe the modified frames to `ffmpeg ... -f mp4 pipe:1`, streaming the output *directly* to the File Service to
       create the final **stego-video**.
* **FR-B-4.5 (Completion):** Upon receiving a completion message from the Video Processing service, the Orchestration
  service shall update the job in jobs\_db to status: "COMPLETED" and store the `outputFileGridFsId`. The temporary
  secret file shall be deleted from GridFS.

##### **FR-B-5: Decoding Job**

* **FR-B-5.1 (Input):** The Orchestration Service shall provide POST /api/v1/decode that accepts:
    1. Stego-Video file (InputStream).
    2. Recipient's PQC Private Key as a `.txt` file (InputStream).
* **FR-B-5.2 (Job Creation):** Create a job in jobs\_db, return a jobId (HTTP 202), and publish a `DECODE_JOB_REQUEST`.
  The stego-video is **not** stored.
* **FR-B-5.3 (Extraction):** The Video Processing Service shall consume the job, stream the stego-video (proxied from
  the Orchestration Service) to ffmpeg to extract frames, and read the LSBs to re-assemble the full binary payload.
* **FR-B-5.4 (Decryption & Verification):** The service shall:
    1. Parse the payload into its components (Header, Sender's Public Key, EncapsulatedKey, Signature, EncryptedData).
    2. **Verify:** Check the Signature against the EncryptedData using the Sender's Public Key (Dilithium). If
       verification fails, stop and set job status to FAILED (SIGNATURE\_INVALID).
    3. **Decapsulate:** Decapsulate the AES key using the *recipient's private key* (Kyber). If this fails, stop and set
       job status to FAILED (DECRYPTION\_FAILED).
    4. **Decrypt:** If both steps succeed, use the recovered AES key to decrypt the EncryptedData.
* **FR-B-5.5 (Completion):** Stream the final **decrypted secret file** to the File Service (GridFS) and update the job
  to status: "COMPLETED" with the corresponding `outputFileGridFsId`.

##### **FR-B-6: File Management**

* **FR-B-6.1 (Status):** GET /api/v1/job/{jobId}/status shall return the job status from jobs\_db.
* **FR-B-6.2 (Download):** GET /api/v1/job/{jobId}/download shall retrieve the `outputFileGridFsId` from the job and
  stream the file from the File Service (GridFS).
* **FR-B-6.3 (List):** GET /api/v1/files shall return a list of all **output files** in GridFS associated with the
  authenticated user's completed jobs.
* **FR-B-6.4 (Delete):** DELETE /api/v1/files/{fileId} shall delete an output file from GridFS, provided the
  authenticated user is the owner.

#### **3.2 Non-Functional Requirements (Backend)**

* **NFR-B-1 (Performance):** All encode/decode APIs must be asynchronous, returning a jobId in \< 200ms.
* **NFR-B-2 (Concurrency):** All Spring Boot services must use **Java 25 Virtual Threads** (
  spring.threads.virtual.enabled=true).
* **NFR-B-3 (Streaming):** All file handling *must* use InputStream and OutputStream to process data as streams, never
  loading a full file into service memory (per C-7).
* **NFR-B-4 (Security):** All API endpoints must be secured with HTTPS/TLS. All communication between the API Gateway
  and downstream services must be over HTTPS and authenticated using JWTs. PQC private keys must never be stored by the
  backend.
* **NFR-B-5 (Scalability):** The Video Processing Service must be horizontally scalable (multiple instances in the same
  Kafka consumer group).
*   **NFR-B-6 (File Cleanup):**
    * **NFR-B-6.1:** A scheduled job (@Scheduled) shall run periodically to delete files from GridFS.
    * **NFR-B-6.2:** The job shall delete files older than a configurable retention period (e.g.,
      pqcstego.cleanup.retention-days=30).
    * **NFR-B-6.3:** The cleanup job must be toggleable via a configuration property (e.g.,
      pqcstego.cleanup.enabled=false).
* **NFR-B-7 (Configuration):** All service-specific configurations (database URLs, Kafka topics, TLS keystore details)
  must be managed in the central **Config Service**.
* **NFR-B-8 (Resilience):** Services must use the **Discovery Service** to find and communicate with each other's secure
  HTTPS endpoints (e.g., via Spring Cloud LoadBalancerClient).

#### **3.3 Data Requirements (Backend)**

* **users\_db (users collection):**
    * \_id, githubId (String, Indexed), username (String), avatarUrl (String), createdAt (ISODate).
* **pqc\_keys\_db (public\_keys collection):**
    * \_id, userId (String, Indexed), kemPublicKey (String), dsaPublicKey (String), isActive (Boolean), createdAt (
      ISODate).
* **jobs\_db (jobs collection):**
    * \_id, jobId (String, Indexed), jobType (String), status (String), senderUserId (String), recipientUserId (String),
      createdAt (ISODate), completedAt (ISODate), storage { secretFileGridFsId (for encoding), outputFileGridFsId },
      errorMessage (String).
* **files\_db (GridFS):**
    * fs.files (Managed by GridFS, will include filename, uploadDate, and custom metadata like ownerUserId).
    * fs.chunks (Managed by GridFS).

---

### **4\. Front-End Specific Requirements**

#### **4.1 Functional Requirements (Front-End)**

##### **FR-F-1: Authentication**

* **FR-F-1.1:** The app shall have a single "Login with GitHub" button. Unauthenticated users shall be prompted to log
  in.
*   **FR-F-1.2:** A "Logout" button must be available in the main navigation.
* **FR-F-1.3:** The user's GitHub username and avatar shall be displayed in the navigation when logged in.

##### **FR-F-2: Key Management Page**

* **FR-F-2.1:** This page shall allow a user to "Generate Key Pair" (calls POST /api/v1/keys/generate).
*   **FR-F-2.2:** The user can "Download" the full key pair (.json) and "Copy" the public keys.
* **FR-F-2.3:** A "Set as My Public Key" button shall call POST /api/v1/keys/set to associate the generated public keys
  with the user's profile.
* **FR-F-2.4:** The page shall display the user's *currently active* public key stored on the server.

##### **FR-F-3: Public Key Directory Page**

* **FR-F-3.1:** A "Contacts" or "Users" page shall call GET /api/v1/users to list all registered users.
*   **FR-F-3.2:** The UI shall display a searchable list of users (avatar, username).
*   **FR-F-3.3:** Clicking a user shall display their active public keys with a "Copy Key" button.

##### **FR-F-4: Encoding Page**

*   **FR-F-4.1:** The page shall provide file upload components for:
    1.  The Carrier Video (MP4).
    2.  The Secret File.
    3. The User's **Private Key File (`.txt`)** (for signing).
* **FR-F-4.2:** The page shall have a "Recipient" dropdown/search box, populated from the user list (FR-F-3).
* **FR-F-4.3:** A clear warning shall state that the private key is *only* used in-browser for this operation and is not
  stored.
* **FR-F-4.4 (Capacity):** Uploading a carrier video shall automatically trigger the POST /api/v1/estimate endpoint and
  display the result.
* **FR-F-4.5 (Submission):** The "Start Encoding" button shall call POST /api/v1/encode and redirect to the Job Status
  page.

##### **FR-F-5: Decoding Page**

*   **FR-F-5.1:** The page shall provide file upload components for:
    1.  The Stego-Video (MP4).
    2. The User's **Private Key File (`.txt`)** (for decryption).
* **FR-F-5.2 (Submission):** The "Start Decoding" button shall call POST /api/v1/decode and redirect to the Job Status
  page.

##### **FR-F-6: Job Status Page**

* **FR-F-6.1:** This page (/{jobId}) shall poll GET /api/v1/job/{jobId}/status every 3 seconds.
* **FR-F-6.2 (Completion):** On status: "COMPLETED", polling stops, and a "Download File" button appears.
* **FR-F-6.3 (Failure):** On status: "FAILED", polling stops. If the errorMessage is SIGNATURE\_INVALID, a specific
  warning must be displayed (e.g., "⚠️ **Authentication Failed:** This file's signature is invalid. It may be tampered
  with or from an impostor.").

##### **FR-F-7: File Management Page**

* **FR-F-7.1:** A "My Files" page shall call GET /api/v1/files to list all output files owned by the user.
* **FR-F-7.2:** The UI shall present a list with file names, creation dates, and types.
* **FR-F-7.3:** Each file entry must have a "Delete" button (calls DELETE /api/v1/files/{fileId}).

#### **4.2 Non-Functional Requirements (Front-End)**

*   **NFR-F-1 (Technology):** Must be a **React (v18+)** SPA.
* **NFR-F-2 (Security):** Private keys must **never** be stored in localStorage or any persistent browser storage. They
  must only exist in component memory during an operation.
* **NFR-F-3 (Usability):** The app must provide clear visual feedback (loading spinners, progress bars for uploads,
  error notifications).

---

### **5\. Interface Requirements**

#### **5.1 REST API (Provided by Backend)**

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

* job.request.encode
* job.request.decode
* job.completion

#### **5.3 External Interface (Backend)**

* **ffmpeg CLI:** Required by the Video Processing Service.
* **GitHub OAuth2 API:** Required by the API Gateway for authentication.
