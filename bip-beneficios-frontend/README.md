# BIP Beneficios Frontend

App Angular (`bip-beneficios-frontend`) que consome a API `bip-beneficios-backend`.

## Scripts

```powershell
npm install
npm start
npm run build
npm test
```

O `npm start` usa `proxy.conf.json` para encaminhar chamadas `/api` para `http://localhost:8080`.

## Organizacao

```text
src/app/
  app.component.*
  modules/
    beneficios/
      components/
        beneficio-page/
      models/
      services/
```
