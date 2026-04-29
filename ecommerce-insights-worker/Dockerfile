# Stage 1: Install dependencies
FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm install --production

# Stage 2: Run the application
FROM node:20-alpine

WORKDIR /app

COPY --from=build /app/node_modules ./node_modules
COPY . .

# No port to expose as it's a worker

ENTRYPOINT ["npm", "start"]
