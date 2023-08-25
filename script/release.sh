#!/usr/bin/env sh

npm run postcss:release
npm run shadow:release
cp -rf public/* resources/public
rm -rf resources/public/js/cljs-runtime

lein uberjar
