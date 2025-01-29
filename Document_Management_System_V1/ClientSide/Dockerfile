# Stage 1: Build
FROM node:18.20.0 AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install --legacy-peer-deps

# Install Angular CLI
RUN npm install -g @angular/cli@latest

# Copy the rest of the application
COPY . .

# Build with production configuration
RUN ng build --configuration=production --verbose

# Stage 2: Serve with Nginx
FROM nginx:latest

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Fixed: Updated path to match angular.json outputPath
COPY --from=build /app/dist/client-side-master /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]