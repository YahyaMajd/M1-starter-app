const http = require('http');

// Note: In a real test, you would get a valid JWT token from login
// For this test, we'll just test the endpoint structure
const testToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.signature'; // Mock token

// Test logout endpoint
function testLogout() {
  const data = JSON.stringify({});

  const options = {
    hostname: 'localhost',
    port: 3000,
    path: '/auth/logout',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${testToken}`,
      'Content-Length': Buffer.byteLength(data)
    }
  };

  const req = http.request(options, (res) => {
    console.log(`\n=== LOGOUT TEST ===`);
    console.log(`Status: ${res.statusCode}`);
    console.log(`Status Text: ${res.statusCode === 200 ? 'OK' : 'ERROR'}`);

    let responseData = '';
    res.on('data', (chunk) => {
      responseData += chunk;
    });

    res.on('end', () => {
      console.log('\n--- Response Body ---');
      console.log(responseData);
      
      try {
        const jsonResponse = JSON.parse(responseData);
        console.log('\n--- Parsed Response ---');
        console.log('Message:', jsonResponse.message);
        console.log('Data:', jsonResponse.data);
        
        if (res.statusCode === 200 && jsonResponse.message) {
          console.log('\n✅ Logout endpoint is working correctly!');
        } else {
          console.log('\n❌ Logout endpoint has issues');
        }
      } catch (e) {
        console.log('\n❌ Could not parse JSON response');
        console.log('Raw response:', responseData);
      }
      
      console.log('\n=== TEST COMPLETE ===\n');
    });
  });

  req.on('error', (error) => {
    console.error('\n❌ Connection Error:', error.message);
    console.log('Make sure the backend server is running on http://localhost:3000');
  });

  req.write(data);
  req.end();
}

console.log('🧪 Testing logout endpoint...');
console.log('📡 Connecting to http://localhost:3000/auth/logout');
testLogout();
