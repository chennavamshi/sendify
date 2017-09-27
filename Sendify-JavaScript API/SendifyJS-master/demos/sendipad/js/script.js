var Sendifysketch = {
    // pen drawing states for canvas
    pen_down: false,
    old_pos: null,
    color: '000',
    line_width: 4,

    // state for XMPP
    connection: null,
    service: null,
    node: null,
	
	//Online Resources connections
    NS_DATA_FORMS: "jabber:x:data",
    NS_PUBSUB: "http://jabber.org/protocol/pubsub",
    NS_PUBSUB_OWNER: "http://jabber.org/protocol/pubsub#owner",
    NS_PUBSUB_ERRORS: "http://jabber.org/protocol/pubsub#errors",
    NS_PUBSUB_NODE_CONFIG: "http://jabber.org/protocol/pubsub#node_config",

    // pubsub event handler
    on_event: function (msg) {
        if ($(msg).find('x').length > 0) {
            var color = $(msg).find('field[var="color"] value').text();
            var line_width = $(msg).find('field[var="line_width"] value').text();
            var from_pos = $(msg).find('field[var="from_pos"] value').text()
                .split(',');
            var to_pos = $(msg).find('field[var="to_pos"] value').text()
                .split(',');
            
            var action = {
                color: color,
                line_width: line_width,
                from: {x: parseFloat(from_pos[0]),
                       y: parseFloat(from_pos[1])},
                to: {x: parseFloat(to_pos[0]),
                     y: parseFloat(to_pos[1])}
            };
            
            Sendifysketch.draw_linesegment(action);
        } else if ($(msg).find('delete[node="' + Sendifysketch.node + '"]')
                   .length > 0) {
            Sendifysketch.show_error("Sendifysketch ended by presenter.");
        }

        return true;
    },
	
	//retrieving the old items if subscribed late.
	
    on_old_items: function (iq) {
        $(iq).find('item').each(function () {
            Sendifysketch.on_event(this);
        });
    },

    // call back if subscription is successfull
    subscribed: function (iq) {
        $(document).trigger("reception_started");
    },

	//call back for subscription if there is any error.
    subscribe_error: function (iq) {
        Sendifysketch.show_error("Subscription failed with " + 
                              Sendifysketch.make_error_from_iq(iq));
    },

    // helper function for error handling
    make_error_from_iq: function (iq) {
        var error = $(iq)
            .find('*[xmlns="' + Strophe.NS.STANZAS + '"]')
            .get(0).tagName;
        var pubsub_error = $(iq)
            .find('*[xmlns="' + Sendifysketch.NS_PUBSUB_ERRORS + '"]');
        if (pubsub_error.length > 0) {
            error = error + "/" + pubsub_error.get(0).tagName;
        }

        return error;
    },
	
	//displaying the error to the user if there is any error in connection or subscribing.
    show_error: function (msg) {
        Sendifysketch.connection.disconnect();
        Sendifysketch.connection = null;
        Sendifysketch.service = null;
        Sendifysketch.node = null;

        $('#error_dialog p').text(msg);
        $('#error_dialog').dialog('open');
    },


//if user is successfully connected
connected :  function () {
    $('#status').html("Connected.");
    
    Sendifysketch.connection.send($pres().c("priority").t("-1"));

    if (Sendifysketch.node.length > 0) {
        // a node was specified, so we attempt to subscribe to it

        // first, set up a callback for the events
        Sendifysketch.connection.addHandler(
            Sendifysketch.on_event,
            null, "message", null, null, Sendifysketch.service);

        // now subscribe
        console.log("sss");
        Sendifysketch.connection.subscribeNode(Sendifysketch.node,Sendifysketch.subscribed,Sendifysketch.subscribe_error);
    } else {
        // Node was not specified,so we create a new node
        console.log("Created");
        Sendifysketch.connection.createNode("",
                                     Sendifysketch.configured,
                                     Sendifysketch.configure_error);
    }
},


	//node configured call back function
    configured: function (iq) {
	    var node = $(iq).find("create").attr('node');
        Sendifysketch.node = node;
        console.log("came here");
        $(document).trigger("broadcast_started");
    },

	//node configuration error call back function.
    configure_error: function (iq) {
        Sendifysketch.show_error("Sendifysketch configuration failed with " +
                              Sendifysketch.make_error_from_iq(iq));
    },

	//Pulbisher sending notification.
    publish_action: function (action) {
        Sendifysketch.connection.sendIQ(
            $iq({to: Sendifysketch.service, type: "set"})
                .c('pubsub', {xmlns: Sendifysketch.NS_PUBSUB})
                .c('publish', {node: Sendifysketch.node})
                .c('item')
                .c('x', {xmlns: Sendifysketch.NS_DATA_FORMS,
                         type: "result"})
                .c('field', {"var": "color"})
                .c('value').t(action.color)
                .up().up()
                .c('field', {"var": "line_width"})
                .c('value').t('' + action.line_width)
                .up().up()
                .c('field', {"var": "from_pos"})
                .c('value').t('' + action.from.x + ',' + action.from.y)
                .up().up()
                .c('field', {"var": "to_pos"})
                .c('value').t('' + action.to.x + ',' + action.to.y));
    },

	//action to draw line segment on the canvas
    draw_linesegment: function (action) {
        // render the line segment
        var ctx = $('#sketch').get(0).getContext('2d');
        ctx.strokeStyle = '#' + action.color;
        ctx.lineWidth = action.line_width;
        ctx.beginPath();
        ctx.moveTo(action.from.x, action.from.y);
        ctx.lineTo(action.to.x, action.to.y);
        ctx.stroke();
    },
	
	//disconnect the user.
    disconnect: function () {
        $('#erase').click();
        Sendifysketch.connection.disconnect();
        Sendifysketch.connection = null;
        Sendifysketch.service = null;
        Sendifysketch.node = null;
        $('#login_dialog').dialog('open');
    }
};

