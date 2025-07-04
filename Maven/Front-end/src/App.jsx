import { useEffect, useState } from 'react';
import './App.css';

/**
 * Rule Management UI (React)
 *
 * - Fetches scoring rules from /api/rules (backend REST API)
 * - Displays rules in a table with editable numeric fields
 * - Allows user to update and save rules with validation and feedback
 * - Shows loading, error, and success states
 *
 * Main logic:
 *   - On mount, fetch rules and initialize state
 *   - On input change, update local edit state
 *   - On save, POST only changed numeric values to backend
 *   - On success, update UI and show feedback
 */
function App() {
  // Current rules from backend
  const [rules, setRules] = useState({});
  // Editable copy of rules
  const [editRules, setEditRules] = useState({});
  // UI state
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Fetch rules on mount
  useEffect(() => {
    fetch('/api/rules')
      .then(res => res.json())
      .then(data => {
        setRules(data.rules || data); // support both {rules: {...}} and {...}
        setEditRules(data.rules || data);
        setLoading(false);
      })
      .catch(() => {
        setError('Failed to load rules.');
        setLoading(false);
      });
  }, []);

  // Handle input change for a rule
  const handleChange = (key, value) => {
    setEditRules({ ...editRules, [key]: value });
  };

  // Save updated rules to backend
  const handleSave = () => {
    setSaving(true);
    setError('');
    setSuccess('');
    // Only send changed numeric values
    const updates = {};
    Object.keys(editRules).forEach(key => {
      if (!isNaN(editRules[key]) && editRules[key] !== rules[key]) {
        updates[key] = Number(editRules[key]);
      }
    });
    fetch('/api/rules', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(updates),
    })
      .then(res => res.json())
      .then(data => {
        setRules(data.rules || data);
        setEditRules(data.rules || data);
        setSuccess('Rules updated successfully!');
        setSaving(false);
      })
      .catch(() => {
        setError('Failed to save rules.');
        setSaving(false);
      });
  };

  if (loading) return <div className="container"><h2>Loading rules...</h2></div>;

  return (
    <div className="container">
      <h1>Rule Management</h1>
      <p>Edit and save scoring rules for your dependency matrix system.</p>
      {error && <div className="error">{error}</div>}
      {success && <div className="success">{success}</div>}
      <table>
        <thead>
          <tr>
            <th>Rule</th>
            <th>Value</th>
          </tr>
        </thead>
        <tbody>
          {Object.keys(editRules).map(key => (
            <tr key={key}>
              <td>{key}</td>
              <td>
                <input
                  type="number"
                  value={editRules[key]}
                  onChange={e => handleChange(key, e.target.value)}
                  disabled={saving}
                  step="any"
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <button onClick={handleSave} disabled={saving} style={{marginTop: '1rem'}}>
        {saving ? 'Saving...' : 'Save Rules'}
      </button>
    </div>
  );
}

export default App;
