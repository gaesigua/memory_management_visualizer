# Interactive Memory Management Visualizer (IMMV)

An educational full-stack web application that demonstrates classic operating systems memory allocation strategies in real time. It provides visual memory maps, address translation tables, and fragmentation metrics for multiple allocation models.

## Features

- Fixed partitioning with internal fragmentation tracking
- Best-fit dynamic partitioning with coalescing of free blocks
- Paging with page table visualization
- Segmentation with segment table visualization
- Live metrics for used memory, internal fragmentation, and external fragmentation

## Tech Stack

- Backend: Java 21, Spring Boot, Maven
- Frontend: React 19, TypeScript, Vite
- Deployment targets: Render for backend, Vercel for frontend

## Project Structure

- `backend/` Spring Boot REST API and memory allocation logic
- `frontend/` React UI and browser-side visualization

## Local Setup

### Prerequisites

- Java Development Kit (JDK) 21 or higher
- Node.js 18+ and npm

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

The backend runs on `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend runs on `http://localhost:5173`.

## API Endpoints

- `POST /api/v1/memory/init?strategy={fixed|best-fit|paging|segmentation}&totalSize=1024`
- `POST /api/v1/memory/allocate?processId=P1&size=200`
- `POST /api/v1/memory/deallocate?processId=P1`
- `GET /api/v1/memory/state`

## Deployment

### Live Deployment

- Frontend: replace with your Vercel deployment URL
- Backend: https://memory-management-visualizer.onrender.com

### Backend on Render

Render deploys this backend as a Dockerized Web Service.

1. Create a new Render Web Service and connect this repository.
2. Choose the `Docker` runtime.
3. Set the root directory to the repository root.
4. Use the Dockerfile at `backend/Dockerfile`.
5. Add no custom environment variables unless you later introduce external services.
6. Deploy the service and use the generated Render URL in the frontend env config.

### Frontend on Vercel

1. Import the `frontend/` folder as a Vercel project.
2. Set the build command to `npm run build`.
3. Set the output directory to `dist`.
4. Add an environment variable named `VITE_API_BASE_URL` with your Render backend URL, for example `https://your-backend.onrender.com`.

### CORS Notes

- The backend allows local Vite development origins and Vercel preview/production domains.
- If you use a custom frontend domain, add it to the backend CORS configuration.

### Render with Docker

If you prefer blueprint deployment, Render can also use the included `render.yaml`, but the Dockerfile is the primary deployment path for the backend.

## Production Checklist

- Confirm the frontend points to `https://memory-management-visualizer.onrender.com` through `VITE_API_BASE_URL`.
- Verify allocate, deallocate, and refresh work in the deployed frontend.
- Keep the backend deployed from `backend/Dockerfile` so Render uses the Java runtime correctly.
- Update the live URLs here if either deployment target changes.

## Usage

1. Start with the default Best-Fit strategy or switch strategies from the UI.
2. Enter a process ID and memory size, then allocate or deallocate it.
3. Use the Refresh action to re-sync the browser view with the backend state.

## Demo Scenario

1. Initialize Best-Fit.
2. Allocate `P1` with `200 KB`.
3. Allocate `P2` with `400 KB`.
4. Deallocate `P1` to create a free hole.
5. Allocate `P4` with `240 KB` and observe how fragmentation changes.

