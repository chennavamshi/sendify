/*
 * Authors
 * Vamshi K Chenna
 * Yash N Shah
 * Dhirubhai Ambhani Institute of Information and Communication Technology.
 *
 *  File: sendify.js
 *
 *  A JavaScript library for XMPP BOSH.
 *
 *  This is the JavaScript version of the Sendify library.  Since JavaScript
 *  has no facilities for persistent TCP connections, this library uses
 *  Bidirectional-streams Over Synchronous HTTP (BOSH) to emulate
 *  a persistent, stateful, two-way connection to an XMPP server.  More
 *  information on BOSH can be found in XEP 124.
 
 */


/** Class: Sendify
 *  An object container for all Sendify library functions.
 *
 *  This class is just a container for all the objects and constants
 *  used in the library.  
 */

var Sendify = {  
jid : null,
pass : null,
node : null,
service : null,
myroster: null,
connection : null,

    /** Constants: XMPP Namespace Constants
     *  Common namespace constants from the XMPP RFCs and XEPs.
     *
     *  NS.HTTPBIND - HTTP BIND namespace from XEP 124.
     *  NS.BOSH - BOSH namespace from XEP 206.
     *  NS.CLIENT - Main XMPP client namespace.
     *  NS.AUTH - Legacy authentication namespace.
     *  NS.ROSTER - Roster operations namespace.
     *  NS.PROFILE - Profile namespace.
     *  NS.DISCO_INFO - Service discovery info namespace from XEP 30.
     *  NS.DISCO_ITEMS - Service discovery items namespace from XEP 30.
     *  NS.MUC - Multi-User Chat namespace from XEP 45.
     *  NS.SASL - XMPP SASL namespace from RFC 3920.
     *  NS.STREAM - XMPP Streams namespace from RFC 3920.
     *  NS.BIND - XMPP Binding namespace from RFC 3920.
     *  NS.SESSION - XMPP Session namespace from RFC 3920.
     */


NS: {
        HTTPBIND: "http://jabber.org/protocol/httpbind",
        BOSH: "urn:xmpp:xbosh",
        CLIENT: "jabber:client",
        AUTH: "jabber:iq:auth",
        ROSTER: "jabber:iq:roster",
        PROFILE: "jabber:iq:profile",
        DISCO_INFO: "http://jabber.org/protocol/disco#info",
        DISCO_ITEMS: "http://jabber.org/protocol/disco#items",
        MUC: "http://jabber.org/protocol/muc",
        SASL: "urn:ietf:params:xml:ns:xmpp-sasl",
        STREAM: "http://etherx.jabber.org/streams",
        BIND: "urn:ietf:params:xml:ns:xmpp-bind",
        SESSION: "urn:ietf:params:xml:ns:xmpp-session",
        VERSION: "jabber:iq:version",
        STANZAS: "urn:ietf:params:xml:ns:xmpp-stanzas"
  },


NS_DATA_FORMS: "jabber:x:data",
NS_PUBSUB: "http://jabber.org/protocol/pubsub",
NS_PUBSUB_OWNER: "http://jabber.org/protocol/pubsub#owner",
NS_PUBSUB_ERRORS: "http://jabber.org/protocol/pubsub#errors",
NS_PUBSUB_NODE_CONFIG: "http://jabber.org/protocol/pubsub#node_config",


/** Constants: Connection Status Constants
     *  Connection status constants for use by the connection handler
     *  callback.
     *
     *  Status.ERROR - An error has occurred
     *  Status.CONNECTING - The connection is currently being made
     *  Status.CONNFAIL - The connection attempt failed
     *  Status.AUTHENTICATING - The connection is authenticating
     *  Status.AUTHFAIL - The authentication attempt failed
     *  Status.CONNECTED - The connection has succeeded
     *  Status.DISCONNECTED - The connection has been terminated
     *  Status.DISCONNECTING - The connection is currently being terminated
     *  Status.ATTACHED - The connection has been attached
*/

 Status: {
        ERROR: 0,
        CONNECTING: 1,
        CONNFAIL: 2,
        AUTHENTICATING: 3,
        AUTHFAIL: 4,
        CONNECTED: 5,
        DISCONNECTED: 6,
        DISCONNECTING: 7,
        ATTACHED: 8
  },

/** Function: connect
 *  Connect to the XMPP-BOSH server.
 *  
 *  Parameters:
 *    (String) BOSH_URL - The url for the server.
 *
 *  Returns:
 *    true if the connection is successfull.
 *    false if the connection was unsuccessfull.
 */

connect : function (BOSH_URL) {
    var conn = new Strophe.Connection(BOSH_URL);
    if(conn!=null)
    {
    this.connection = conn;
     return true;
    }
    else
     return false;
},

/** Function: login
 *  login using the credentials.
 *  
 *  Parameters:
 *    None
 *
 *  Returns:
 *    true if the login is successfull.
 *    false if the login was unsuccessfull.
 */

login : function(success){
    console.log(this.jid);
    console.log(this.pass);
    console.log(this.connection);
    this.connection.connect(this.jid, this.pass, function (status) {
        if (status === Sendify.Status.CONNECTED) {
             //console.log("Connected and Logged in! "+x);
            //Sendify.getRoster();
            success();
        } else if (status === Sendify.Status.DISCONNECTED) {
               console.log("Disconnected!");
    }});
 },

/** Function: getRoster
 *  get the roster of the user. A roster is a list of contacts to which the user is subscribed to.
 *  
 *  Parameters:
 *    None
 *
 *  Returns:
 *     (structure) roster {roster.jid,roster.sub,roster.boj,roster.name}:  contatns  the arrays of name, jid, subscribtion status of the contacts to which the user is subscribed to.
 */

getRoster: function(success,err){
  console.log("inside getRoaster");
  	var iq = $iq({type: 'get'}).c('query', {xmlns: 'jabber:iq:roster'});
    this.connection.sendIQ(iq,function(iq){
              i = 0;
              result = {};
              result.jid = [];
              result.name = [];
              result.sub = [];
              result.obj = [];
              $(iq).find('item').each(function () {
                  result.jid[i] = $(this).attr('jid');
                  result.name[i] = $(this).attr('name') || result.jid[i]; //???
              		result.sub[i] = $(this).attr('subscription');

            			result.obj[i] = this;
        	      	i++;
              });
              console.log(result);
              Sendify.myroster = result;
              //Sendify.service = "pubsub.sendify";
              Sendify.connection.send($pres());
              success(result);

              //Sendify.publishNode("asdfsadf asdf ","latest_books");
              //Sendify.sendMessage("try@sendify","asdfasdf","chat");
              //Sendify.subscribeJidPresence("try@sendify","test");
              //Sendify.getPreviousPublications("latest_books",4);
              //Sendify.deleteNode("657511917");
              //Sendify.unsubscribeNode("657511917");
              //Sendify.getSubscribers("latest_books");
              //Sendify.subscribeNode("657511917");
              //Sendify.createNode("");
              //return result;
    },err);
},

/** Function: addHandler
     *  Add a stanza handler for the connection.
     *
     *  This function adds a stanza handler to the connection.  The
     *  handler callback will be called for any stanza that matches
     *  the parameters.  Note that if multiple parameters are supplied,
     *  they must all match for the handler to be invoked.
     *
     *  The handler will receive the stanza that triggered it as its argument.
     *  The handler should return true if it is to be invoked again;
     *  returning false will remove the handler after it returns.
     *
     *  As a convenience, the ns parameters applies to the top level element
     *  and also any of its immediate children.  This is primarily to make
     *  matching /iq/query elements easy.
     *
     *  The options argument contains handler matching flags that affect how
     *  matches are determined. 
     *
     *  Parameters:
     *    (Function) handler - The user callback.
     *    (String) ns - The namespace to match.
     *    (String) name - The stanza name to match.
     *    (String) type - The stanza type attribute to match.
     *    (String) id - The stanza id attribute to match.
     *    (String) from - The stanza from attribute to match.
     *    (String) options - The handler options
     *
     *  Returns:
     *    [None]
     */
    addHandler: function (handler, ns, name, type, id, from, options)
    {
        Sendify.connection.addHandler(handler, ns, name, type, id, from, options);
    },



  /** Function: getBareJidFromJid
     *  Get the bare JID from a JID String.
     *
     *  Parameters:
     *    (String) jid - A JID.
     *
     *  Returns:
     *    A String containing the bare JID.
     */

    getBareJidFromJid: function (jid)
      {
         return jid.split("/")[0];
     },

  
   /** Function: getNodeFromJid
     *  Get the node portion of a JID String.
     *
     *  Parameters:
     *    (String) jid - A JID.
     *
     *  Returns:
     *    A String containing the node.
     */
    getNodeFromJid: function (jid)
    {
        if (jid.indexOf("@") < 0) { return null; }
        return jid.split("@")[0];
    },


 fileHandler : function (from, sid, filename, size, mime) {
   console.log("received");
    // received a stream initiation
  // be prepared
},


/** Function: createAccount
 *  Register a account with the server.
 *  
 *  Parameters:
 *    (String) jid - jid of the user who want to register.
 *    (String) pass - password of the user who want to register.
 *  Returns:
 *    true if the registration is successfull.
 *    false if the registration was unsuccessfull.
 */

createAccount: function(jid,pass){
    if(jid == null && pass== null)
      return;
    console.log("In Create createAccount");
    this.connection.register.connect(jid,pass,function(status){
        if (status === Strophe.Status.REGISTER) {
            console.log("register");
            connection.register.submit();

        } else if (status === Strophe.Status.REGISTERED) {
            console.log("registered!");
            connection.authenticate();
        } else if (status === Strophe.Status.CONNECTED) {
            console.log("logged in!");
        } else {
            console.log("something wrong !");
        }
    });
},

/** Function: send
     *  Send a stanza.
     *
     *  This function is called to push data onto the send queue to
     *  go out over the wire.  Whenever a request is sent to the BOSH
     *  server, all pending data is sent and the queue is flushed.
     *
     *  Parameters:
     *    (XMLElement |
     *     [XMLElement] |
     *     Strophe.Builder) elem - The stanza to send.
     */
    send: function (elem){

    Sendify.connection.send(elem);
    },
    


/** Function: createNode
 *  Create a node to implement pub sub model.
 *  
 *  Parameters:
 *    (String) nodeName [Optional] - name of the node that you want to create.
 *    
 *  Returns:
 *    null if the creation was unsuccessfull.
 *    nodeName if the nodeName is not specified.
 *    nodeName on sucessfull creation if the nodeName is specified.
 */

createNode: function(nodeName,err){
  var createiq = $iq({to: "pubsub.sendify",
                            type: "set"})
            .c('pubsub', {xmlns: Sendify.NS_PUBSUB})
            .c('create', {node : nodeName});
        console.log("inside createNode");
        this.connection.sendIQ(createiq,function(iq){
            var node =  $(iq).find("create").attr('node');
            this.node = node;
            //return node;
            console.log("Node Created name : " + node);
        },function(iq){
           console.log("Node Creation Error " +iq);
        });
},


/** Function: subscribeNode
 *  Subscribe to a particular node to recive notification from that node.
 *  
 *  Parameters:
 *    (String) nodeName - name of the node that you want to subscribe to.
 *    
 *  Returns:
 *    true if the subscription was successful.
 *    false if the subscription was unsuccessful.
 */


subscribeNode: function (nodeName){
      var subiq = $iq({to: "pubsub.sendify",type: "set"})
         .c('pubsub', {xmlns: this.NS_PUBSUB})
         .c('subscribe', {node: nodeName,
                          jid: this.connection.jid});
      console.log("Inside Subscribe Node ");
      this.connection.sendIQ(subiq,function subscribed(iq){
          console.log("Successfully subscribed");
          return true;
      }, function subscribeError(iq){
        var err = $(iq).find("error");
          console.log("Not subscribed"+err);
          console.log(iq);
   
      });
},

/** Function: subscribeJidPresence
 *  Subscribe to a particular Jid for presence.If you subscribe to a JID, you will recieve the presence of when the user is online, or when is offline
 *  or when he changes his presenc                          
 *  
 *  Parameters:
 *    (String) JID - Jid of the contact whom you want to subscribe to.
 *    (String) name - Name of the contact in the contact list.
 *  Returns:
 *    true if the subscription was successful.
 *    false if the subscription was unsuccessful.
 */


subscribeJidPresence: function (JID,name){
      data = {};
      data.jid = JID;
      data.name = name;
      var iq = $iq({type: "set"}).c("query", {xmlns: "jabber:iq:roster"})
          .c("item", data);
      this.connection.sendIQ(iq);
      var subscribe = $pres({to: data.jid, "type": "subscribe"});
      this.connection.send(subscribe);
},


/** Function: publishNode
 *  publish information on a particular node. 
 *  Parameters:
 *    (String) msg - the message that you want to sent.
 *    (String) node - the node on which you want to publish.
 *  Returns:
 *     [None]
 */

publishNode : function(msg,node){
     pubiq = $iq({to: Sendify.service, type: "set"})
                .c('pubsub', {xmlns: Sendify.NS_PUBSUB})
                .c('publish', {node: node})
                .c('item')
                .c('x', {xmlns: Sendify.NS_DATA_FORMS,
                         type: "result"})
                .c('value').t('' + msg);
                this.connection.sendIQ(pubiq); 
      console.log("in Publish Node");
},

/** Function: setStatus
 *  change your present status 
 *  Parameters:
 *    (String) myJID - your Jid.
 *    (String) status - the status that you want to display on the roster.
 *    (Interger) priority - Integer b/w -127 to +127.
 *  Returns:
 *     [None]
 */

setStatus: function(myJID,status,priority){
      var iq = $pres().c("status",status)
                .up().c("priority",priority);
                this.connection.sendIQ(iq);
      },

setPresence: function(myJid, show){
      var iq = $pres().c("show",show);
                this.connection.sendIQ(iq);
},


/** Function: sendMessage
 *  sendMessage to a particular contact 
 *  Parameters:
 *    (String) to - Jid of the recipient.
 *    (String) body - body of the message.
 *    (String) type . type to message
 *  Returns:
 *     [None]
 */


sendMessage: function(to,body,type){
            var message = $msg({to: jid,
                                "type": type})
                .c('body').t(body).up()
                .c('active', {xmlns: "http://jabber.org/protocol/chatstates"});
            this.connection.send(message);
},


/** Function: getSubscribers
 *  get all the Subscribers of the  node.
 *  Parameters:
 *    (String) nodeName - name of the node.

 *  Returns:
 *     (Structure) Subscribers list {jid,subscriptionid,object}
 *     false if there is any error.
 */


getSubscribers:function(nodeName) {
            var iq = $iq({type: "get", to: Sendify.service})
                .c('pubsub',{xmlns: this.NS_PUBSUB_OWNER})
                .c('subscriptions',{node: nodeName});
                console.log("Inside getSubscribers");
            this.connection.sendIQ(iq,function(iq){
              i = 0;
              result = {};
              result.jid =[];
              result.subid =[];
              result.obj = [];
              $(iq).find('subscription').each(function () {
                  result.jid[i] = $(this).attr('jid');
                  result.subid[i] = $(this).attr('subid');
                  result.obj[i] = this;
                  i++;
              });
              console.log(result);
              return result;                
            })
  },
  

/** Function: unsubscribeNode
 *  Unsubscribe from the node.
 *  
 *  Parameters:
 *    (String) nodeName - name of the node that you want to unsubscribe.
 *    
 *  Returns:
 *    false if the unsubcription was unsuccessfull.
 *    true if succesfull.
 */


unsubscribeNode: function(nodeName){
            this.connection.sendIQ($iq({to: Sendify.service,type: "set"})
            .c('pubsub', {xmlns: this.NS_PUBSUB_OWNER})
            .c('unsubscribe', {node: nodeName,
                               jid: this.connection.jid}),function(iq){
              
              console.log(iq);
                              
            }, function unsubscribeError(iq){
        var err = $(iq).find("error");
          console.log("Not subscribed"+err);
          console.log(iq);
   
      });

},

/** Function: getPreviousPublications
 *  get the previous publications from a node.
 *  
 *  Parameters:
 *    (String) nodeName - name of the node.
 *    (String) num - number of previous publications needed.
 *  Returns:
 *    (Structure) publications list {id,data,object}
 *    false if there is any error.  
 */

getPreviousPublications: function(nodeName,num){
  console.log("In getPreviousPublications");
          var iq = $iq({type: "get", to: this.service})
                .c('pubsub',{xmlns: this.NS_PUBSUB})
                .c('items',{node: nodeName, max_items: num});

            this.connection.sendIQ(iq,function(iq){
              i = 0;
              result = {};
              result.id = [];
              result.data = [];
              result.obj = [];
              $(iq).find('item').each(function () {
                  result.id[i] = $(this).attr('id');
                  result.data[i] = $(this).find('x');
                  result.obj[i] = this;
                  i++;
              });
              console.log(result);
              return result;                
            })

},

/** Function: deleteNode
 *  Delete a node.
 *  
 *  Parameters:
 *    (String) nodeName  - name of the node that you want to delete.
 *    
 *  Returns:
 *    true if the deletion was succesful.
 *    false if the deletion was unsuccesful.
 */

  deleteNode: function(nodeName){
            this.connection.sendIQ($iq({to: Sendify.service,type: "set"})
            .c('pubsub', {xmlns: this.NS_PUBSUB_OWNER})
            .c('delete', {node: nodeName}));

  },

/** Function: disconnect
 *  disconnect form the server.
 *  
 *  Parameters:
 *    (String) msg - msg that you want to keep as the status.
 *
 *  Returns:
 *    true if the disconnection is successfull.
 *    false if the disconnection was unsuccessfull.
 */
  disconnect: function(msg){
        this.connection.disconnect(msg);
        this.connection = null;
        this.service = null;
        this.node = null;
        this.jid = null;
        this.pass = null;
  }
};