$(document).ready(function () {

	//open the dialog box in the start
    $('#login_dialog').dialog({
        autoOpen: true,
        draggable: false,
        modal: true,
        title: 'Connect to a Sendifysketch',
        buttons: {
            "Connect": function () {
                $(document).trigger('connect', {
                    jid: $('#jid').val().toLowerCase(),
                    password: $('#password').val(),
                    service: $('#service').val().toLowerCase(),
                    node: $('#node').val()
                });
                
                $('#password').val('');
                $(this).dialog('close');
            }
        }
    });

    $('#error_dialog').dialog({
        autoOpen: false,
        draggable: false,
        modal: true,
        title: 'Whoops!  Something Bad Happened!',
        buttons: {
            "Ok": function () {
                $(this).dialog('close');
                $('#login_dialog').dialog('open');
            }
        }
    });

	//function if mouse is clicked
    $('#sketch').mousedown(function () {
        Sendifysketch.pen_down = true;
    });

	//function if mouse is relased
    $('#sketch').mouseup(function () {
        Sendifysketch.pen_down = false;
    });

	//function if mouse is moved
    $('#sketch').mousemove(function (ev) {
        // get the position of the drawing area, our offset of cavas
        var offset = $(this).offset();
        // calculate our position within the drawing area
        var pos = {x: ev.pageX - offset.left, 
                   y: ev.pageY - offset.top};

        if (Sendifysketch.pen_down) {
            if (!Sendifysketch.old_pos) {
                Sendifysketch.old_pos = pos;
                return;
            }

            if (!$('#sketch').hasClass('disabled') &&
                (Math.abs(pos.x - Sendifysketch.old_pos.x) > 2 ||
                 Math.abs(pos.y - Sendifysketch.old_pos.y) > 2)) {
                Sendifysketch.draw_linesegment({
                    color: Sendifysketch.color,
                    line_width: Sendifysketch.line_width,
                    from: {x: Sendifysketch.old_pos.x, 
                           y: Sendifysketch.old_pos.y},
                    to: {x: pos.x,
                         y: pos.y}});
                
                Sendifysketch.publish_action({
                    color: Sendifysketch.color,
                    line_width: Sendifysketch.line_width,
                    from: Sendifysketch.old_pos,
                    to: pos
                });

                Sendifysketch.old_pos = pos;
            }
        } else {
            Sendifysketch.old_pos = null;
        }
    });

	//change the color of the pen
    $('.color').click(function (ev) {
        Sendifysketch.color = $(this).attr('id').split('-')[1];
    });

	//change the width of the pen
    $('.linew').click(function (ev) {
        Sendifysketch.line_width = $(this).attr('id').split('-')[1];
    });

	//erase the canvas
    $('#erase').click(function () {
        var ctx = $('#sketch').get(0).getContext('2d');
        ctx.fillStyle = '#000';
        ctx.strokeStyle = '#fff';
        ctx.fillRect(0, 0, 600, 500);
    });
});

//connect to the server
$(document).bind('connect', function (ev, data) {
    $('#status').html('Connecting...');
    
    var conn =  Sendify;
    conn.jid = data.jid;
    conn.pass = data.password;
    if(conn.connect('http://162.242.243.182:5280/http-bind'))
    {
        conn.login(Sendifysketch.connected);
    } 
    else
    {
        console.log("something is wrong: ");
    }
    
    
    Sendifysketch.connection = conn;
    Sendifysketch.service = data.service;
    Sendifysketch.node = data.node;
});

//Publisher broadcast started - send notifications
$(document).bind('broadcast_started', function () {
    $('#status').html('Broadcasting at service: <i>' + 
                      Sendifysketch.service + '</i> node: <i>' +
                      Sendifysketch.node + "</i>");

    $('.button').removeClass('disabled');
    $('#sketch').removeClass('disabled');
    $('#erase').removeAttr('disabled');
    $('#disconnect').removeAttr('disabled');
     //to disconnect
    $('#disconnect').click(function () {
        $('.button').addClass('disabled');
        $('#sketch').addClass('disabled');
        $('#erase').attr('disabled', 'disabled');
        $('#disconnect').attr('disabled', 'disabled');

        Sendifysketch.connection.connection.sendIQ(
            $iq({to: Sendifysketch.service,
                 type: "set"})
                .c('pubsub', {xmlns: Sendifysketch.NS_PUBSUB_OWNER})
                .c('delete', {node: Sendifysketch.node}));

        Sendifysketch.disconnect();
    });
});

//For subscribet to start receving notifications
$(document).bind('reception_started', function () {
    $('#status').html('Receiving Sendifysketch.');

    $('#disconnect').removeAttr('disabled');
    $('#disconnect').click(function () {
        $('#disconnect').attr('disabled', 'disabled');
        Sendifysketch.connection.unsubscribeNode(Sendifysketch.node);
        Sendifysketch.disconnect();
    });

    // get the missed notifications
    Sendifysketch.connection.getPreviousPublications(Sendifysketch.node,
        Sendifysketch.on_old_items);
});
