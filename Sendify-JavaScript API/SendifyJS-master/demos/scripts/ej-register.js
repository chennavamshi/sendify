var BOSH_SERVICE = 'http://162.242.243.182:5280/http-bind'
connection = new Strophe.Connection(BOSH_SERVICE);

$(document).ready(function () {

    var callback = function (status) {
        if (status === Strophe.Status.REGISTER) {
            connection.register.submit();
		console.log("inside regisger");
        } else if (status === Strophe.Status.REGISTERED) {
            console.log("registered!");
            connection.authenticate();
        } else if (status === Strophe.Status.CONNECTED) {
            console.log("logged in!");
        } else {
            console.log("something wrong !");
        }
    };

    $('#ej-register').bind('click', function () {
        user = $('#ej-username').val();
        pass = $('#ej-password').val();
        var domain = $('#ej-domain').val(); 

        console.log("User: "+user+"Pass: "+pass+"Domain: "+domain);
        if(!user || !pass || !domain) {
            console.log('please input some values');
            return;
        }
        connection.register.connect($('#ej-domain').val(), callback);
    });
    $('#ej-connect').bind('click', function () {
        
            jid = $('#ej-username').val()+'@'+$('#ej-domain').val();
            console.log(jid);
            connection.connect(jid,$('#ej-password').val(),onConnect);
    
      
    });

});




function onConnect(status)
{
    if (status == Strophe.Status.CONNECTING) {
        console.log('Strophe is connecting.');
    } else if (status == Strophe.Status.CONNFAIL) {
        console.log('Strophe failed to connect.');
        $('#connect').get(0).value = 'connect';
    } else if (status == Strophe.Status.DISCONNECTING) {
        console.log('Strophe is disconnecting.');
    } else if (status == Strophe.Status.DISCONNECTED) {
        console.log('Strophe is disconnected.');
        $('#connect').get(0).value = 'connect';
    } else if (status == Strophe.Status.CONNECTED) {
        console.log('Strophe is connected.');
        connection.disconnect();
    }
}

function log(msg)
{
    $('#log').append('<div></div>').append(document.createTextNode(msg));
}

function rawInput(data)
{
    log('RECV: ' + data);
}

function rawOutput(data)
{
    log('SENT: ' + data);
}

