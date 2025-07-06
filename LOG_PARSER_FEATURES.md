# Log Parser - Enhanced File Upload Features

## Overview
The Log Parser page has been enhanced to support traditional file uploads for all five data sources, allowing users to process dependency data from multiple file types in real-time.

## Features

### 1. Traditional File Upload Support
- **Router Logs**: Upload `.log` or `.txt` files containing router/network traffic logs
- **Codebase Dependencies**: Upload `.xml`, `.json`, `.txt`, or `.gradle` files with dependency definitions
- **API Gateway Logs**: Upload `.json`, `.log`, or `.txt` files with API gateway access logs
- **CI/CD Logs**: Upload `.log`, `.txt`, or `.yml` files with pipeline execution logs  
- **Telemetry & Monitoring**: Upload `.txt`, `.log`, `.metrics`, or `.json` files with monitoring data

### 2. Multi-File Type Support
Each data source accepts multiple file formats to accommodate different logging systems and dependency formats.

### 3. Sample File Downloads
Each section includes a "Download Sample" button that provides lightweight example files:

#### Router Logs Sample (`sample-router-logs.log`)
```
2024-07-04T10:30:45Z [INFO] user-service -> auth-service:8080 HTTP GET /api/validate 200 125ms
2024-07-04T10:31:00Z [INFO] order-service -> inventory-service:8080 HTTP GET /api/inventory/check 200 80ms
```

#### Codebase Dependencies Sample (`sample-codebase-deps.txt`)
```
<dependency><groupId>com.enterprise</groupId><artifactId>user-service-client</artifactId><version>2.1.0</version></dependency>
npm:@company/shared-components:^3.2.1
gradle:com.enterprise:mobile-api-client:1.4.2
```

#### API Gateway Logs Sample (`sample-api-gateway.json`)
```json
{"timestamp":"2024-07-04T10:30:45Z","method":"GET","path":"/api/users","response_time":125}
{"timestamp":"2024-07-04T10:31:00Z","method":"POST","path":"/api/orders","response_time":200}
```

#### CI/CD Logs Sample (`sample-cicd-logs.log`)
```
[2024-07-04T10:30:00.123Z] Job: deploy-user-service -> deploy-database SUCCESS
2024-07-04T10:31:30.000Z workflow: api-gateway-deployment depends_on: [user-service, order-service]
```

#### Telemetry Sample (`sample-telemetry.txt`)
```
http_requests_total{job="user-service",instance="user-service:8080",target="auth-service"} 1250.5
span{service.name="api-gateway",operation.name="route_request",peer.service="user-service"}
```

### 4. Real-Time Processing
- Upload files using the "Upload File" buttons
- Files are automatically parsed and content loaded into text areas
- Visual feedback shows uploaded file names as success chips
- Click "Process Custom Data" button to generate dependency matrix in real-time
- Processing progress shown with loading indicators

### 5. User Experience Enhancements
- **Visual Feedback**: Uploaded files display as green chips with file names
- **Progress Indicators**: Loading states and progress bars during processing
- **Error Handling**: Clear error messages if processing fails
- **Flexible Input**: Support both file upload and manual text entry
- **Sample Data**: Quick "Load Sample Data" and "Process Sample" buttons for testing

## Usage Workflow

1. **Upload Files**: Use "Upload File" buttons to select files from your system
2. **Download Samples**: Use "Download Sample" buttons to get example file formats
3. **Review Content**: Uploaded file content appears in text areas for review/editing
4. **Process Data**: Click "Process Custom Data" to generate dependency matrix
5. **View Results**: Generated matrix information appears in the results panel

## File Format Requirements

### Router Logs
- Format: `timestamp [level] source -> destination:port protocol method path status response_time`
- Extensions: `.log`, `.txt`

### Codebase Dependencies  
- Format: XML dependencies, npm packages, gradle dependencies, pip packages
- Extensions: `.xml`, `.json`, `.txt`, `.gradle`

### API Gateway Logs
- Format: JSON objects with timestamp, method, path, response_time
- Extensions: `.json`, `.log`, `.txt`

### CI/CD Logs
- Format: Timestamped job executions and workflow dependencies
- Extensions: `.log`, `.txt`, `.yml`

### Telemetry & Monitoring
- Format: Prometheus metrics, spans, dependency telemetry
- Extensions: `.txt`, `.log`, `.metrics`, `.json`

## Quick Start

1. Navigate to the Log Parser page: http://localhost:5173/log-parser
2. Click "Download Sample" for any data source to get example files
3. Use "Upload File" to select your own files or modify the sample data
4. Click "Process Custom Data" to generate the dependency matrix
5. View results in the right panel

The enhanced Log Parser provides a comprehensive solution for processing dependency data from multiple sources with an intuitive file upload interface.
