import React, { useState } from 'react';
import {
  Container,
  Typography,
  Grid,
  Card,
  CardContent,
  TextField,
  Button,
  Box,
  Alert,
  CircularProgress,
  Chip,
  Divider,
  Accordion,
  AccordionSummary,
  AccordionDetails,
} from '@mui/material';
import {
  CloudUpload as UploadIcon,
  PlayArrow as ProcessIcon,
  ExpandMore as ExpandMoreIcon,
  CheckCircle as SuccessIcon,
  Error as ErrorIcon,
} from '@mui/icons-material';
import axios from 'axios';

const LogParser = () => {
  const [routerLogs, setRouterLogs] = useState('');
  const [codebaseDeps, setCodebaseDeps] = useState('');
  const [apiGatewayLogs, setApiGatewayLogs] = useState('');
  const [processing, setProcessing] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const handleSampleData = async () => {
    setProcessing(true);
    setError('');
    try {
      const response = await axios.get('/api/dependency-matrix/sample');
      setResult(response.data);
    } catch (err) {
      setError(`Failed to process sample data: ${err.message}`);
    } finally {
      setProcessing(false);
    }
  };

  const handleProcessData = async () => {
    setProcessing(true);
    setError('');
    try {
      const payload = {
        routerLogs: routerLogs.split('\n').filter(line => line.trim()),
        codebaseDeps: codebaseDeps.split('\n').filter(line => line.trim()),
        apiGatewayLogs: apiGatewayLogs.split('\n').filter(line => line.trim()),
      };
      
      const response = await axios.post('/api/dependency-matrix/process', payload);
      setResult(response.data);
    } catch (err) {
      setError(`Failed to process data: ${err.message}`);
    } finally {
      setProcessing(false);
    }
  };

  const loadSampleData = () => {
    setRouterLogs(`2024-01-15T10:30:00Z [INFO] user-service -> auth-service HTTP/1.1 200 OK latency=25ms
2024-01-15T10:30:05Z [INFO] auth-service -> database-service HTTP/1.1 200 OK latency=15ms
2024-01-15T10:30:10Z [INFO] user-service -> notification-service HTTP/1.1 200 OK latency=100ms`);

    setCodebaseDeps(`<dependency><groupId>com.enterprise</groupId><artifactId>user-service</artifactId><version>2.0.0</version></dependency>
<dependency><groupId>com.enterprise</groupId><artifactId>auth-service</artifactId><version>1.5.0</version></dependency>`);

    setApiGatewayLogs(`2024-01-15T10:29:58Z [TRACE] api-gateway routing request from mobile-app to user-service
2024-01-15T10:30:12Z [TRACE] api-gateway routing request from web-app to user-service`);
  };

  return (
    <Container maxWidth="xl">
      <Typography variant="h4" gutterBottom>
        Log Parser & Data Processor
      </Typography>
      <Typography variant="body1" color="textSecondary" sx={{ mb: 4 }}>
        Process dependency data from multiple sources to generate dependency matrices
      </Typography>

      <Grid container spacing={3}>
        {/* Input Section */}
        <Grid item xs={12} lg={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Data Input
              </Typography>
              
              <Box sx={{ mb: 2 }}>
                <Button
                  variant="outlined"
                  onClick={loadSampleData}
                  startIcon={<UploadIcon />}
                  sx={{ mr: 2 }}
                >
                  Load Sample Data
                </Button>
                <Button
                  variant="contained"
                  onClick={handleSampleData}
                  startIcon={<ProcessIcon />}
                  disabled={processing}
                >
                  Process Sample
                </Button>
              </Box>

              <Divider sx={{ my: 2 }} />

              <Accordion>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography>Router Logs</Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <TextField
                    fullWidth
                    multiline
                    rows={4}
                    placeholder="Enter router log entries (one per line)
Example: 2024-01-15T10:30:00Z [INFO] service-a -> service-b HTTP/1.1 200 OK latency=45ms"
                    value={routerLogs}
                    onChange={(e) => setRouterLogs(e.target.value)}
                  />
                </AccordionDetails>
              </Accordion>

              <Accordion>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography>Codebase Dependencies</Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <TextField
                    fullWidth
                    multiline
                    rows={4}
                    placeholder="Enter codebase dependency entries (one per line)
Example: <dependency><groupId>com.enterprise</groupId><artifactId>service-a</artifactId><version>1.0.0</version></dependency>"
                    value={codebaseDeps}
                    onChange={(e) => setCodebaseDeps(e.target.value)}
                  />
                </AccordionDetails>
              </Accordion>

              <Accordion>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography>API Gateway Logs</Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <TextField
                    fullWidth
                    multiline
                    rows={4}
                    placeholder="Enter API gateway log entries (one per line)
Example: 2024-01-15T10:29:58Z [TRACE] gateway-1 routing request from client-app to service-a"
                    value={apiGatewayLogs}
                    onChange={(e) => setApiGatewayLogs(e.target.value)}
                  />
                </AccordionDetails>
              </Accordion>

              <Box sx={{ mt: 2 }}>
                <Button
                  variant="contained"
                  onClick={handleProcessData}
                  disabled={processing || (!routerLogs && !codebaseDeps && !apiGatewayLogs)}
                  startIcon={processing ? <CircularProgress size={20} /> : <ProcessIcon />}
                  fullWidth
                >
                  {processing ? 'Processing...' : 'Process Custom Data'}
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Results Section */}
        <Grid item xs={12} lg={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Processing Results
              </Typography>

              {error && (
                <Alert severity="error" sx={{ mb: 2 }} icon={<ErrorIcon />}>
                  {error}
                </Alert>
              )}

              {result && (
                <Box>
                  <Alert severity="success" sx={{ mb: 2 }} icon={<SuccessIcon />}>
                    Dependency matrix generated successfully!
                  </Alert>

                  <Box sx={{ mb: 2 }}>
                    <Typography variant="subtitle2" gutterBottom>
                      Matrix Information
                    </Typography>
                    <Chip label={`ID: ${result.matrixId}`} size="small" sx={{ mr: 1 }} />
                    <Chip label={`Generated: ${new Date(result.timestamp).toLocaleString()}`} size="small" />
                  </Box>

                  {result.summary && (
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="subtitle2" gutterBottom>
                        Summary
                      </Typography>
                      <Typography variant="body2" color="textSecondary">
                        {result.summary}
                      </Typography>
                    </Box>
                  )}

                  {result.dependencies && result.dependencies.length > 0 && (
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="subtitle2" gutterBottom>
                        Dependencies Found ({result.dependencies.length})
                      </Typography>
                      <Box sx={{ maxHeight: 300, overflow: 'auto' }}>
                        {result.dependencies.map((dep, index) => {
                          const confidenceValue = dep.confidenceScore.value;
                          let confidenceColor = 'error';
                          if (confidenceValue > 0.8) confidenceColor = 'success';
                          else if (confidenceValue > 0.5) confidenceColor = 'warning';
                          
                          return (
                            <Box key={`${dep.sourceAppId}-${dep.targetAppId}-${index}`} sx={{ mb: 1, p: 1, bgcolor: 'background.default', borderRadius: 1 }}>
                              <Typography variant="body2">
                                <strong>{dep.sourceAppId}</strong> → <strong>{dep.targetAppId}</strong>
                              </Typography>
                              <Box sx={{ display: 'flex', gap: 1, mt: 0.5 }}>
                                <Chip label={dep.type} size="small" variant="outlined" />
                                <Chip 
                                  label={`Confidence: ${(confidenceValue * 100).toFixed(1)}%`} 
                                  size="small" 
                                  color={confidenceColor}
                                />
                              </Box>
                            </Box>
                          );
                        })}
                      </Box>
                    </Box>
                  )}

                  {result.graph && (
                    <Box>
                      <Typography variant="subtitle2" gutterBottom>
                        Graph Statistics
                      </Typography>
                      <Grid container spacing={1}>
                        <Grid item xs={6}>
                          <Chip label={`Nodes: ${result.graph.nodeCount || 0}`} size="small" />
                        </Grid>
                        <Grid item xs={6}>
                          <Chip label={`Edges: ${result.graph.edgeCount || 0}`} size="small" />
                        </Grid>
                      </Grid>
                    </Box>
                  )}
                </Box>
              )}

              {!result && !error && !processing && (
                <Typography variant="body2" color="textSecondary" textAlign="center">
                  No data processed yet. Load sample data or enter custom data to get started.
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Container>
  );
};

export default LogParser;
