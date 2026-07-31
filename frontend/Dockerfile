FROM node:20.11.0-alpine AS build
WORKDIR /app
COPY package.json ./
RUN npm install
COPY tsconfig.json vite.config.ts vitest.config.ts tailwind.config.js postcss.config.js index.html ./
COPY src ./src
COPY tests ./tests
RUN npm run build

FROM nginxinc/nginx-unprivileged:1.27.3-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 8080
USER nginx
HEALTHCHECK --interval=10s --timeout=3s --retries=5 CMD wget -qO- http://127.0.0.1:8080/ || exit 1
