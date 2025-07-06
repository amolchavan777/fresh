import React from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { CssBaseline, Typography, Container } from '@mui/material';

console.log('Simple MUI test starting');

const theme = createTheme({
  palette: {
    mode: 'dark',
  },
});

function App() {
  console.log('Simple MUI App rendering');
  
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Container>
        <Typography variant="h4" component="h1" gutterBottom>
          Material-UI Test
        </Typography>
        <Typography variant="body1">
          If you can see this styled text, Material-UI is working!
        </Typography>
      </Container>
    </ThemeProvider>
  );
}

export default App;
