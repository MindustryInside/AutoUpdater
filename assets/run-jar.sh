#!/usr/bin/env bash
if [[ $# -eq 0 ]] ; then
    echo 'A server jar must be supplied as the first argument.'
    exit 1
fi

if [[ ! -e $1 ]] ; then
    echo "The supplied jar file '$1' must exist."
    exit 1
fi

while true; do
#auto-restart until ctrl-c or exit 0
java -jar -XX:+HeapDumpOnOutOfMemoryError $1
excode=$?
if [ $excode -eq 0 ] || [ $excode -eq 130 ]; then
    exit 0
fi
done
