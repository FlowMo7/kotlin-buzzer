## Directory for screenshots for this repository

Puppeteer is used to create automated screenshots.

Usage:

```shell
cd <this ./screenshots directory>
rm *.png
docker run -i --init --rm -v ${PWD}:/screenshots buildkite/puppeteer:latest node -e "`cat screenshots.js`"
sudo chown $(whoami) *.png
```
