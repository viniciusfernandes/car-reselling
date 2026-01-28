# Used Car Reseller MVP — Feature Context (Backend: Spring Boot | Frontend: React + Tailwind)

This document is a **single feature-context file** (like the attached example) that describes **end-to-end flows** and **acceptance criteria** to implement the MVP. It is designed to be directly consumable by an AI agent (e.g., Cursor) to generate backend + frontend code and comprehensive test cases.

> Scope: **Vehicle Registration**, **Service Management**, **Document Management**, **Basic Reporting**.  
> Out of scope for MVP: Authentication/Authorization, partner commission settlement, advanced pricing/market analysis, CRM pipeline.

---

## 1) Domain Overview

### Business Description
Business Description
I am a used car reseller in Brazil, and my daily routine consists of researching cars with higher liquidity in the market and negotiating a price below the current market value so that I can have a profit margin when reselling. My research sources are the internet and personal contacts. After purchasing a car, I carry out a personal inspection, take it to a mechanic for a check-up, and perform any necessary maintenance so that it is in full working condition before resale. Once all repairs are completed, I send it to a multi-brand partner dealership so they can showcase the car and start negotiating with potential buyers. If the negotiation is successful and the sale is completed, the partner dealer will receive a commission at the end of the month.

### Business Goal
Centralize control of vehicles acquired by a used car reseller in Brazil, tracking:
- **Vehicles in the lot** (purchase + identity fields)
- **Costs per vehicle** (purchase + freight + services)
- **Documents per vehicle** (invoices, receipts, service orders)
- **Basic report** to show distributed vehicles by partner dealership and totals

### Key Concepts (Ubiquitous Language)
- **Vehicle**: A car the reseller purchased and is managing until resale.
- **Supplier Source**: Where the vehicle was found/purchased (internet or personal contact).
- **Service**: A maintenance/repair action with a type and value, attached to a vehicle.
- **Document**: Any file associated with a vehicle (invoice, receipt, service order note).
- **Partner Dealership**: Multi-brand partner that receives vehicles for showcase/negotiation.
- **Distribution**: Assigning a vehicle to a partner dealership (where it is currently showcased).

---

## 2) System Boundaries

### Backend (Java + Spring Boot)
- REST API (JSON) for all CRUD flows.
- File upload for documents.
- Database persistence (relational; e.g., PostgreSQL or MySQL).
- Server-side validation for all commands.
- Deterministic IDs (UUID) and auditing timestamps.

### Frontend (React + Tailwind)
- Rich interfaces to manage vehicles, services, documents, and reports.
- Form validation aligned with backend rules.
- UX expectations: list/search/filter, detail pages, inline totals, upload previews.

---

## 3) Code Standards & Conventions (must-follow)

> If your `application-context.md` has stricter/different rules, those must override this section.

### Backend Conventions
- Package base: `br.com.carreselling`
- Clean Architecture layering:
  - `domain` (entities, value objects, domain services)
  - `application` (use cases, commands/queries, DTOs, ports)
  - `infrastructure` (persistence, storage adapters, security stubs)
  - `presentation` (controllers, request/response models)
- Naming:
  - Use cases: `CreateVehicleUseCase`, `UpdateVehicleUseCase`, `UploadVehicleDocumentUseCase`, etc.
  - Commands: `CreateVehicleCommand`, `AddServiceCommand`, ...
  - Controllers: `VehicleController`, `VehicleServiceController`, `VehicleDocumentController`, `ReportController`
- Validation:
  - Spring `@Validated`, Bean Validation (`jakarta.validation`)
  - Domain-level validation for invariants (e.g., plate format constraints).
- Errors:
  - Standard error envelope with `code`, `message`, `details[]`, `timestamp`, `traceId`.
- Testing:
  - Unit tests for use cases and domain validation.
  - Controller tests for contract and validation.
  - Repository tests for persistence mapping.

### Frontend Conventions
- React 18 + TypeScript.
- Tailwind for styling.
- Component naming: `VehicleForm`, `VehicleTable`, `ServiceList`, `DocumentUploader`, `PartnerSelect`.
- Pages:
  - `/vehicles` list
  - `/vehicles/new` create
  - `/vehicles/:id` detail (tabs: Overview, Services, Documents, Distribution)
  - `/reports/distributed-vehicles` report
- Data fetching: `fetch` or `axios` (choose one consistently).
- Testing:
  - Component tests (React Testing Library)
  - E2E tests (Cypress) aligned with acceptance criteria.

