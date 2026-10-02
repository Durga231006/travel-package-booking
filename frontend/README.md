# Travel Package Booking - Frontend (VS Code)

Plain HTML, CSS and JavaScript. No install or build step.

## Files
- `index.html`  page structure (tabs, tables, forms)
- `style.css`   design
- `app.js`      calls the Spring Boot REST API and fills the page

## How to run
1. Start MySQL and the Spring Boot backend first (it must run on http://localhost:8080).
2. Open this folder in VS Code: File > Open Folder > `travel-frontend`.
3. Install the extension **Live Server** (by Ritwick Dey).
4. Right-click `index.html` > **Open with Live Server**.
5. The browser opens at http://127.0.0.1:5500 and shows the bookings.

Do not double-click `index.html` to open it as a file. The browser treats that as a different
origin and blocks the API calls. Use Live Server.

## If the backend is on another port
Edit the first lines of `app.js`:
```javascript
const API_BASE = 'http://localhost:8080/api';
```
