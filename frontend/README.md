# Frontend

React + Vite.

```bash
make run          # BD + API (:8080) + frontend (:5173)
make front-test
make front-build  # genera frontend/dist
```

Para apuntar a otra API en desarrollo: `API_TARGET=http://host:puerto npm run dev`.
En producción se configura con `VITE_API_URL`.
