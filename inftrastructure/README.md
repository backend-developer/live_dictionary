# Instructions for a continuous integration envionment to ensure pipeline is always working: 

# 1. Install Ubuntu Linux

# 2. Schedule machine to turn on daily
# create/modify file: /etc/rc.local with contents:

#!/bin/sh -e
sh -c "echo 0 > /sys/class/rtc/rtc0/wakealarm"
sh -c "echo `date '+%s' -d '6:00 next day'` > /sys/class/rtc/rtc0/wakealarm"
exit 0

# 3. Add permissions of excution for the owner for this file
sudo chmod 744 /etc/rc.local
