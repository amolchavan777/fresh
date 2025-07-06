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
  LinearProgress,
} from '@mui/material';
import {
  CloudUpload as UploadIcon,
  PlayArrow as ProcessIcon,
  ExpandMore as ExpandMoreIcon,
  CheckCircle as SuccessIcon,
  Error as ErrorIcon,
  Download as DownloadIcon,
  AttachFile as AttachFileIcon,
} from '@mui/icons-material';
import axios from 'axios';

const LogParser = () => {
  const [routerLogs, setRouterLogs] = useState('');
  const [codebaseDeps, setCodebaseDeps] = useState('');
  const [apiGatewayLogs, setApiGatewayLogs] = useState('');
  const [ciCdLogs, setCiCdLogs] = useState('');
  const [telemetryLogs, setTelemetryLogs] = useState('');
  const [processing, setProcessing] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [uploadedFiles, setUploadedFiles] = useState({
    router: null,
    codebase: null,
    apiGateway: null,
    cicd: null,
    telemetry: null,
  });
  const [parsingProgress, setParsingProgress] = useState(0);
  const [currentStep, setCurrentStep] = useState('');

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
    setParsingProgress(0);
    setCurrentStep('Preparing data...');
    
    try {
      const payload = {
        routerLogs: routerLogs.split('\n').filter(line => line.trim()),
        codebaseDeps: codebaseDeps.split('\n').filter(line => line.trim()),
        apiGatewayLogs: apiGatewayLogs.split('\n').filter(line => line.trim()),
        ciCdLogs: ciCdLogs.split('\n').filter(line => line.trim()),
        telemetryLogs: telemetryLogs.split('\n').filter(line => line.trim()),
      };
      
      setParsingProgress(20);
      setCurrentStep('Sending data to backend...');
      
      const response = await axios.post('/api/dependency-matrix/process', payload);
      
      setParsingProgress(100);
      setCurrentStep('Processing complete!');
      setResult(response.data);
    } catch (err) {
      setError(`Failed to process data: ${err.message}`);
    } finally {
      setProcessing(false);
      setTimeout(() => {
        setParsingProgress(0);
        setCurrentStep('');
      }, 2000);
    }
  };

  // File upload handlers
  const handleFileUpload = async (file, dataType) => {
    if (!file) return;
    
    try {
      const reader = new FileReader();
      reader.onload = (e) => {
        const content = e.target.result;
        switch (dataType) {
          case 'router':
            setRouterLogs(content);
            break;
          case 'codebase':
            setCodebaseDeps(content);
            break;
          case 'apiGateway':
            setApiGatewayLogs(content);
            break;
          case 'cicd':
            setCiCdLogs(content);
            break;
          case 'telemetry':
            setTelemetryLogs(content);
            break;
          default:
            break;
        }
        
        setUploadedFiles(prev => ({
          ...prev,
          [dataType]: file.name
        }));
      };
      
      reader.onerror = () => {
        setError(`Failed to read file: ${file.name}`);
      };
      
      reader.readAsText(file);
    } catch (error) {
      setError(`Error processing file: ${error.message}`);
    }
  };

  const downloadSampleFile = (dataType) => {
    let content = '';
    let filename = '';
    let mimeType = 'text/plain';

    switch (dataType) {
      case 'router':
        content = `2024-07-04T10:30:45Z [INFO] user-service -> auth-service:8080 HTTP GET /api/validate 200 125ms
2024-07-04T10:31:00Z [INFO] order-service -> inventory-service:8080 HTTP GET /api/inventory/check 200 80ms
2024-07-04T10:31:05Z [INFO] order-service -> payment-service:8080 HTTP POST /api/payment/process 200 300ms`;
        filename = 'sample-router-logs.log';
        break;
      case 'codebase':
        content = `<dependency><groupId>com.enterprise</groupId><artifactId>user-service-client</artifactId><version>2.1.0</version></dependency>
<dependency><groupId>com.enterprise</groupId><artifactId>order-service-client</artifactId><version>1.5.0</version></dependency>
npm:@company/shared-components:^3.2.1
gradle:com.enterprise:mobile-api-client:1.4.2`;
        filename = 'sample-codebase-deps.txt';
        break;
      case 'apiGateway':
        content = `{"timestamp":"2024-07-04T10:30:45Z","method":"GET","path":"/api/users","response_time":125}
{"timestamp":"2024-07-04T10:31:00Z","method":"POST","path":"/api/orders","response_time":200}`;
        filename = 'sample-api-gateway.json';
        mimeType = 'application/json';
        break;
      case 'cicd':
        content = `[2024-07-04T10:30:00.123Z] Job: deploy-user-service -> deploy-database SUCCESS
2024-07-04T10:31:30.000Z workflow: api-gateway-deployment depends_on: [user-service, order-service]`;
        filename = 'sample-cicd-logs.log';
        break;
      case 'telemetry':
        content = `http_requests_total{job="user-service",instance="user-service:8080",target="auth-service"} 1250.5
span{service.name="api-gateway",operation.name="route_request",peer.service="user-service"}`;
        filename = 'sample-telemetry.txt';
        break;
      default:
        return;
    }

    const blob = new Blob([content], { type: mimeType });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  const loadSampleData = () => {
    setRouterLogs(`2024-07-04T10:30:45Z [INFO] user-service -> auth-service:8080 HTTP GET /api/validate 200 125ms
2024-07-04T10:31:00Z [INFO] order-service -> inventory-service:8080 HTTP GET /api/inventory/check 200 80ms
2024-07-04T10:31:05Z [INFO] order-service -> payment-service:8080 HTTP POST /api/payment/process 200 300ms
2024-07-04T10:31:10Z [INFO] api-gateway -> user-service:8080 HTTP GET /api/users/profile 200 90ms
2024-07-04T10:31:15Z [INFO] mobile-app -> api-gateway:8080 HTTP POST /api/mobile/login 200 180ms`);

    setCodebaseDeps(`<dependency><groupId>com.enterprise</groupId><artifactId>user-service-client</artifactId><version>2.1.0</version></dependency>
<dependency><groupId>com.enterprise</groupId><artifactId>order-service-client</artifactId><version>1.5.0</version></dependency>
<dependency><groupId>com.enterprise</groupId><artifactId>inventory-service-client</artifactId><version>1.8.2</version></dependency>
npm:@company/shared-components:^3.2.1
gradle:com.enterprise:mobile-api-client:1.4.2
pip:enterprise-ml-utils==1.2.3`);

    setApiGatewayLogs(`{"timestamp":"2024-07-04T10:30:45Z","method":"GET","path":"/api/users","user_agent":"web-portal/1.0","response_time":125}
{"timestamp":"2024-07-04T10:31:00Z","method":"POST","path":"/api/orders","user_agent":"mobile-app/2.1","response_time":200}
{"timestamp":"2024-07-04T10:31:15Z","method":"GET","path":"/api/inventory","user_agent":"order-service/1.5","response_time":80}
{"timestamp":"2024-07-04T10:31:30Z","method":"GET","path":"/api/admin/dashboard","user_agent":"admin-portal/1.2","response_time":95}`);

    setCiCdLogs(`[2024-07-04T10:30:00.123Z] Job: deploy-user-service -> deploy-database SUCCESS
[2024-07-04T10:30:30.456Z] Job: deploy-order-service -> deploy-user-service SUCCESS
2024-07-04T10:31:30.000Z workflow: api-gateway-deployment depends_on: [user-service, order-service]
2024-07-04T10:32:30.000Z stage: integration-tests needs: [unit-tests, build-services]`);

    setTelemetryLogs(`http_requests_total{job="user-service",instance="user-service:8080",target="auth-service"} 1250.5
span{service.name="api-gateway",operation.name="route_request",peer.service="user-service",duration=125ms}
dependency{source="user-service",target="database",type="sql",response_time=45ms,success_rate=99.8}
TELEMETRY 2024-07-04T10:30:45Z analytics-service -> kafka-broker throughput=1250.5 msg/sec`);
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
                  sx={{ mr: 2 }}
                >
                  Process Sample
                </Button>
                <Button
                  variant="contained"
                  onClick={handleProcessData}
                  startIcon={<ProcessIcon />}
                  disabled={processing}
                  color="secondary"
                >
                  Process Custom Data
                </Button>
              </Box>

              {processing && parsingProgress > 0 && (
                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="textSecondary" gutterBottom>
                    {currentStep}
                  </Typography>
                  <LinearProgress variant="determinate" value={parsingProgress} />
                </Box>
              )}

              <Divider sx={{ my: 2 }} />

              {/* File Upload Sections */}
              <Accordion>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography>Router Logs</Typography>
                  {uploadedFiles.router && (
                    <Chip 
                      label={uploadedFiles.router} 
                      size="small" 
                      color="success" 
                      sx={{ ml: 2 }}
                    />
                  )}
                </AccordionSummary>
                <AccordionDetails>
                  <Box sx={{ mb: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                    <Button
                      variant="outlined"
                      component="label"
                      startIcon={<AttachFileIcon />}
                      size="small"
                    >
                      Upload File
                      <input
                        type="file"
                        hidden
                        accept=".log,.txt"
                        onChange={(e) => {
                          e.preventDefault();
                          handleFileUpload(e.target.files[0], 'router');
                          e.target.value = '';
                        }}
                      />
                    </Button>
                    <Button
                      variant="text"
                      startIcon={<DownloadIcon />}
                      size="small"
                      onClick={() => downloadSampleFile('router')}
                    >
                      Download Sample
                    </Button>
                  </Box>
                  <TextField
                    fullWidth
                    multiline
                    rows={4}
                    placeholder="Enter router log entries (one per line) or upload a file
Example: 2024-01-15T10:30:00Z [INFO] service-a -> service-b HTTP/1.1 200 OK latency=45ms"
                    value={routerLogs}
                    onChange={(e) => setRouterLogs(e.target.value)}
                  />
                </AccordionDetails>
              </Accordion>

              <Accordion>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography>Codebase Dependencies</Typography>
                  {uploadedFiles.codebase && (
                    <Chip 
                      label={uploadedFiles.codebase} 
                      size="small" 
                      color="success" 
                      sx={{ ml: 2 }}
                    />
                  )}
                </AccordionSummary>
                <AccordionDetails>
                  <Box sx={{ mb: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                    <Button
                      variant="outlined"
                      component="label"
                      startIcon={<AttachFileIcon />}
                      size="small"
                    >
                      Upload File
                      <input
                        type="file"
                        hidden
                        accept=".txt,.xml,.json"
                        onChange={(e) => {
                          e.preventDefault();
                          handleFileUpload(e.target.files[0], 'codebase');
                          e.target.value = '';
                        }}
                      />
                    </Button>
                    <Button
                      variant="text"
                      startIcon={<DownloadIcon />}
                      size="small"
                      onClick={() => downloadSampleFile('codebase')}
                    >
                      Download Sample
                    </Button>
                  </Box>
                  <TextField
                    fullWidth
                    multiline
                    rows={4}
                    placeholder="Enter codebase dependency entries (one per line) or upload a file
Example: <dependency><groupId>com.enterprise</groupId><artifactId>service-a</artifactId><version>1.0.0</version></dependency>"
                    value={codebaseDeps}
                    onChange={(e) => setCodebaseDeps(e.target.value)}
                  />
                </AccordionDetails>
              </Accordion>

              <Accordion>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography>API Gateway Logs</Typography>
                  {uploadedFiles.apiGateway && (
                    <Chip 
                      label={uploadedFiles.apiGateway} 
                      size="small" 
                      color="success" 
                      sx={{ ml: 2 }}
                    />
                  )}
                </AccordionSummary>
                <AccordionDetails>
                  <Box sx={{ mb: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                    <Button
                      variant="outlined"
                      component="label"
                      startIcon={<AttachFileIcon />}
                      size="small"
                    >
                      Upload File
                      <input
                        type="file"
                        hidden
                        accept=".json,.log,.txt"
                        onChange={(e) => {
                          e.preventDefault();
                          handleFileUpload(e.target.files[0], 'apiGateway');
                          e.target.value = '';
                        }}
                      />
                    </Button>
                    <Button
                      variant="text"
                      startIcon={<DownloadIcon />}
                      size="small"
                      onClick={() => downloadSampleFile('apiGateway')}
                    >
                      Download Sample
                    </Button>
                  </Box>
                  <TextField
                    fullWidth
                    multiline
                    rows={4}
                    placeholder="Enter API gateway log entries (one per line) or upload a file
Example: {&quot;timestamp&quot;:&quot;2024-07-04T10:30:45Z&quot;,&quot;method&quot;:&quot;GET&quot;,&quot;path&quot;:&quot;/api/users&quot;}"
                    value={apiGatewayLogs}
                    onChange={(e) => setApiGatewayLogs(e.target.value)}
                  />
                </AccordionDetails>
              </Accordion>

              <Accordion>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography>CI/CD Pipeline Logs</Typography>
                  {uploadedFiles.cicd && (
                    <Chip 
                      label={uploadedFiles.cicd} 
                      size="small" 
                      color="success" 
                      sx={{ ml: 2 }}
                    />
                  )}
                </AccordionSummary>
                <AccordionDetails>
                  <Box sx={{ mb: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                    <Button
                      variant="outlined"
                      component="label"
                      startIcon={<AttachFileIcon />}
                      size="small"
                    >
                      Upload File
                      <input
                        type="file"
                        hidden
                        accept=".log,.txt"
                        onChange={(e) => {
                          e.preventDefault();
                          handleFileUpload(e.target.files[0], 'cicd');
                          e.target.value = '';
                        }}
                      />
                    </Button>
                    <Button
                      variant="text"
                      startIcon={<DownloadIcon />}
                      size="small"
                      onClick={() => downloadSampleFile('cicd')}
                    >
                      Download Sample
                    </Button>
                  </Box>
                  <TextField
                    fullWidth
                    multiline
                    rows={4}
                    placeholder="Enter CI/CD pipeline log entries (one per line) or upload a file
Example: [2024-07-04T10:30:00.123Z] Job: deploy-user-service -> deploy-database SUCCESS"
                    value={ciCdLogs}
                    onChange={(e) => setCiCdLogs(e.target.value)}
                  />
                </AccordionDetails>
              </Accordion>

              <Accordion>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography>Telemetry & Monitoring</Typography>
                  {uploadedFiles.telemetry && (
                    <Chip 
                      label={uploadedFiles.telemetry} 
                      size="small" 
                      color="success" 
                      sx={{ ml: 2 }}
                    />
                  )}
                </AccordionSummary>
                <AccordionDetails>
                  <Box sx={{ mb: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                    <Button
                      variant="outlined"
                      component="label"
                      startIcon={<AttachFileIcon />}
                      size="small"
                    >
                      Upload File
                      <input
                        type="file"
                        hidden
                        accept=".txt,.log,.metrics,.json"
                        onChange={(e) => {
                          e.preventDefault();
                          handleFileUpload(e.target.files[0], 'telemetry');
                          e.target.value = '';
                        }}
                      />
                    </Button>
                    <Button
                      variant="text"
                      startIcon={<DownloadIcon />}
                      size="small"
                      onClick={() => downloadSampleFile('telemetry')}
                    >
                      Download Sample
                    </Button>
                  </Box>
                  <TextField
                    fullWidth
                    multiline
                    rows={4}
                    placeholder="Enter telemetry data entries (one per line) or upload a file
Example: http_requests_total job=user-service target=auth-service 1250.5"
                    value={telemetryLogs}
                    onChange={(e) => setTelemetryLogs(e.target.value)}
                  />
                </AccordionDetails>
              </Accordion>

              <Box sx={{ mt: 2 }}>
                <Button
                  variant="contained"
                  onClick={handleProcessData}
                  disabled={processing || (!routerLogs && !codebaseDeps && !apiGatewayLogs && !ciCdLogs && !telemetryLogs)}
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
