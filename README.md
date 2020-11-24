# Authentication + Authorization backend

The backend application is baked mainly with Http4s, cats and circe.

## How to start

Run the docker compose file under the `docker` folder.

`$ docker-compose -f docker/docker-compose-local.yml up`

## How to run

Run the database migration: `$ sbt "project baked-db" "run update"`

Run the backend application: `$ sbt "project baked-api" run`

### Calling the API

#### Login endpoints

Normal login:

```
curl -v -X POST http://localhost:8081/api/login \
-H 'Content-Type: text/json' \
-d @- << EOF
{
 "email": "email@email.com",
 "password": "password"
}
EOF
```

Google login:

```
curl -v -X POST http://localhost:8081/api/login/google \
-H 'Content-Type: text/json' \
-d @- << EOF
{
 "token": "GOOGLE_TOKEN"
}
EOF
```

When running on `local env`, you can set any value on token, it will represent by name and email.

```
curl -v -X POST http://localhost:8081/api/login/google \
-H 'Content-Type: text/json' \
-d @- << EOF
{
 "token": "myemail@email.com"
}
EOF
```

## How to test

Run the unit test: `sbt "project baked-api" test`

Run the integration test: `sbt "project baked-api" it:test`