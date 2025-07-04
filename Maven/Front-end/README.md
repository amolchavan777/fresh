# Rule Management Frontend (React + Vite)

This project is a modern React + Vite frontend for managing dynamic scoring rules in your Application Dependency Matrix system.

---

## 📁 Project Structure

```
Front-end/
├── .github/
│   └── copilot-instructions.md   # Custom Copilot instructions for this workspace
├── .vscode/                      # VS Code tasks and settings
├── public/
│   └── vite.svg                  # Vite logo asset
├── src/
│   ├── App.jsx                   # Main rule management UI (fully documented)
│   ├── App.css                   # App-specific styles
│   ├── index.css                 # Global styles
│   ├── main.jsx                  # React entry point (fully documented)
│   └── assets/
│       └── react.svg             # React logo asset
├── package.json                  # Project metadata and scripts
├── vite.config.js                # Vite config (proxy, plugins, documented)
├── start-all.sh                  # Script to start backend and frontend together
├── README.md                     # This file
└── ...
```

---

## 🚀 Getting Started

1. **Install dependencies:**
   ```sh
   npm install
   ```
2. **Start the backend:**
   ```sh
   cd ../dependency-matrix
   mvn spring-boot:run
   ```
3. **Start the frontend:**
   ```sh
   cd ../Front-end
   npm run dev
   ```
   Or run both together:
   ```sh
   ./start-all.sh
   ```
4. **Open the app:**
   Visit [http://localhost:5173](http://localhost:5173) in your browser.

---

## 🧩 File/Folder Details

- **src/App.jsx**: Main UI for rule management. Fetches rules from `/api/rules`, displays them in a table, allows editing and saving. Fully documented with logic and flow.
- **src/main.jsx**: Entry point for React app. Renders `App` in `#root` with React StrictMode. Fully documented.
- **src/App.css, src/index.css**: Styling for the app and global styles.
- **vite.config.js**: Vite config. Proxies `/api` to backend, uses React plugin. Fully documented.
- **start-all.sh**: Bash script to start both backend and frontend together. Kills backend when frontend stops.
- **.github/copilot-instructions.md**: Custom Copilot instructions for this workspace.

---

## 🔄 Logic Flow

1. **App loads:**
   - `App.jsx` fetches rules from `/api/rules` (backend REST API).
   - Rules are displayed in a table with editable numeric fields.
2. **User edits rules:**
   - Changes are tracked in local state (`editRules`).
3. **User clicks Save:**
   - Only changed numeric values are sent to backend via POST `/api/rules`.
   - On success, UI updates and shows feedback.
4. **Proxy:**
   - All `/api` requests are proxied to backend (`http://localhost:8080`) via `vite.config.js`.

---

## 🛠️ Useful Commands

- `npm install` — Install dependencies
- `npm run dev` — Start frontend dev server
- `npm run build` — Build for production
- `npm run lint` — Lint code
- `./start-all.sh` — Start both backend and frontend together

---

## 📝 Customization & Extending

- Edit `src/App.jsx` to add new rules, validation, or UI features.
- Update backend REST API as needed for new rule types.
- Style with `src/App.css` or `src/index.css`.

---

## 💡 Tips
- Ensure backend is running and accessible at `http://localhost:8080` for API calls to work.
- For production, update proxy or CORS settings as needed.
- All code is commented for easy onboarding and maintenance.

---
