# UX/UI Context — Vehicle MVP Adjustments

This document describes **UX/UI requirements and behaviors** for the MVP screens of the Used Car Reselling System.

It is designed to be used by an AI agent (e.g., Cursor) to implement:
- **React 18 + TypeScript + TailwindCSS** pages and components
- Proper **validation**, **masking**, and **error feedback**
- UX patterns consistent across all modules

> Notes:
> - API rules and database constraints are defined in feature-context and application-context documents.
> - This document focuses on the **frontend experience** and how UI must behave.

---

## 1) Global UX/UI Standards

### 1.1 Form Behavior (All Screens)
**Rules**
1. All required fields must be visually marked (e.g., `*`).
2. Field-level validation must be displayed:
   - on submit
   - and on blur (recommended)
3. A form submit must:
   - disable the submit button while the request is in progress
   - show a loading state (text or spinner)
4. Success feedback:
   - show a toast message (e.g., “Saved successfully”)
   - redirect only after success response
5. Error feedback:
   - display backend validation errors under the correct fields
   - display backend conflicts (409) in a clear way to the user

**Acceptance Criteria**
- ✅ All forms block submission if required fields are missing.
- ✅ User always knows what went wrong and where.
- ✅ Double submits are prevented.

---

### 1.2 Monetary Values (Mask + Formatting)
**Rules**
1. Monetary inputs must support:
   - typing with **2 decimal places**
   - values like: `1000`, `1000.50`, `0.99`
2. Negative values must not be accepted.
3. UI must render monetary values using:
   - two decimal digits
   - right alignment inside tables (recommended)

**Acceptance Criteria**
- ✅ User can type and edit prices smoothly.
- ✅ UI values match backend persisted values with 2 decimals.

---

### 1.3 Combobox Fields (Suggest + Create New)
Some fields must behave as:
- dropdown suggestions from backend
- but still allow typing a new value

**Rules**
1. Combobox must support:
   - search filtering while typing
   - selecting an existing option
   - entering a new option not in the list
2. If user enters a new value:
   - UI sends it as plain string to backend
   - backend is responsible to persist it and return it in future suggestions
3. Field remains required unless otherwise stated.

**Acceptance Criteria**
- ✅ Suggestions load from backend successfully.
- ✅ New typed values are accepted and saved.
- ✅ Next time user opens the screen, new values appear in suggestions.

---

### 1.4 Tables (Listing Pages)
**Rules**
1. Every list page must support:
   - loading state (skeleton or spinner)
   - empty state message (no records)
   - basic pagination
2. Table rows must support click → navigate to detail page.

**Acceptance Criteria**
- ✅ User can navigate quickly between list and details.
- ✅ Empty lists are understandable and not confusing.

---

## 2) Vehicle Pages — UX/UI Context

### 2.1 Vehicles List Page (`/vehicles`)
**Main Goal**
Allow the reseller to quickly visualize vehicles in the lot and access details.

**UI Components**
- Search input (`plate`, `model`, `brand`)
- Status filter (chips/select)
- Table of vehicles
- Primary action: **New Vehicle**

**Table Columns**
- Plate
- Brand
- Model
- Year
- Status
- Purchase Price
- Freight
- Services Total
- Total Cost
- Assigned Partner (if any)

**Acceptance Criteria**
- ✅ Search updates results.
- ✅ Filter by status works.
- ✅ Clicking a row navigates to vehicle details.

---

### 2.2 New Vehicle Page (`/vehicles/new`)
**Main Goal**
Create a new vehicle entry with correct identity fields and initial cost.

#### 2.2.1 Fields
**Required**
- licensePlate
- year
- color
- brand
- model
- supplierSource
- purchasePrice

**Optional**
- renavam
- vin
- freightCost (default: `0.00`)

#### 2.2.2 Default Values
**Rules**
1. `year` must default to the **current year**.
2. `freightCost` should default to `0.00`.

**Acceptance Criteria**
- ✅ Opening the page already pre-fills `year = currentYear`.
- ✅ Freight starts at `0.00`.

#### 2.2.3 Monetary Fields
**Rules**
- `purchasePrice` and `freightCost` must use **money mask with 2 decimals**.

**Acceptance Criteria**
- ✅ User cannot type invalid monetary formats.
- ✅ Values are sent as decimals to API.

#### 2.2.4 Combobox Fields (Color, Brand, Model)
**Rules**
- `color`, `brand`, `model` must show suggestions from backend and allow typing new values.

**Acceptance Criteria**
- ✅ Suggestions appear for all 3 fields.
- ✅ Typing “new brand” works and saves successfully.
- ✅ New values appear in future suggestions automatically.

#### 2.2.5 Conflicts and Validation
**Rules**
1. If backend returns 409 conflict for:
   - licensePlate already used
   - vin already used
   - renavam already used  
   UI must show a clear message like:
   - “This license plate is already registered.”

**Acceptance Criteria**
- ✅ Duplicate plate shows user-friendly error.
- ✅ The user can fix and submit again.

---

### 2.3 Vehicle Detail Page (`/vehicles/:id`)
**Main Goal**
Manage the vehicle lifecycle and see total investment.

**Tabs**
- Overview
- Services
- Documents
- Distribution

#### Overview Tab
Must display:
- identity fields (plate, brand, model, year, etc.)
- purchasePrice, freightCost
- computed totals:
  - servicesTotal
  - totalCost = purchasePrice + freightCost + servicesTotal

**Acceptance Criteria**
- ✅ Totals are always visible and correct.
- ✅ Monetary values are consistently formatted.

---

## 3) Services UX Context (Per Vehicle)

### 3.1 Services Tab
**Main Goal**
Allow tracking all service costs and compute totals.

**UI Components**
- Services list/table
- Total services value (sum)
- Add Service button

**Acceptance Criteria**
- ✅ Add, update, delete updates totals instantly after success.
- ✅ Negative service values are blocked in UI.

---

## 4) Documents UX Context (Per Vehicle)

### 4.1 Documents Tab
**Main Goal**
Attach and manage files for each vehicle.

**UI Components**
- Upload control (documentType + file)
- Document list
- Actions: Download / Delete

**Acceptance Criteria**
- ✅ Upload shows success toast and refreshes list.
- ✅ Delete requires confirmation.

---

## 5) Distribution UX Context (Per Vehicle)

### 5.1 Distribution Tab
**Main Goal**
Assign a vehicle to a partner dealership.

**UI Components**
- Partner dropdown
- Assign button
- Current assigned partner info

**Rules**
1. Assign button enabled only when:
   - status == READY_FOR_DISTRIBUTION
   - partner selected

**Acceptance Criteria**
- ✅ Non-ready vehicles show disabled assign button with explanation.
- ✅ Successful assign updates status and assigned partner.

---

## 6) Reports UX Context

### 6.1 Distributed Vehicles Report Page (`/reports/distributed-vehicles`)
**Main Goal**
See which vehicles are distributed per partner dealership.

**UI**
- Grouped table by partner
- Partner totals:
  - vehicles count
  - total value
- Overall totals at top

**Acceptance Criteria**
- ✅ Grouping is readable.
- ✅ Totals match backend response.
