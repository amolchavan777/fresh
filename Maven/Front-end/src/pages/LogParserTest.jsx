import React, { useState } from 'react';
import {
  Container,
  Typography,
  Button,
  Box,
} from '@mui/material';

const LogParserTest = () => {
  const [message, setMessage] = useState('LogParser component is working!');

  return (
    <Container maxWidth="xl">
      <Typography variant="h4" gutterBottom>
        Test Log Parser
      </Typography>
      <Box sx={{ mt: 2 }}>
        <Typography>{message}</Typography>
        <Button 
          variant="contained" 
          onClick={() => setMessage('Button clicked!')}
          sx={{ mt: 2 }}
        >
          Test Button
        </Button>
      </Box>
    </Container>
  );
};

export default LogParserTest;
