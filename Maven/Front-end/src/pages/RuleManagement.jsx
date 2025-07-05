import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Card,
  CardContent,
  TextField,
  Button,
  Alert,
  Box,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  Grid,
  Slider,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  IconButton,
  Tooltip,
} from '@mui/material';
import { Save, Refresh, RestoreSharp } from '@mui/icons-material';

const RuleManagement = () => {
  const [rules, setRules] = useState({});
  const [editRules, setEditRules] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [presetProfile, setPresetProfile] = useState('');

  // Predefined rule presets
  const rulePresets = {
    conservative: {
      baseConfidence: 0.3,
      codebaseWeight: 0.95,
      routerLogWeight: 0.7,
      apiGatewayWeight: 0.6,
      recencyFactor: 0.5,
      frequencyBonus: 0.1,
      confidenceThreshold: 0.8,
    },
    balanced: {
      baseConfidence: 0.5,
      codebaseWeight: 0.9,
      routerLogWeight: 0.8,
      apiGatewayWeight: 0.7,
      recencyFactor: 0.7,
      frequencyBonus: 0.15,
      confidenceThreshold: 0.6,
    },
    aggressive: {
      baseConfidence: 0.7,
      codebaseWeight: 0.85,
      routerLogWeight: 0.9,
      apiGatewayWeight: 0.8,
      recencyFactor: 0.9,
      frequencyBonus: 0.2,
      confidenceThreshold: 0.4,
    },
  };

  // Rule descriptions for user guidance
  const ruleDescriptions = {
    baseConfidence: 'Default confidence score for new dependencies',
    codebaseWeight: 'Weight for dependencies found in source code',
    routerLogWeight: 'Weight for dependencies found in router logs',
    apiGatewayWeight: 'Weight for dependencies found in API gateway logs',
    recencyFactor: 'How much recent data is preferred over old data',
    frequencyBonus: 'Bonus multiplier for frequently observed dependencies',
    confidenceThreshold: 'Minimum confidence required to accept a dependency',
  };

  useEffect(() => {
    fetchRules();
  }, []);

  const fetchRules = () => {
    setLoading(true);
    fetch('/api/rules')
      .then(res => res.json())
      .then(data => {
        const rulesData = data.rules || data;
        setRules(rulesData);
        setEditRules({ ...rulesData });
        setLoading(false);
      })
      .catch(() => {
        // Fallback to default rules if API fails
        const defaultRules = rulePresets.balanced;
        setRules(defaultRules);
        setEditRules({ ...defaultRules });
        setError('Could not connect to backend. Using default rules.');
        setLoading(false);
      });
  };

  const handleRuleChange = (key, value) => {
    setEditRules({ ...editRules, [key]: value });
    setError('');
    setSuccess('');
  };

  const handlePresetChange = (preset) => {
    if (preset && rulePresets[preset]) {
      setEditRules({ ...rulePresets[preset] });
      setPresetProfile(preset);
      setSuccess(`Applied ${preset} preset configuration`);
    }
  };

  const handleSave = () => {
    setSaving(true);
    setError('');
    setSuccess('');

    // Validate rules
    const validationError = validateRules(editRules);
    if (validationError) {
      setError(validationError);
      setSaving(false);
      return;
    }

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
        const rulesData = data.rules || data;
        setRules(rulesData);
        setEditRules({ ...rulesData });
        setSuccess('Rules updated successfully!');
        setSaving(false);
      })
      .catch(() => {
        setError('Failed to save rules. Backend may be unavailable.');
        setSaving(false);
      });
  };

  const handleReset = () => {
    setEditRules({ ...rules });
    setError('');
    setSuccess('');
    setPresetProfile('');
  };

  const validateRules = (rulesToValidate) => {
    for (const [key, value] of Object.entries(rulesToValidate)) {
      if (isNaN(value)) {
        return `Invalid value for ${key}: must be a number`;
      }
      if (key.includes('Weight') || key.includes('confidence') || key.includes('Factor')) {
        if (value < 0 || value > 1) {
          return `${key} must be between 0 and 1`;
        }
      }
    }
    return null;
  };

  const hasChanges = () => {
    return Object.keys(editRules).some(key => editRules[key] !== rules[key]);
  };

  if (loading) {
    return (
      <Container>
        <Box display="flex" justifyContent="center" mt={4}>
          <Typography>Loading rules...</Typography>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Typography variant="h4" gutterBottom>
        Rule Management
      </Typography>
      <Typography variant="body1" color="textSecondary" paragraph>
        Configure scoring rules for the dependency matrix inference engine.
        These rules control how confident the system is in different types of dependencies.
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}

      <Grid container spacing={3}>
        {/* Preset Configurations */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Quick Presets
              </Typography>
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                {Object.keys(rulePresets).map(preset => (
                  <Button
                    key={preset}
                    variant={presetProfile === preset ? 'contained' : 'outlined'}
                    onClick={() => handlePresetChange(preset)}
                    sx={{ textTransform: 'capitalize' }}
                  >
                    {preset}
                  </Button>
                ))}
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Rule Configuration */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6">
                  Scoring Rules
                </Typography>
                <Box>
                  <Tooltip title="Reset to saved values">
                    <IconButton onClick={handleReset} disabled={!hasChanges()}>
                      <RestoreSharp />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Refresh from server">
                    <IconButton onClick={fetchRules}>
                      <Refresh />
                    </IconButton>
                  </Tooltip>
                </Box>
              </Box>

              <TableContainer component={Paper} variant="outlined">
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Rule</TableCell>
                      <TableCell>Description</TableCell>
                      <TableCell>Value</TableCell>
                      <TableCell>Slider</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {Object.keys(editRules).map(key => (
                      <TableRow key={key}>
                        <TableCell>
                          <Typography variant="body2" fontWeight="medium">
                            {key}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2" color="textSecondary">
                            {ruleDescriptions[key] || 'Custom scoring rule'}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <TextField
                            type="number"
                            value={editRules[key]}
                            onChange={e => handleRuleChange(key, parseFloat(e.target.value))}
                            disabled={saving}
                            size="small"
                            inputProps={{
                              step: 0.01,
                              min: 0,
                              max: key.includes('Weight') || key.includes('confidence') || key.includes('Factor') ? 1 : 10,
                            }}
                            sx={{ width: 100 }}
                          />
                        </TableCell>
                        <TableCell>
                          {(key.includes('Weight') || key.includes('confidence') || key.includes('Factor')) && (
                            <Box sx={{ width: 200 }}>
                              <Slider
                                value={editRules[key] || 0}
                                onChange={(_, value) => handleRuleChange(key, value)}
                                disabled={saving}
                                min={0}
                                max={1}
                                step={0.01}
                                marks={[
                                  { value: 0, label: '0' },
                                  { value: 0.5, label: '0.5' },
                                  { value: 1, label: '1' },
                                ]}
                                size="small"
                              />
                            </Box>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>

              <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
                <Button
                  variant="contained"
                  onClick={handleSave}
                  disabled={saving || !hasChanges()}
                  startIcon={<Save />}
                >
                  {saving ? 'Saving...' : 'Save Rules'}
                </Button>
                <Button
                  variant="outlined"
                  onClick={handleReset}
                  disabled={!hasChanges()}
                >
                  Reset Changes
                </Button>
              </Box>

              {hasChanges() && (
                <Alert severity="warning" sx={{ mt: 2 }}>
                  You have unsaved changes. Click "Save Rules" to apply them.
                </Alert>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default RuleManagement;
