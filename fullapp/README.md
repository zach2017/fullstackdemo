# A comprehensive Docker Compose setup with all 6 core services 

## **Services Included:**

1. **Keycloak Database** (PostgreSQL) - For authentication data
2. **Keycloak** - Identity and Access Management server
3. **PostGIS Database** - PostgreSQL with spatial extensions for your backend
4. **Java Spring Backend** - With OAuth2 integration and PostGIS support  
5. **React Vite Frontend** - Development server with hot reloading
6. **Nginx** - Reverse proxy controlling ingress/egress traffic

## **Key Features:**

- **Network Isolation**: Backend databases are on an internal network for security
- **Health Checks**: All services have proper health monitoring
- **CORS Configuration**: Properly configured for cross-origin requests
- **Rate Limiting**: API and authentication endpoint protection
- **Security Headers**: XSS, CSRF, and other security protections
- **Hot Reloading**: Development-friendly setup with live code updates

## **Service URLs:**
- Main App: http://localhost (port 80) 
- Backend API: http://localhost/api
- Keycloak: http://localhost/auth
- Health Check: http://localhost/health

The Nginx configuration handles all traffic routing, CORS headers, rate limiting, and security headers. The setup is production-ready with proper volume persistence, health checks, and restart policies.

To get started, create the directory structure as shown in the documentation, add your Spring Boot and React applications to their respective folders, and run `docker-compose up -d`!