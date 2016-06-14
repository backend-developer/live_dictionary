#!/bin/bash

function verifyCurrentBackup {
	local recentlyAddWordExists=$(sqlite3 $1 "SELECT COUNT(*) FROM translations WHERE nativeWord = 'a ribbon';")
	[[ $recentlyAddWordExists -eq 0 ]] && echo "${BASH_SOURCE[0]}: broken or old database file. Exiting" && exit 1
	
	local actualNumberOfWords=$(sqlite3 $1 "SELECT COUNT(*) FROM translations")
	
	. ./build.properties;
	[[ -z $expectedNumberOfWords ]] && echo "${BASH_COURSE[0]}: expectedNumberOfWords variable not found" && eixt 1
	[[ $actualNumberOfWords -lt $expectedNumberOfWords ]] && echo "${BASH_SOURCE[0]}: database file might be old. expected number of words: $expectedNumberOfWords , found: $actualNumberOfWords" && exit 1
	[[ $actualNumberOfWords -gt $expectedNumberOfWords ]] && echo "${BASH_SOURCE[0]}: unexpected number of records, verify and update expectedNumberOfWords" && exit 1
	
	echo "${BASH_SOURCE[0]}: database backup is located in $1"
}

function tryExtractDatabaseFromAndroid {
	local internalNameForDbBackup=$(basename $1)
	[[ $(adb devices | awk '$2=="device"{print $0}' | wc -l) -ne 1 ]] && echo "device not connected or connected multiple devices. Exiting" && exit 1
	if [[ $(adb shell "pm list packages -f" | grep "uk.ignas.livedictionary" | wc -l) -eq 1 ]]
	then 
		adb shell "run-as uk.ignas.livedictionary chmod 666 databases/LiveDictionary.db"
		adb shell "cp /data/data/uk.ignas.livedictionary/databases/LiveDictionary.db /sdcard/$internalNameForDbBackup"
		adb pull /sdcard/$internalNameForDbBackup $1
		[[ "$?" -ne 0 ]] && echo "${BASH_SOURCE[0]}: failed to pull database. Exiting" && exit 1
		echo "${BASH_SOURCE[0]}: copied database from $1"
		echo "${BASH_SOURCE[0]}: verifying database"
		verifyCurrentBackup $1
	else
		echo "${BASH_SOURCE[0]}: skipping backup. Application not installed"
	fi
}

function confirm {
	echo "${1}. [y/N]:"
	read hasUserConfirmed
	[[ $hasUserConfirmed =~ ^[Yy]$ ]]
}

function verifyPreviousBackups {
	local backupDirectory=$1
	
	[[ ! -d $backupDirectory ]] && echo "${BASH_SOURCE[0]}: temporary directory not found. Create directory: ${backupDirectory}. Exiting" && exit 1
	
	local previousDbBackupPath=$(ls -lad $backupDirectory/* | awk '{print $9}' | sort | tail -n1)
	[[ -z $previousDbBackupPath ]] && echo "${BASH_SOURCE[0]}:no files found in backupDirectory. Exiting" && exit 1
	local previousDbBackupTimestamp=$(basename $previousDbBackupPath | sed 's/.*[^0-9]\(14[0-9]\{8\}\)[^0-9].*/\1/')
	
	[[ ! $previousDbBackupTimestamp =~ ^14[0-9]{8}$ ]] && echo "${BASH_SOURCE[0]}:backup file name does not contains timeestamp. Exiting" && exit 1
	[[ "$previousDbBackupTimestamp" -ge "$(date +%s)" ]] && echo "${BASH_SOURCE[0]}:backup file name timestamp is greater than current timestamp. Exiting" && exit 1
	
	confirm "${BASH_SOURCE[0]}: previous database if from time: $(date -d @$previousDbBackupTimestamp +%F\ %T). do you want to proceed?" 
	[[ $? -ne 0 ]] && exit 1
}

function pushLatestBackupToAndroid {
	local internalNameForDbBackup=$(ls -la $1 | awk '{print $9}' | sort | tail -n1) 
	adb push $1/$internalNameForDbBackup /sdcard/$internalNameForDbBackup
	adb shell "run-as uk.ignas.livedictionary chmod 666 databases/LiveDictionary.db"
	adb shell "cp /sdcard/$internalNameForDbBackup /data/data/uk.ignas.livedictionary/databases/LiveDictionary.db"
	echo "${BASH_SOURCE[0]}: last backup of db pushed"
}

function initializeAppFiles {
	adb shell am start -n uk.ignas.livedictionary/uk.ignas.livedictionary.LiveDictionaryActivity
	sleep 1
	adb shell am force-stop uk.ignas.livedictionary
}

dbBackup=~/Documents/liveDictionaryBackup/AppDatabase$(date +%s).db
verifyPreviousBackups $(dirname $dbBackup) 
tryExtractDatabaseFromAndroid $dbBackup
./gradlew installDebug 
initializeAppFiles
pushLatestBackupToAndroid $(dirname $dbBackup)
adb shell am start -n uk.ignas.livedictionary/uk.ignas.livedictionary.LiveDictionaryActivity
