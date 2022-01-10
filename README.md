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
* `PATH`: When this application is served on a sub-path (not directly on the given domain, by e.g. a reverse proxy), you need to set the path here. Defaults to ``.
* `IS_SECURE`: Whether this application is available as HTTPS / behind an HTTPS reverse proxy (which it should be). Default to `true`.
* `ENABLE_DEBUG_LOGS`: Whether the application (server-side) should output a bit more information, and also print the buzz log to stdout. Defaults to `false`.
* `COLOR_FORM_BUTTON`: The color of the buttons before joining a lobby (on the index page). Defaults to `#161D99`.
* `COLOR_BUZZER_BUTTON_READY`: The color of the buzzer button. Defaults to `limegreen`.
* `COLOR_BUZZER_BUTTON_BUZZED`: The color of the buzzer button when already buzzed. Defaults to `cornflowerblue`.
* `DEFAULT_LOBBY_CODE_LENGTH`: The length of the generated (`[0-9a-f]`, lower-cased) lobby codes. Defaults to `6`.

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
```

## Acknowledgments

This project is inspired by [bufferapp/buzzer](https://github.com/bufferapp/buzzer).

# LICENSE

```
Copyright 2021-2022 Florian Mötz

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
