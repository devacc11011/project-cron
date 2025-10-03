# Build stage
FROM node:20-alpine AS build
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Build application with Turbopack
RUN npm run build

# Runtime stage
FROM node:20-alpine
WORKDIR /app

# Copy package files and install production dependencies only
COPY package*.json ./
RUN npm ci --omit=dev

# Copy built application from build stage
COPY --from=build /app/.next ./.next
COPY --from=build /app/public ./public

# Expose port
EXPOSE 3000

# Set production environment
ENV NODE_ENV=production

# Start application
CMD ["npm", "start"]
