# Buzzer Service

**Host an online Buzzing System for online party games.**

An online buzzing system for your browser implemented in KTor / Kotlin using websockets.

This service provides a buzzing system with different lobbies with unlimited participants to join and a host to have an 
overview on who buzzed first, and to reset the buzzers remotely.

This is the source-code of [buzzer.moetz.dev](https://buzzer.moetz.dev).

## Setup

The docker image can be found here: [hub.docker.com/r/flowmo7/kotlin-buzzer](https://hub.docker.com/r/flowmo7/kotlin-buzzer).

Possible environment variables:

* `DOMAIN`: The domain this application is available at, e.g. `buzzer.example.org`
* `IS_SECURE`: Whether this application is available as HTTPS / behind an HTTPS reverse proxy (which it should be). Default to `true`.
* `ENABLE_DEBUG_LOGS`: Whether the application (server-side) should output a bit more information, and also print the buzz log to stdout. Defaults to `false`.

### Data persistence

The server stores no state (or better: lobby-state is only stored in-memory), the only persistence that can be mapped out of the container is a log-file that contains all (buzzing / lobby related) events in a textfile:
* `/etc/log/kotlin-buzzer/log.txt`

### Example docker-compose.yml

```yaml
services:
  buzzer:
    image: "flowmo7/kotlin-buzzer:master"
    restart: unless-stopped
    ports:
      - 8080:8080 #Should be behind an SSL reverse proxy
    environment:
      - DOMAIN=buzzer.example.org
    volumes:
      - /srv/docker/buzzer/logs:/etc/log/kotlin-buzzer:rw
```

## Acknowledgments

This project is inspired by [bufferapp/buzzer](https://github.com/bufferapp/buzzer).

# LICENSE

```
Copyright 2021 Florian Mötz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
