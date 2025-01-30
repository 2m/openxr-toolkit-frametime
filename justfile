dev:
  process-compose -p 8088 up

dev-js:
  cd modules/frontend; npm run dev

dev-scala-js:
  sbt --client ~frontend/fastLinkJS

build-scala-js:
  sbt --client publicProd

build-js:
  cd modules/frontend; npm run build

install:
  cd modules/frontend; npm install

serve:
  cd modules/frontend/dist; caddy file-server --listen :8001
