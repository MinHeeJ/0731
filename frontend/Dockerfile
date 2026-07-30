FROM node:20.11.0-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY index.html ./
COPY tsconfig.json vite.config.ts vitest.config.ts tailwind.config.js postcss.config.js ./
COPY src ./src
COPY public ./public
RUN npm run build

FROM nginx:1.27.3-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
RUN sed -i 's|pid        /var/run/nginx.pid;|pid        /tmp/nginx.pid;|' /etc/nginx/nginx.conf \
    && chown -R nginx:nginx /var/cache/nginx /usr/share/nginx/html /etc/nginx/conf.d
USER nginx
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --retries=3 CMD wget -qO- http://127.0.0.1:8080/ || exit 1