---

## 4) Data Model (MVP)

### 4.1 Vehicle
**Fields**
- `id: UUID`
- `licensePlate: string` (required, unique)
- `renavam: string` (optional, unique if provided)
- `vin: string` (optional, unique if provided)
- `year: int` (required)
- `color: string` (required)
- `model: string` (required)
- `brand: string` (required)
- `supplierSource: enum` (required) → `INTERNET | PERSONAL_CONTACT`
- `purchasePrice: money` (required)
- `freightCost: money` (optional, default 0)
- `purchasePaymentReceiptDocumentId: UUID?` (optional; link to Document)
- `purchaseInvoiceDocumentId: UUID?` (optional; link to Document)
- `status: enum` (required) → `IN_LOT | IN_SERVICE | READY_FOR_DISTRIBUTION | DISTRIBUTED`
- `assignedPartnerId: UUID?` (nullable; set when DISTRIBUTED)
- `createdAt, updatedAt: instant`

**Invariants**
- `purchasePrice >= 0`
- `freightCost >= 0`
- If `status == DISTRIBUTED` then `assignedPartnerId != null`.
- If `assignedPartnerId != null` then `status == DISTRIBUTED`.

### 4.2 Service (per Vehicle)
**Fields**
- `id: UUID`
- `vehicleId: UUID`
- `serviceType: enum` → `MECHANICAL | PAINT | BODYWORK | ELECTRICAL | UPHOLSTERY | WINDOWS`
- `description: string` (optional, max 500)
- `serviceValue: money` (required, >= 0)
- `performedAt: date` (optional; default today in UI)
- `createdAt, updatedAt`

> Note: The business list includes “service value” and “total value”.  
> For MVP, “total value” is a **computed** sum of services per vehicle and is returned by API as `servicesTotal`.

### 4.3 Document (per Vehicle)
**Fields**
- `id: UUID`
- `vehicleId: UUID`
- `documentType: enum` → `INVOICE | RECEIPT | SERVICE_ORDER | OTHER`
- `originalFileName: string`
- `contentType: string`
- `sizeBytes: long`
- `storageKey: string` (path/key in storage)
- `uploadedAt: instant`
- `uploadedBy: string` (optional for MVP; can be “system”)

**Storage**
- MVP acceptable options:
  - Local filesystem in dev (`/storage/vehicles/{vehicleId}/...`)
  - S3 in prod (recommended, but still “MVP-friendly”)

### 4.4 Partner Dealership
**Fields**
- `id: UUID`
- `name: string` (required, unique)
- `city: string` (optional)
- `commissionRate: decimal` (optional; out of scope for MVP settlement)
- `createdAt, updatedAt`

---

## 5) API Contracts (MVP)

> All endpoints prefixed with `/api/v1`.  
> All money values use **decimal** with 2 fraction digits in JSON.

### 5.1 Vehicle Registration
**Create vehicle**
- `POST /vehicles`
- Request:
  - licensePlate, renavam?, vin?, year, color, model, brand, supplierSource, purchasePrice, freightCost?
- Responses:
  - `201 Created` with `vehicleId`
  - `409 Conflict` if unique constraints violated
  - `400 Bad Request` validation errors

**Get vehicle list**
- `GET /vehicles?status=&q=&page=&size=`
- Supports:
  - filter by `status`
  - text search `q` across plate/model/brand
  - pagination

**Get vehicle details**
- `GET /vehicles/{vehicleId}`
- Returns:
  - vehicle fields
  - computed: `servicesTotal`, `documentsCount`

**Update vehicle**
- `PUT /vehicles/{vehicleId}`
- Can update:
  - year, color, model, brand, supplierSource, purchasePrice, freightCost
- Must not allow:
  - changing `licensePlate` once created (avoid re-identification issues in MVP)

**Transition status**
- `POST /vehicles/{vehicleId}/status`
- Request: `status`
- Rules:
  - `IN_LOT -> IN_SERVICE -> READY_FOR_DISTRIBUTION -> DISTRIBUTED`
  - Prevent skipping unless explicitly allowed (not allowed in MVP)
  - When setting `DISTRIBUTED`, must include `assignedPartnerId` (or call distribution endpoint below)

### 5.2 Services (per Vehicle)
**Add service**
- `POST /vehicles/{vehicleId}/services`
- Request: `serviceType`, `serviceValue`, `description?`, `performedAt?`
- Response: `201` with `serviceId`

