/**
 * MAEHWebSocketServer
 * A WebSocketServer (WSS) for Eventhandling over multiple applications based
 * on the node-module ws and the concept of MAEH
 *
 * Copyright(c) 2014 Dominik Bauer <duffy356@gmail.com>
 * MIT Licensed
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the 'Software'), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

// Load the library(ws) for the WebSocketServer(WSS) and register it on port 13337
var WebSocketServer = require('ws').Server;
var port = 13337;
var wss = new WebSocketServer({port: port});

// Load the library for utility functions
var utils = require('./../js/Utils');

// Set sequences and debug-mode
var seq_maehWebsocketId = 0, seq_callbackid = 0;
var debug = true;

// Log WSS start-up
console.log("%s WebSocketServer startet at localhost:"+ port, utils.getActTime());

/**
 * Called by the WSS when a WebSocket omits an event
 * A WebSocket from the client calls an event on the server
 * - clients shuts down the connection --> 'close'
 * - WebSocket from client sends a message --> 'message'
 *
 * @param ws - the websocket, which receives the action
 */
wss.on('connection', function(ws) {

    // Log the closing-reason
	ws.on('close', function(error) {
		if (ws.maehWebsocketId !== undefined) {
			console.log('Client with id ' + ws.maehWebsocketId + ' terminated');
		} else {
			console.log("Unknown connection closed");
		}
	});

    // handle the logic of MAEH
    ws.on('message', function(message) {
        if (debug) {
            console.log('%s received: %s', utils.getActTime(), message);
        }

        var maehMessage = null;

        // Parse the JSON-String to JavaScript object
        try {
            maehMessage = JSON.parse(message);
        } catch (e) {
            maehMessage = null;
        }

        // Object is valid?
        if (maehMessage != null && maehMessage.messagetype !== undefined) {
            if (maehMessage.messagetype === 'REG_REQ'
                && utils.isString(maehMessage.maehUser)
                && utils.isString(maehMessage.maehApplication)) {
                /**
                 * Handle the Registration Request
                 * of a Stateful Connection
                 **/
                registerClient(ws, maehMessage);

                // Send Response with the granted Id
                var reg_res = JSON.stringify({messagetype: 'REG_RES', maehWebsocketId: ws.maehWebsocketId});
                send(ws, reg_res);
            } else if (maehMessage.messagetype === 'STATELESS_REG_REQ'
                && utils.isString(maehMessage.maehUser)
                && utils.isString(maehMessage.maehApplication)) {
                /**
                 * Handle the Registration Request
                 * of a Stateless Connection
                 **/
                registerClient(ws, maehMessage);

                var stateless_res = {};
                stateless_res.messagetype = 'STATELESS_REG_RES';
                stateless_res.maehWebsocketId = ws.maehWebsocketId;

                /* Add Callbacks to Socket */
                var newCallbacks = addCallbacks(ws, maehMessage);

                if (newCallbacks != null) {
                    stateless_res.newCallbacks = newCallbacks;
                }

                send(ws, JSON.stringify(stateless_res));
            } else if (maehMessage.messagetype === 'INT_REQ'
                            && ws.maehWebsocketId !== undefined) {
                /**
                 * Handle the Update Request for the Callbacks
                 * of a Stateful connection
                 **/
                /* Add Callbacks to Socket */
                var newCallbacks = addCallbacks(ws, maehMessage);

                var removedCallbacks = null;

                if (utils.isArray(maehMessage.removedCallbacks)) {
                    for (var i = 0, len = maehMessage.removedCallbacks.length; i < len; i++) {
                        var callbackName = maehMessage.removedCallbacks[i];
                        for (var j = 0, l = ws.callbacks.length; j < l; j++) {
                            if (ws.callbacks[j].name === callbackName) {
                                ws.callbacks.pop(ws.callbacks[j]);
                                var callback = {};
                                callback.name = callbackName;
                                callback.id = -1;
                                if (removedCallbacks == null) {
                                    removedCallbacks = [];
                                }
                                removedCallbacks.push(callback);
                                break;
                            }
                        }
                    }
                }

                // Create Object for Response
                var int_res = { messagetype: 'INT_RES' };
                // set New Callbacks
                if (newCallbacks != null) {
                    int_res.newCallbacks = newCallbacks;
                }
                // set Removed Callbacks
                if (removedCallbacks != null) {
                    int_res.removedCallbacks = removedCallbacks;
                }

                send(ws, JSON.stringify(int_res));
            } else if (maehMessage.messagetype === 'EVT_REQ'
                            && ws.maehWebsocketId !== undefined) {
                /*
                 * Handle the Event requests of
                 * Stateless or stateful connections
                 **/
                if (utils.isObject(maehMessage.data)
                        && utils.isArray(maehMessage.receivers)) {
                    var actReceiver;
                    var receiversCnt = 0;
                    for (var i = 0, len = maehMessage.receivers.length; i < len; i++) {
                        actReceiver = maehMessage.receivers[i];

                        if (utils.isObject(actReceiver)
                                    && utils.isString(actReceiver.callback)) {
                            if (actReceiver.receivertype === 'ID') {
                                // Send by ID
                                for (var j = 0, l = wss.clients.length; j < l; j++) {
                                    if (wss.clients[j].maehWebsocketId === actReceiver.id) {
                                        if (sendEventmessageToClient(ws, wss.clients[j], actReceiver.callback, maehMessage)) {
                                            receiversCnt++;
                                        }
                                    }
                                }
                            } else if (actReceiver.receivertype === 'BROADCAST') {
                                // Make Broadcast
                                for (var j = 0, l = wss.clients.length; j < l; j++) {
                                    if (ws !== wss.clients[j]) {
                                        if (sendEventmessageToClient(ws, wss.clients[j], actReceiver.callback, maehMessage)) {
                                            receiversCnt++;
                                        }
                                    }
                                }
                            } else if (actReceiver.receivertype === 'USER') {
                                // Send to User with the name
                                for (var j = 0, l = wss.clients.length; j < l; j++) {
                                    if (ws !== wss.clients[j]
                                        && wss.clients[j].maehUser === actReceiver.username) {
                                        if (sendEventmessageToClient(ws, wss.clients[j], actReceiver.callback, maehMessage)) {
                                            receiversCnt++;
                                        }
                                    }
                                }
                            } else if (actReceiver.receivertype === 'APPLICATION') {
                                // Send to Applications with the name
                                for (var j = 0, l = wss.clients.length; j < l; j++) {
                                    if (ws !== wss.clients[j]
                                        && wss.clients[j].maehApplication === actReceiver.application) {
                                        if (sendEventmessageToClient(ws, wss.clients[j], actReceiver.callback, maehMessage)) {
                                            receiversCnt++;
                                        }
                                    }
                                }
                            } else if (actReceiver.receivertype === 'USERAPPLICATION') {
                                // Send to Users && Application
                                for (var j = 0, l = wss.clients.length; j < l; j++) {
                                    if (ws !== wss.clients[j]
                                        && wss.clients[j].maehUser === actReceiver.username
                                        && wss.clients[j].maehApplication === actReceiver.application) {
                                        if (sendEventmessageToClient(ws, wss.clients[j], actReceiver.callback, maehMessage)) {
                                            receiversCnt++;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Send the number of receiving clients back to the sending callback
                    send(ws, JSON.stringify({messagetype : 'EVT_RES',
                        "receivers" : receiversCnt,
                        "callback" : maehMessage.callback}));
                }
            }
        }
    });
});

/** Adds a new Callback-Identifier to the registered callbacks of
 * the websocket
 * If the callback is already in the list, the callback will get replaced by the new callback
 *
 * @param ws - the MAEH-WebSocket
 * @param maehMessage - the message, which contains the new callback-actions
 * @return newCallbacks - the new callback-actions for the MAEH-WebSocket
 **/
function addCallbacks(ws, maehMessage) {
    var newCallbacks = null;

    if (utils.isArray(maehMessage.newCallbacks)) {
        for (var i = 0, len = maehMessage.newCallbacks.length; i < len; i++) {
            var socketCallbackIdx = -1;
            var newCallback = {};
            newCallback.name = maehMessage.newCallbacks[i];
            newCallback.id = seq_callbackid++;

            if (newCallbacks == null) {
                newCallbacks = [];
            }
            newCallbacks.push(newCallback);

            for (var j = 0, callbackLen = ws.callbacks.length; j < callbackLen; j++) {
                if (ws.callbacks[j].name === newCallback.name) {
                    socketCallbackIdx = j;
                    break;
                }
            }
            if (socketCallbackIdx == -1) {
                ws.callbacks.push(newCallback);
            } else {
                ws.callbacks[socketCallbackIdx] = newCallback;
            }
        }
    }

    return newCallbacks;
};

/**
 * Logs the message on the console and sends it to the given WebSocket
 *
 * @param ws - the websocket
 * @param json - the JSON message
 **/
function send (ws, json) {
    if (debug) {
        console.log('%s send: %s', utils.getActTime(), json);
    }

    ws.send(json);
}

/**
 * Registers the transmitted properties of the new WebSocket endpoint
 * this allows a WebSocket to be a MAEHWebSocket
 *
 * @param ws - the websocket for upgrade
 * @param maehMessage - the MAEH registration message
 */
function registerClient(ws, maehMessage) {
    // set user and application of the socket from the contends of the message
    ws.maehUser = maehMessage.maehUser;
    ws.maehApplication = maehMessage.maehApplication;
    ws.callbacks = [];

    // grant id for connected socket
    ws.maehWebsocketId = seq_maehWebsocketId++;

    if (debug) {
        console.log('Client with id: ' + ws.maehWebsocketId + ' registered'
            + '\n\t username: ' + ws.maehUser
            + '\n\t application: ' + ws.maehApplication);
    }
}

/**
 * Checks the receiverSocket and sends the MAEH-Event-Message
 *
 * @param senderSocket - the socket, which sends the message
 * @param receiverSocket - the socket, which gets checked and receives the message
 * @param callbackName - the name of the receiver's callback
 * @param maehMessage - the message
 * @returns {boolean} - true -> message sent
 */
function sendEventmessageToClient(senderSocket, receiverSocket, callbackName, maehMessage) {
    if (hasSocketRegisteredCallback(receiverSocket, callbackName)) {
        send(receiverSocket, JSON.stringify({messagetype: 'EVT_MSG',
            "senderId" : senderSocket.maehWebsocketId,
            "callback" : callbackName,
            "data" : maehMessage.data}));
        return true;
    }
    return false;
}

/**
 * Checks if the given MAEH-websocket registered the callback action
 *
 * @param ws - the MAEH-websocket, which gets checked
 * @param callback - the name of the callback-action
 * @returns {boolean} - true -> the callback-action is registered on the socket
 */
function hasSocketRegisteredCallback(ws, callback) {
    for (var i = 0, len = ws.callbacks.length; i < len; i++) {
        if (ws.callbacks[i].name === callback) {
            return true;
        }
    }

    return false;
}

