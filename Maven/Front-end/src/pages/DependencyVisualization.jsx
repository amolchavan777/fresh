import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Card,
  CardContent,
  Box,
  Alert,
  Button,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  List,
  ListItem,
  ListItemText,
  Divider,
  Switch,
  FormControlLabel,
} from '@mui/material';
import {
  AccountTree,
  Warning,
  Refresh,
  Download,
  FilterList,
} from '@mui/icons-material';
import {
  ResponsiveContainer,
  Sankey,
  Tooltip as RechartsTooltip,
} from 'recharts';

const DependencyVisualization = () => {
  const [dependencies, setDependencies] = useState([]);
  const [graphStats, setGraphStats] = useState(null);
  const [cycles, setCycles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedApp, setSelectedApp] = useState('');
  const [filterType, setFilterType] = useState('all');
  const [showCyclesOnly, setShowCyclesOnly] = useState(false);

  // Mock data for demonstration
  const mockDependencies = [
    { id: 1, source: 'web-portal', target: 'user-service', type: 'API', confidence: 0.95 },
    { id: 2, source: 'web-portal', target: 'order-service', type: 'API', confidence: 0.90 },
    { id: 3, source: 'mobile-app', target: 'user-service', type: 'API', confidence: 0.88 },
    { id: 4, source: 'mobile-app', target: 'order-service', type: 'API', confidence: 0.85 },
    { id: 5, source: 'user-service', target: 'user-database', type: 'DATABASE', confidence: 0.98 },
    { id: 6, source: 'order-service', target: 'order-database', type: 'DATABASE', confidence: 0.96 },
    { id: 7, source: 'order-service', target: 'user-service', type: 'API', confidence: 0.82 },
    { id: 8, source: 'order-service', target: 'payment-service', type: 'API', confidence: 0.90 },
    { id: 9, source: 'payment-service', target: 'payment-database', type: 'DATABASE', confidence: 0.94 },
    { id: 10, source: 'notification-service', target: 'user-service', type: 'API', confidence: 0.87 },
  ];

  const mockStats = {
    nodeCount: 9,
    edgeCount: 10,
    cycleCount: 0,
    componentCount: 1,
  };

  const mockCycles = [
    // ['service-a', 'service-b', 'service-c', 'service-a'],
  ];

  useEffect(() => {
    // Simulate API call
    setTimeout(() => {
      setDependencies(mockDependencies);
      setGraphStats(mockStats);
      setCycles(mockCycles);
      setLoading(false);
    }, 1000);
  }, []);

  const getUniqueApplications = () => {
    const apps = new Set();
    dependencies.forEach(dep => {
      apps.add(dep.source);
      apps.add(dep.target);
    });
    return Array.from(apps).sort();
  };

  const getFilteredDependencies = () => {
    let filtered = dependencies;

    if (selectedApp) {
      filtered = filtered.filter(dep => 
        dep.source === selectedApp || dep.target === selectedApp
      );
    }

    if (filterType !== 'all') {
      filtered = filtered.filter(dep => dep.type === filterType);
    }

    return filtered;
  };

  const getDependencyTypes = () => {
    const types = new Set(dependencies.map(dep => dep.type));
    return Array.from(types).sort();
  };

  const getConfidenceColor = (confidence) => {
    if (confidence >= 0.9) return 'success';
    if (confidence >= 0.7) return 'warning';
    return 'error';
  };

  const exportDependencies = () => {
    const filtered = getFilteredDependencies();
    const data = filtered.map(dep => ({
      source: dep.source,
      target: dep.target,
      type: dep.type,
      confidence: dep.confidence,
    }));
    
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'dependencies.json';
    a.click();
    URL.revokeObjectURL(url);
  };

  if (loading) {
    return (
      <Container>
        <Box display="flex" justifyContent="center" mt={4}>
          <Typography>Loading dependency graph...</Typography>
        </Box>
      </Container>
    );
  }

  const filteredDependencies = getFilteredDependencies();

  return (
    <Container maxWidth="xl">
      <Typography variant="h4" gutterBottom>
        Dependency Visualization
      </Typography>

      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      {/* Graph Statistics */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Applications
              </Typography>
              <Typography variant="h4">
                {graphStats?.nodeCount || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Dependencies
              </Typography>
              <Typography variant="h4">
                {graphStats?.edgeCount || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Cycles Detected
              </Typography>
              <Typography variant="h4" color={cycles.length > 0 ? 'error' : 'success'}>
                {cycles.length}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Components
              </Typography>
              <Typography variant="h4">
                {graphStats?.componentCount || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* Filters and Controls */}
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                <FilterList sx={{ mr: 1 }} />
                Filters & Controls
              </Typography>
              
              <Box sx={{ mb: 2 }}>
                <FormControl fullWidth size="small">
                  <InputLabel>Focus Application</InputLabel>
                  <Select
                    value={selectedApp}
                    label="Focus Application"
                    onChange={(e) => setSelectedApp(e.target.value)}
                  >
                    <MenuItem value="">All Applications</MenuItem>
                    {getUniqueApplications().map(app => (
                      <MenuItem key={app} value={app}>{app}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Box>

              <Box sx={{ mb: 2 }}>
                <FormControl fullWidth size="small">
                  <InputLabel>Dependency Type</InputLabel>
                  <Select
                    value={filterType}
                    label="Dependency Type"
                    onChange={(e) => setFilterType(e.target.value)}
                  >
                    <MenuItem value="all">All Types</MenuItem>
                    {getDependencyTypes().map(type => (
                      <MenuItem key={type} value={type}>{type}</MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Box>

              <FormControlLabel
                control={
                  <Switch
                    checked={showCyclesOnly}
                    onChange={(e) => setShowCyclesOnly(e.target.checked)}
                  />
                }
                label="Show Cycles Only"
                disabled={cycles.length === 0}
              />

              <Divider sx={{ my: 2 }} />

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                <Button
                  variant="outlined"
                  startIcon={<Refresh />}
                  onClick={() => window.location.reload()}
                  size="small"
                >
                  Refresh Data
                </Button>
                <Button
                  variant="outlined"
                  startIcon={<Download />}
                  onClick={exportDependencies}
                  size="small"
                >
                  Export Data
                </Button>
              </Box>
            </CardContent>
          </Card>

          {/* Cycles Detection */}
          {cycles.length > 0 && (
            <Card sx={{ mt: 2 }}>
              <CardContent>
                <Typography variant="h6" gutterBottom color="error">
                  <Warning sx={{ mr: 1 }} />
                  Circular Dependencies
                </Typography>
                <List dense>
                  {cycles.map((cycle, index) => (
                    <ListItem key={index} divider={index < cycles.length - 1}>
                      <ListItemText
                        primary={`Cycle ${index + 1}`}
                        secondary={cycle.join(' → ')}
                      />
                    </ListItem>
                  ))}
                </List>
              </CardContent>
            </Card>
          )}
        </Grid>

        {/* Dependency List */}
        <Grid item xs={12} md={9}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                <AccountTree sx={{ mr: 1 }} />
                Dependencies ({filteredDependencies.length})
              </Typography>
              
              {filteredDependencies.length === 0 ? (
                <Typography color="textSecondary">
                  No dependencies match the current filters.
                </Typography>
              ) : (
                <List>
                  {filteredDependencies.map((dep, index) => (
                    <React.Fragment key={dep.id}>
                      <ListItem>
                        <ListItemText
                          primary={
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                              <Typography variant="body1">
                                <strong>{dep.source}</strong> → <strong>{dep.target}</strong>
                              </Typography>
                              <Chip 
                                label={dep.type} 
                                size="small" 
                                variant="outlined"
                              />
                              <Chip 
                                label={`${(dep.confidence * 100).toFixed(1)}%`}
                                size="small"
                                color={getConfidenceColor(dep.confidence)}
                              />
                            </Box>
                          }
                          secondary={`Confidence: ${dep.confidence.toFixed(3)}`}
                        />
                      </ListItem>
                      {index < filteredDependencies.length - 1 && <Divider />}
                    </React.Fragment>
                  ))}
                </List>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default DependencyVisualization;