**List services**
- `GET /vehicles/{vehicleId}/services`
- Returns list + `total` sum

**Update service**
- `PUT /vehicles/{vehicleId}/services/{serviceId}`
- Can update: type, value, description, performedAt

**Delete service**
- `DELETE /vehicles/{vehicleId}/services/{serviceId}`
- Response: `204 No Content`

### 5.3 Documents (per Vehicle)
**Upload document**
- `POST /vehicles/{vehicleId}/documents`
- Content-Type: `multipart/form-data`
- Fields:
  - `documentType`
  - `file`
- Response: `201` with `documentId`

**List documents**
- `GET /vehicles/{vehicleId}/documents`
- Returns metadata list

**Download document**
- `GET /vehicles/{vehicleId}/documents/{documentId}/download`
- Returns file stream

**Delete document**
- `DELETE /vehicles/{vehicleId}/documents/{documentId}`
- Response `204`

### 5.4 Distribution (assign to partner dealership)
**Assign vehicle to partner**
- `POST /vehicles/{vehicleId}/distribution`
- Request: `partnerId`
- Effects:
  - sets `assignedPartnerId`
  - sets `status = DISTRIBUTED`
- Rules:
  - Only allowed when `status == READY_FOR_DISTRIBUTION`
  - If already DISTRIBUTED, either:
    - return `409` (MVP simplest), OR
    - allow reassignment (not MVP)

### 5.5 Partners (minimal CRUD for reporting & distribution)
**Create partner**
- `POST /partners`
- Request: `name`, `city?`
- Response: `201`

**List partners**
- `GET /partners`

### 5.6 Reports
**Distributed vehicles report**
- `GET /reports/distributed-vehicles`
- Returns grouped result:
  - per partner:
    - `partnerId`, `partnerName`
    - `vehicles[]` with `vehicleId`, `licensePlate`, `brand`, `model`, `year`, `purchasePrice`, `totalCost`
    - `partnerVehiclesTotalValue` = sum of `purchasePrice` (or choose `totalCost`, see rules below)
    - `partnerVehiclesCount`
  - overall totals:
    - `overallVehiclesCount`, `overallVehiclesTotalValue`

**Report rules (MVP)**
- “value of each vehicle” = `purchasePrice` by default
- Also return `totalCost = purchasePrice + freightCost + servicesTotal`
- In UI, show both columns; totals default to purchasePrice but can toggle to totalCost.

---

## 6) Frontend UX Flows & Acceptance Criteria (Testable)

### Feature A — Vehicle Registration (Create + List + Detail)
**User Story**  
As a reseller, I want to register acquired vehicles so I can start inspection and maintenance tracking.

**Acceptance Criteria**
1. Vehicle list page shows:
   - table columns: Plate, Brand, Model, Year, Status, Purchase Price, Services Total, Total Cost, Assigned Partner
   - search input (plate/model/brand)
   - status filter chips (IN_LOT, IN_SERVICE, READY_FOR_DISTRIBUTION, DISTRIBUTED)
   - button “New Vehicle”
2. New Vehicle form validates:
   - required fields: plate, year, color, model, brand, supplierSource, purchasePrice
   - plate format: accepts common BR patterns (both old and Mercosul), and is uppercased on submit
   - numeric inputs cannot be negative
3. On save success:
   - show toast “Vehicle created”
   - redirect to `/vehicles/{id}`
4. Vehicle detail page has tabs:
   - Overview (vehicle fields + computed totals)
   - Services
   - Documents
   - Distribution
5. Errors:
   - if plate already exists → show “License plate already registered”
   - backend validation errors show under the correct form fields

**Backend Test Cases**
- Creating with missing required field returns 400 with field errors.
- Duplicate plate returns 409.
- Vehicle totals are computed correctly after services and freight updates.

**Frontend Test Cases**
- Create vehicle happy path.
- Create vehicle with invalid price shows inline error.
- List filters and search update results.

---

### Feature B — Service Management (Per Vehicle)
**User Story**  
As a reseller, I want to track all service costs per vehicle so I know total investment before resale.

**Acceptance Criteria**
1. In Vehicle detail → Services tab:
   - show service list (type, date, value, description)
   - show “Total Services” sum
   - show button “Add Service”
2. Add Service modal/form:
   - requires serviceType and serviceValue
   - value must be >= 0
   - optional description and performedAt
