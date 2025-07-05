import React, { useState, useEffect } from 'react';
import {
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  Box,
  CircularProgress,
  Alert,
  Button,
  Chip,
} from '@mui/material';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  LineChart,
  Line,
} from 'recharts';
import { Refresh as RefreshIcon } from '@mui/icons-material';
import axios from 'axios';

const Dashboard = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchDashboardData = async () => {
    setLoading(true);
    setError('');
    try {
      // Fetch summary data from backend
      const summaryResponse = await axios.get('/api/dependency-matrix/summary');
      
      // Fetch health status
      const healthResponse = await axios.get('/api/dependency-matrix/health');
      
      // Process the data into dashboard format
      const processedStats = {
        totalApplications: summaryResponse.data.totalApplications || 0,
        totalDependencies: summaryResponse.data.totalDependencies || 0,
        cyclesDetected: summaryResponse.data.cyclesDetected || 0,
        lastProcessed: summaryResponse.data.lastProcessed || new Date().toISOString(),
        systemStatus: healthResponse.data.status || 'UNKNOWN',
        sourceBreakdown: [
          { name: 'Codebase', value: summaryResponse.data.codebaseCount || 0, color: '#8884d8' },
          { name: 'Router Logs', value: summaryResponse.data.routerLogCount || 0, color: '#82ca9d' },
          { name: 'API Gateway', value: summaryResponse.data.apiGatewayCount || 0, color: '#ffc658' },
          { name: 'CI/CD', value: summaryResponse.data.cicdCount || 0, color: '#ff7300' },
        ],
        dependencyTypes: [
          { name: 'API', count: summaryResponse.data.apiDependencies || 0 },
          { name: 'Database', count: summaryResponse.data.databaseDependencies || 0 },
          { name: 'Build', count: summaryResponse.data.buildDependencies || 0 },
          { name: 'Runtime', count: summaryResponse.data.runtimeDependencies || 0 },
        ],
        processingHistory: summaryResponse.data.processingHistory || [
          { date: '2025-07-01', processed: 0 },
          { date: '2025-07-02', processed: 0 },
          { date: '2025-07-03', processed: 0 },
          { date: '2025-07-04', processed: 0 },
          { date: '2025-07-05', processed: summaryResponse.data.totalDependencies || 0 },
        ],
      };
      
      setStats(processedStats);
    } catch (err) {
      console.error('Failed to fetch dashboard data:', err);
      // Fallback to mock data if backend is not available
      setStats({
        totalApplications: 24,
        totalDependencies: 156,
        cyclesDetected: 3,
        lastProcessed: new Date().toISOString(),
        systemStatus: 'HEALTHY',
        sourceBreakdown: [
          { name: 'Codebase', value: 68, color: '#8884d8' },
          { name: 'Router Logs', value: 45, color: '#82ca9d' },
          { name: 'API Gateway', value: 32, color: '#ffc658' },
          { name: 'CI/CD', value: 11, color: '#ff7300' },
        ],
        dependencyTypes: [
          { name: 'API', count: 78 },
          { name: 'Database', count: 24 },
          { name: 'Build', count: 35 },
          { name: 'Runtime', count: 19 },
        ],
        processingHistory: [
          { date: '2025-07-01', processed: 120 },
          { date: '2025-07-02', processed: 135 },
          { date: '2025-07-03', processed: 142 },
          { date: '2025-07-04', processed: 156 },
          { date: '2025-07-05', processed: 156 },
        ],
      });
      setError('Using offline data - backend connection failed');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  if (loading) {
    return (
      <Container>
        <Box display="flex" justifyContent="center" mt={4}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  if (error) {
    return (
      <Container>
        <Alert severity="error">{error}</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">
          Dependency Matrix Dashboard
        </Typography>
        <Button
          variant="outlined"
          onClick={fetchDashboardData}
          startIcon={<RefreshIcon />}
          disabled={loading}
        >
          Refresh
        </Button>
      </Box>
      
      {error && (
        <Alert severity="warning" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}
      
      {/* Key Metrics */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Total Applications
              </Typography>
              <Typography variant="h4">
                {stats.totalApplications}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Total Dependencies
              </Typography>
              <Typography variant="h4">
                {stats.totalDependencies}
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
              <Typography variant="h4" color="error">
                {stats.cyclesDetected}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Last Processed
              </Typography>
              <Typography variant="body1">
                {new Date(stats.lastProcessed).toLocaleString()}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Charts */}
      <Grid container spacing={3}>
        {/* Source Breakdown */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Dependencies by Source
              </Typography>
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={stats.sourceBreakdown}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ name, value }) => `${name}: ${value}`}
                    outerRadius={80}
                    fill="#8884d8"
                    dataKey="value"
                  >
                    {stats.sourceBreakdown.map((entry) => (
                      <Cell key={`cell-${entry.name}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>

        {/* Dependency Types */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Dependency Types
              </Typography>
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={stats.dependencyTypes}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="count" fill="#646cff" />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>

        {/* Processing History */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Processing History (Last 5 Days)
              </Typography>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={stats.processingHistory}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Line 
                    type="monotone" 
                    dataKey="processed" 
                    stroke="#646cff" 
                    strokeWidth={2}
                    name="Dependencies Processed"
                  />
                </LineChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Dashboard;
