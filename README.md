# Authentication + Authorization backend

The backend application is baked mainly with Http4s, cats and circe.

## How to start

Run the docker compose file under the `docker` folder.

`$ docker-compose -f docker/docker-compose-local.yml up`

## How to run

Run the database migration: `$ sbt "project market-db" "run update"`

Run the backend application: `$ sbt "project market-api run`

### Calling the API

Login endpoint:

```
curl -v -X POST http://localhost:8081/api/login \                                                                                               SIGINT(2) ↵  10011  00:30:00
-H 'Content-Type: text/json' \
-d @- << EOF
{
 "email": "email@email.com",
 "password": "password"
}
EOF
```

## How to test

Run the unit test: `sbt "project market-api" test`

Run the integration test: `sbt "project market-api" it:test`