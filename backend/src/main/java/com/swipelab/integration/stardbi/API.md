# SwipeLab API

Base URL: `/swipe_lab/`

All endpoints require a valid JWT token in the `Authorization` header:
```
Authorization: Bearer <access_token>
```

Tokens are obtained from `POST /auth/get_token/` with `{"username": ..., "password": ...}`.

---

## Authentication roles

| Role | Who | Used by |
|------|-----|---------|
| **Any party** | A Django user with any `ExperimentParties` record | Experiment list |
| **Modifier** | A Django user with `ExperimentParties.modifier = True` for the requested experiment | Crop metadata, crop image, bulk download |
| **SwipeLab server** | The dedicated `swipe_lab` service account | Post label, taxonomy |

---

## Endpoints

### 1. Experiment List

```
GET /swipe_lab/experiments/
```

Returns all experiments the authenticated user has any party role in (modifier, contributor, identifier, or bounding box modifier). Superusers receive all experiments.

**Auth:** Any authenticated user with at least one `ExperimentParties` record.

**Response `200 OK`**

```json
[
  {
    "id": 24,
    "name": "a very long name for exp",
    "start_date": "2023-06-19",
    "end_date": "2023-06-19",
    "notes": "b"
  }
]
```

| Field | Type | Description |
|-------|------|-------------|
| `id` | integer | Experiment ID — use this in all other endpoints |
| `name` | string | Display name |
| `start_date` | date | Experiment start |
| `end_date` | date | Experiment end |
| `notes` | string | Free-text notes |

---

### 3. Crop Metadata List

```
GET /swipe_lab/crops/?experiment=<id>
```

Returns the full list of bounding boxes for an experiment, with the most recent human identification per box if one exists.

**Auth:** Modifier in the requested experiment.

**Query parameters**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `experiment` | integer | Yes | Experiment ID |

**Response `200 OK`**

Array of objects, one per bounding box:

```json
[
  { "box_id": 39475, "image_id": 922, "species_id": 1 },
  { "box_id": 43926, "image_id": 922, "species_id": null }
]
```

| Field | Type | Description |
|-------|------|-------------|
| `box_id` | integer | Bounding box ID |
| `image_id` | integer | Parent `TrapImage` ID |
| `species_id` | integer or null | Most recent `Identification.species_id` for this box; `null` if never identified |

**Errors**

| Status | Condition |
|--------|-----------|
| `400` | `experiment` parameter missing |
| `403` | Authenticated user is not a modifier in this experiment |
| `404` | Experiment not found |

---

### 5. Bulk Crop Download

```
GET /swipe_lab/crops/download/?experiment=<id>
```

Downloads all bounding box crops for an experiment as a single ZIP file. Each crop is computed on the fly from the full trap image. Files are named `<image_id>_<box_id>.jpeg`. Each source image is opened only once regardless of how many boxes it contains.

**Auth:** Modifier in the requested experiment.

**Query parameters**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `experiment` | integer | Yes | Experiment ID |

**Response `200 OK`**

```
Content-Type: application/zip
Content-Disposition: attachment; filename="experiment_<id>_crops.zip"
```

Binary ZIP body — streamed in 8 KB chunks. Files inside: `<image_id>_<box_id>.jpeg` for every bounding box in the experiment. Boxes whose source image file is missing on disk are silently skipped.

**Errors**

| Status | Condition |
|--------|-----------|
| `400` | `experiment` parameter missing |
| `403` | Authenticated user is not a modifier in this experiment |
| `404` | Experiment not found |

---

### 4. Crop Image

```
GET /swipe_lab/crops/<box_id>/image/
```

Returns the image crop for a single bounding box as a JPEG file. The crop is computed on the fly from the full trap image using the box coordinates `(x, y, w, h)`.

**Auth:** Modifier in the experiment that owns this box.

**Path parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `box_id` | integer | Bounding box ID |

**Response `200 OK`**

```
Content-Type: image/jpeg
Content-Disposition: attachment; filename="<image_id>_<box_id>.jpeg"
```

Binary JPEG body — the cropped region `img[y : y+h, x : x+w]`.

**Errors**

| Status | Condition |
|--------|-----------|
| `403` | Authenticated user is not a modifier in this box's experiment |
| `404` | Box ID not found |
| `404` | Image file missing from disk |
| `500` | JPEG encoding failed |

---

### 6. Post Label

```
POST /swipe_lab/labels/
Content-Type: application/json
```

Records a SwipeLab user's identification of a bounding box. If the same user has already labelled the same box, the existing record is updated (upsert). Also updates the user's grade if the value has changed.

**Auth:** `swipe_lab` service account only.

**Request body**

```json
{
  "box_id":            39475,
  "image_id":          922,
  "species_id":        1,
  "swipe_lab_user_id": "user_abc",
  "user_grade":        3
}
```

| Field | Type | Description |
|-------|------|-------------|
| `box_id` | integer | Bounding box being labelled |
| `image_id` | integer | Expected parent image of the box — validated server-side |
| `species_id` | integer | Species assigned by the user |
| `swipe_lab_user_id` | string | SwipeLab's own user identifier, max 100 chars (auto-created in `SwipeLabUser` on first appearance) |
| `user_grade` | integer | Current grade of the user — updated only if it differs from the stored value |

All fields are required.

**Response `201 Created`** — new label

```json
{ "label_id": 1 }
```

**Response `200 OK`** — existing label updated (same `box_id` + `swipe_lab_user_id`)

```json
{ "label_id": 1 }
```

**Errors**

| Status | Condition |
|--------|-----------|
| `400` | Any required field is missing |
| `400` | `image_id` does not match the box's actual parent image |
| `403` | Authenticated user is not the `swipe_lab` service account |
| `404` | `box_id` not found |
| `404` | `species_id` not found |

---

### 7. Taxonomy List

```
GET /swipe_lab/taxonomy/
```

Returns the complete species list with the full taxonomy hierarchy flattened into a single object per species. Intended for use as a lookup table on the SwipeLab server.

**Auth:** `swipe_lab` service account only.

**Response `200 OK`**

```json
[
  {
    "species_id": 1,
    "species":    "Apis mellifera",
    "genus":      "Apis",
    "family":     "Apidae",
    "order":      "Hymenoptera",
    "class":      "Insecta"
  }
]
```

| Field | Type | Description |
|-------|------|-------------|
| `species_id` | integer | Primary key of the `Species` record |
| `species` | string | Species name |
| `genus` | string | Genus name |
| `family` | string | Family name |
| `order` | string | Order name |
| `class` | string | Class name |

**Errors**

| Status | Condition |
|--------|-----------|
| `403` | Authenticated user is not the `swipe_lab` service account |