#!/usr/bin/env sh
while getopts s:o: flag
do
    case "${flag}" in
        s) server=${OPTARG};;
        o) only=${OPTARG};;
    esac
done

if [[ $only == backend ]]; then
    lein uberjar
else
    echo $only
    npm run postcss:release
    npm run shadow:release
    cp -rf public/* resources/public
    rm -rf resources/public/js/cljs-runtime
    lein uberjar
fi

scp target/*-standalone.jar $server:/opt/prod/liberty-hu.jar
ssh $server "systemctl restart liberty-hu.service"

echo "done!"