3. Edit service updates list and totals immediately after save.
4. Delete service asks confirmation and updates totals.
5. Vehicle status constraint:
   - adding services is allowed for status IN_LOT and IN_SERVICE
   - if status is DISTRIBUTED, services are read-only (MVP rule to avoid post-distribution cost changes)

**Backend Test Cases**
- Add service to missing vehicle returns 404.
- serviceValue negative returns 400.
- servicesTotal recomputes correctly.

**Frontend Test Cases**
- Add service → totals update.
- Delete service → totals update.
- Status DISTRIBUTED disables add/edit/delete actions.

---

### Feature C — Document Management (Vehicle Folder)
**User Story**  
As a reseller, I want to store documents per vehicle in a single place so I can find invoices and service orders quickly.

**Acceptance Criteria**
1. Documents tab shows:
   - upload area (drag & drop optional)
   - list of documents with: type, file name, size, uploaded date
   - actions: Download, Delete
2. Upload requires:
   - documentType selection
   - file (max size configurable; MVP default 20MB)
3. After upload:
   - show toast “Uploaded successfully”
   - documents list refreshes
4. Delete requires confirmation.
5. Special linking (MVP):
   - from Vehicle Overview, user can set “Invoice Document” and “Payment Receipt Document” by selecting among uploaded docs
   - selection updates the vehicle and shows as a link

**Backend Test Cases**
- Upload rejects files over max size.
- Upload to missing vehicle returns 404.
- Download returns correct content-type and filename.
- Delete removes metadata and storage object.

**Frontend Test Cases**
- Upload + list refresh.
- Download triggers file download.
- Delete removes from list.
- Set invoice/receipt links updates overview.

---

### Feature D — Distribution to Partner Dealership
**User Story**  
As a reseller, I want to assign a vehicle to a partner dealership to start negotiation with buyers.

**Acceptance Criteria**
1. Distribution tab shows:
   - current status
   - current assigned partner (if any)
   - partner dropdown (from `/partners`)
   - action button “Assign to Partner”
2. Assign is only enabled when:
   - status == READY_FOR_DISTRIBUTION
   - a partner is selected
3. After assigning:
   - status becomes DISTRIBUTED
   - assigned partner appears in vehicle list and detail
4. If user tries to assign when status not READY_FOR_DISTRIBUTION:
   - show clear error “Vehicle must be ready for distribution.”

**Backend Test Cases**
- Assign when status != READY_FOR_DISTRIBUTION returns 409.
- Assign sets partnerId and status DISTRIBUTED atomically.

**Frontend Test Cases**
- Ready vehicle can be assigned.
- Non-ready vehicle shows disabled UI and helpful text.

---

### Feature E — Reports & Dashboard (Distributed Vehicles)
**User Story**  
As a reseller, I want a report that shows which partner dealer each car is assigned to, including vehicle values and totals per partner.

**Acceptance Criteria**
1. Report page `/reports/distributed-vehicles` shows:
   - a table grouped by partner
   - for each partner: partner header with total value and count
   - inside group: vehicles list with plate, model, year, purchasePrice, totalCost
2. Show overall totals at top:
   - total distributed vehicles
   - total value (purchasePrice-based)
3. Provide toggle:
   - “Totals by: Purchase Price / Total Cost”
4. Export (optional MVP+):
   - CSV download for current view (if implemented)

**Backend Test Cases**
- Report returns only DISTRIBUTED vehicles.
- Totals per partner match sum of returned vehicles.
- Partners with zero vehicles are not included (or included with zero—choose one; MVP default: not included).

**Frontend Test Cases**
- Report renders grouped data.
- Toggle changes totals without refetch (if both totals included) or refetch (if server computed).

---

## 7) Non-Functional Requirements (MVP)
- Audit fields in DB for vehicles/services/documents/partners.
- Pagination for vehicle list.
- Consistent error format for validation and conflicts.
- Basic observability:
  - request logging with correlation id
  - structured logs (JSON recommended)

---

## 8) Seed Data & Local Dev
- Provide seed partners:
  - “Partner A”, “Partner B”
- Provide a sample vehicle dataset for UI development.
- Provide local storage folder for documents with Docker volume mapping.

---

## 9) Definition of Done
- All acceptance criteria above implemented.
- Backend: unit + integration tests pass.
- Frontend: component tests + core E2E flows pass.
- API documentation available (OpenAPI/Swagger).
- Linting/formatting passes (backend + frontend).

