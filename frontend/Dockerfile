FROM node:20.11.0-alpine AS build
WORKDIR /app
COPY package.json ./
COPY tsconfig.json vite.config.ts tailwind.config.js postcss.config.js index.html ./
RUN npm install
COPY src ./src
RUN npm run build

FROM nginx:1.27.2-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
