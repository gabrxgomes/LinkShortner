# 🚀 Vercel Deployment Guide - LinkShort

## Prerequisites

- GitHub account
- Vercel account (sign up at https://vercel.com)
- Project pushed to GitHub

---

## Step 1: Create Vercel Postgres Database

1. Go to https://vercel.com/dashboard
2. Click on **Storage** tab
3. Click **Create Database**
4. Select **Postgres**
5. Choose a name: `linkshort-db`
6. Select region closest to your users
7. Click **Create**

---

## Step 2: Get Database Credentials

After creating the database:

1. Go to the database page
2. Click on **.env.local** tab
3. You'll see environment variables like:
   ```
   POSTGRES_URL="postgresql://..."
   POSTGRES_USER="default"
   POSTGRES_PASSWORD="..."
   ```
4. **Keep this tab open** - you'll need these values

---

## Step 3: Push Code to GitHub

```bash
# Initialize git (if not already)
git init

# Add all files
git add .

# Commit
git commit -m "Initial commit - LinkShort URL Shortener"

# Create repository on GitHub
# Then add remote and push
git remote add origin https://github.com/YOUR_USERNAME/linkshort.git
git branch -M main
git push -u origin main
```

---

## Step 4: Import Project to Vercel

1. Go to https://vercel.com/dashboard
2. Click **Add New** → **Project**
3. Import your GitHub repository
4. Configure the project:
   - **Framework Preset:** Other
   - **Root Directory:** `./`
   - **Build Command:** `mvn clean package -DskipTests`
   - **Output Directory:** `target`

---

## Step 5: Add Environment Variables

In the Vercel project settings, add these environment variables:

### Required Variables:

```
POSTGRES_URL=<from Step 2>
POSTGRES_USER=<from Step 2>
POSTGRES_PASSWORD=<from Step 2>
```

### Optional Variables:

```
BASE_URL=https://your-project.vercel.app
SPRING_PROFILES_ACTIVE=production
```

**How to add:**
1. Go to **Settings** tab
2. Click **Environment Variables**
3. Add each variable (Name and Value)
4. Click **Add**

---

## Step 6: Connect Database to Project

1. In Vercel dashboard, go to your project
2. Go to **Storage** tab
3. Click **Connect Store**
4. Select your Postgres database
5. Click **Connect**

This will automatically add the database environment variables.

---

## Step 7: Deploy

1. Click **Deploy** button
2. Wait for build to complete (3-5 minutes first time)
3. Once deployed, click **Visit** to see your site!

---

## Step 8: Configure Custom Domain (Optional)

1. Go to **Settings** → **Domains**
2. Add your custom domain
3. Follow DNS configuration instructions
4. Update `BASE_URL` environment variable to your custom domain

---

## Troubleshooting

### Build Fails

**Error:** Maven not found
- Check Build Command is correct: `mvn clean package -DskipTests`

**Error:** Java version mismatch
- Vercel uses Java 17 by default (which matches our project)

### Database Connection Fails

**Check:**
1. Environment variables are set correctly
2. Database is in the same region as deployment
3. Check Vercel logs: **Deployments** → Click deployment → **Function Logs**

### Application Errors

**View Logs:**
1. Go to **Deployments**
2. Click on latest deployment
3. Click **Function Logs**
4. Look for errors in Spring Boot startup

---

## Testing Your Deployment

1. **Create a link:**
   - Visit your Vercel URL
   - Paste a URL and click "Shorten Link"

2. **Test redirection:**
   - Click on the generated short URL
   - Should redirect to original URL

3. **Check statistics:**
   - Should show real numbers from database
   - Create a few links to see numbers change

4. **Test QR Code:**
   - Download QR code
   - Scan with phone - should redirect

---

## Monitoring

### Check Database

1. Go to **Storage** → Your database
2. Click **Data** tab
3. You can see tables and data
4. Run SQL queries to check:

```sql
SELECT * FROM links ORDER BY created_at DESC LIMIT 10;
SELECT COUNT(*) as total_links FROM links;
SELECT SUM(click_count) as total_clicks FROM links;
```

### Check Logs

1. Go to **Deployments** → Latest deployment
2. Click **Function Logs**
3. Filter by error/warning if needed

---

## Updating Your App

When you push to GitHub, Vercel automatically redeploys:

```bash
# Make changes to code
git add .
git commit -m "Update feature X"
git push
```

Vercel will:
1. Detect the push
2. Build automatically
3. Deploy new version
4. Zero downtime (keeps old version running until new is ready)

---

## Cost Estimation

### Free Tier Limits:
- ✅ **Vercel Postgres:** 256 MB storage (enough for ~100K links)
- ✅ **Vercel Hosting:** 100 GB bandwidth/month
- ✅ **Functions:** 100 GB-hours compute
- ✅ **Deployments:** Unlimited

### When to Upgrade:
- More than 100K links
- More than 100 GB traffic/month
- Need more compute power

---

## Security Recommendations

### Before Production:

1. **Enable HTTPS only** (Vercel does this automatically)

2. **Add Rate Limiting:**
   - Currently not implemented
   - Consider adding Spring Rate Limiter

3. **Restrict CORS:**
   - Update `SecurityConfig.java` to allow only your domain

4. **Add API Key (Optional):**
   - For administrative endpoints
   - Protect system stats if needed

5. **Enable Database Backups:**
   - Vercel provides automatic backups
   - Check **Storage** → Database → **Backups**

---

## Performance Tips

1. **Database Indexing** (already done):
   - `shortCode` is indexed (unique)
   - Queries are optimized

2. **Connection Pooling** (configured):
   - Max pool size: 5 connections
   - Enough for hobby/small projects

3. **Monitor Performance:**
   - Use Vercel Analytics
   - Check function execution time

---

## Support & Resources

- **Vercel Docs:** https://vercel.com/docs
- **Vercel Postgres:** https://vercel.com/docs/storage/vercel-postgres
- **Spring Boot on Vercel:** https://vercel.com/docs/frameworks/java

---

## Quick Commands Reference

```bash
# Local testing
mvn spring-boot:run

# Local with production profile
mvn spring-boot:run -Dspring.profiles.active=production

# Build for deployment
mvn clean package -DskipTests

# Check application logs (after deploy)
vercel logs <deployment-url>
```

---

**Your LinkShort app is now ready for production! 🎉**

Need help? Check Vercel dashboard → **Support** for assistance.
