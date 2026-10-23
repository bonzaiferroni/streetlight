#!/bin/bash
# Speaks each 10% milestone of animated packs until all are done.
cd /home/starfox/projects/streetlight
total=$(grep -o 'svgPackOf("' koala/src/commonMain/kotlin/koala/SvgFile.kt | wc -l)
labels=$(grep -o 'svgPackOf("[^"]*")' koala/src/commonMain/kotlin/koala/SvgFile.kt | sed 's/svgPackOf("//;s/")//')
last=$(( $(for l in $labels; do [ -f www/svg/$l-animated.svg ] && echo; done | wc -l) * 10 / total ))
while true; do
    n=$(for l in $labels; do [ -f www/svg/$l-animated.svg ] && echo; done | wc -l)
    tenth=$(( n * 10 / total ))
    if [ $tenth -gt $last ]; then
        last=$tenth
        echo "Ahoy navigator, the animated icons are at $(( tenth * 10 )) percent. $n of $total." > /tmp/kokoro-tts.fifo
    fi
    [ $n -ge $total ] && exit 0
    sleep 30
done
