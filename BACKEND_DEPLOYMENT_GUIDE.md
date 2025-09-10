# Backend Deployment Guide

## Quick Deploy to Render (Recommended)

### Step 1: Prepare Repository
Your backend is already set up correctly with:
- ✅ `package.json` with proper scripts
- ✅ `build` script that compiles TypeScript
- ✅ `start` script that runs the compiled app
- ✅ Environment variable support

### Step 2: Deploy to Render

1. **Create Render Account**
   - Go to [render.com](https://render.com)
   - Sign up with your GitHub account

2. **Create New Web Service**
   - Click "New +" → "Web Service"
   - Connect your GitHub repository: `UBC-CPEN321-Fall2025/M1-starter-app`
   - Set up the service:
     - **Name**: `cpen321-backend` (or your choice)
     - **Region**: Oregon (US West)
     - **Branch**: `main`
     - **Root Directory**: `backend`
     - **Runtime**: Node
     - **Build Command**: `npm install && npm run build`
     - **Start Command**: `npm start`

3. **Environment Variables**
   Add these in Render dashboard:
   ```
   NODE_ENV=production
   PORT=10000
   MONGODB_URI=your_mongodb_connection_string
   JWT_SECRET=your_jwt_secret
   GOOGLE_CLIENT_ID=your_google_client_id
   GOOGLE_CLIENT_SECRET=your_google_client_secret
   ```

4. **Deploy**
   - Click "Create Web Service"
   - Render will automatically build and deploy
   - You'll get a URL like: `https://cpen321-backend.onrender.com`

### Step 3: Update DEPLOYMENT.md
Once deployed, update the DEPLOYMENT.md file with your server URL.

## Alternative: Railway Deployment

If you prefer Railway:

1. Go to [railway.app](https://railway.app)
2. Sign in with GitHub
3. "New Project" → "Deploy from GitHub repo"
4. Select your repository
5. Set root directory to `backend`
6. Railway will auto-detect and deploy

## Alternative: Vercel Deployment

For Vercel:
1. Install Vercel CLI: `npm i -g vercel`
2. In the backend directory: `vercel`
3. Follow prompts

## Testing Your Deployment

Once deployed, test your API endpoints:
```bash
curl https://your-app-url.onrender.com/api/health
curl https://your-app-url.onrender.com/api/auth/user
```

## Important Notes

- **Database**: Ensure your MongoDB database is accessible from the cloud (MongoDB Atlas recommended)
- **Environment Variables**: All sensitive data should be in environment variables, not in code
- **CORS**: Make sure your CORS settings allow your frontend domain
- **File Uploads**: Cloud platforms have different file storage requirements
- **Keep Server Running**: Most free tiers sleep after inactivity, consider upgrading for consistent availability

## Troubleshooting

- **Build Fails**: Check that all dependencies are in `package.json`
- **Server Won't Start**: Verify `PORT` environment variable is set correctly
- **Database Connection**: Ensure MongoDB URI is correct and accessible
- **API Not Responding**: Check logs in the platform dashboard
