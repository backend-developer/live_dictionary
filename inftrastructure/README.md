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

# 4. configure AWS SES for notifying about broken local CI.
# configure and verify your email within SES for getting a message about failing local CI
# configure and verify your auxilary email within SES to indicate which email it was send from

# 5. create policy within AWS to allow to allow send email via SES

{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "logs:CreateLogGroup",
                "logs:CreateLogStream",
                "logs:PutLogEvents",
                "ses:SendEmail",
                "ses:SendRawEmail"
            ],
            "Resource": "*"
        }
    ]
}

# 6. create role within AWS with the policy attached

# 7. create lamda to be executed daily with the code defined in a lamda-ci-check.js
# daily can be configured as CloadWatch events rule "cron(0 12 * * * *)"
