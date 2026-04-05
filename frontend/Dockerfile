# Stage 1: Build
FROM --platform=linux/amd64 node:20 AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
ARG EXPO_PUBLIC_API_URL
ENV EXPO_PUBLIC_API_URL=$EXPO_PUBLIC_API_URL
RUN npx expo export -p web

# Stage 2: Serve
FROM --platform=linux/amd64 nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
