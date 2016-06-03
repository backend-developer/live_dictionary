#!/bin/bash

timestamp=$(date +%Y%m%d%H%M%S)
databaseFileName="AppDatabase${timestamp}.db"
adb shell "cp /data/data/uk.ignas.livedictionary/databases/LiveDictionary.db /sdcard/$databaseFileName"
adb pull /sdcard/$databaseFileName ~/Downloads/$databaseFileName

recentlyAddWordExists=$(sqlite3 LiveDictionary.db "SELECT COUNT(*) FROM translations WHERE nativeWord = 'a ribbon';")
[[ $recentlyAddWordExists -eq 0 ]] && echo "${BASH_SOURCE[0]}: broken or ald database file" && exit 1

actualNumberOfWords=$(sqlite3 LiveDictionary.db "SELECT COUNT(*) FROM translations")

. ./build.properties;
[[ -z $expectedNumberOfWords ]] && echo "${BASH_COURSE[0]}: expectedNumberOfWords variable not found" && eixt 1
[[ $actualNumberOfWords -lt $expectedNumberOfWords ]] && echo "${BASH_SOURCE[0]}: old database file might be old" && exit 1
[[ $actualNumberOfWords -gt $expectedNumberOfWords ]] && echo "${BASH_SOURCE[0]}: unexpected number of records, verify and update expectedNumberOfWords" && exit 1

echo "database backup is located in ~/Downloads/$databaseFileName"

./gradlew installDebug 
