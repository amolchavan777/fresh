import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

/**
 * Entry point for the React app.
 * - Renders the App component inside the #root element.
 * - Uses React StrictMode for highlighting potential problems.
 */
createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
