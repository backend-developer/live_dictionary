var aws = require('aws-sdk');
var ses = new aws.SES({
   region: 'eu-west-1'
});

exports.handler = function(event, context) {
    console.log("Running daily CI verification check: ", event);
    
    var eParams = {
        Destination: {
            ToAddresses: [<notifee email>]
        },
        Message: {
            Body: {
                Text: {
                    Data: "Local CI environment has not checked-in today, most likely due to not finishing the daily build successfully"
                }
            },
            Subject: {
                Data: "Local CI failed"
            }
        },
        Source: "<auxilary email>"
    };

    console.log('===SENDING EMAIL===');
    var email = ses.sendEmail(eParams, function(err, data){
        if(err) console.log(err);
        else {
            console.log("===EMAIL SENT===");
            console.log(data);


            console.log("EMAIL CODE END");
            console.log('EMAIL: ', email);
            context.succeed(event);
        }
    });
};
