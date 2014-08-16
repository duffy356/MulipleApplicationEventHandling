/** JavaScript class with wrapped WebSocket
 *  This class handles the onopen, onmessage, etc with the callbacks..
 **/
function MAEHWebsocket(uri, user, app) {
	/* Check the params */
    isStringArgValid(uri, "URI");
    isStringArgValid(user, "User");
    isStringArgValid(app, "Application");

    /* Reserve var for socket */
    var socket;
    var callbacks = [];
    var maehUser = user;
    var maehApplication = app;
    var maehWebsocketId = null;
    this.stateComponent = null;

    var self = this;
    var connectionRetries = 1;

    this.connect = function () {
        socket = new WebSocket(uri);

        socket.onopen = function (event) {
            // Reset Connection Retry counter;
            connectionRetries = 1;

            // Call Statechangercallback with new State
            if (self.stateComponent !== null) {
                self.stateComponent(socket.readyState);
            }

            // Send Registermessage
            var stateless_reg_req = {};
            stateless_reg_req.messagetype = 'STATELESS_REG_REQ';
            stateless_reg_req.maehUser = maehUser;
            stateless_reg_req.maehApplication = maehApplication;

            for (var i = 0, len = callbacks.length; i < len; i++) {
                if (stateless_reg_req.newCallbacks === undefined) {
                    stateless_reg_req.newCallbacks = [];
                }
                stateless_reg_req.newCallbacks.push(callbacks[i].getName());
            }
            socket.send(JSON.stringify(stateless_reg_req));
        };

        socket.onerror = function (event) {
            // Call Statechangercallback with new State
            if (self.stateComponent !== null) {
                self.stateComponent(socket.readyState);
            }

            console.log(event);
        };

        socket.onclose = function (event) {
            // Call Statechangercallback with new State
            if (self.stateComponent !== null) {
                self.stateComponent(socket.readyState);
            }

            console.log(event);

            // get time for next reconnection attempt
            var time = generateConnectionInterval(connectionRetries);

            // Set a Timeout and invoke Method for Connection-Retry
            setTimeout(function () {
                // Increment Connection Retries by one
                connectionRetries++;

                // Invoke the connect Method
                self.connect();
            }, time);
        };

        socket.onmessage = function (event) {
            var data = JSON.parse(event.data);
            if (data.messagetype === "STATELESS_REG_RES") {
                maehWebsocketId = data.maehWebsocketId;
            } else if (data.messagetype === "EVT_MSG") {
                for (var i = 0, len = callbacks.length; i < len; i++) {
                    if (callbacks[i].getName() === data.callback) {
                        callbacks[i].callback(new ReceivedMessage(data));
                    }
                }
            } else if (data.messagetype === "EVT_RES") {
                for (var i = 0, len = callbacks.length; i < len; i++) {
                    if (callbacks[i].getName() === data.callback) {
                        callbacks[i].callback(new ReceivedResponse(data));
                    }
                }
            }
        }

    };

    function generateConnectionInterval (retries) {
        var maxInterval = (Math.pow(2, retries) - 1) * 1000;

        // Set Maximum of Interval to 20 Seconds
        if (maxInterval > 20 * 10000) {
            maxInterval = 20 * 10000;
        }

        // Generate number between 0 and 1 and multiply with maxInterval
        return Math.random() * maxInterval;
    }

    /* Handle Callbacks */
    this.addCallback = function(callback) {
        if (typeof (callback) !== "object"
                || !callback instanceof Callback) {
            throw new Error("Must be a callback");
        } else {
            for (var i = 0, len = callbacks.length; i < len; i++) {
                if (callbacks[i].getName() === callback.getName()) {
                    throw new Error("Callback already registered");
                }
            }
            callbacks.push(callback);
        }
    };

    /* Send message */
    this.send = function (event) {
        if (typeof (event) !== "object"
                || !event instanceof MAEHMessage) {
            throw new Error("Object must be a MAEHMessage");
        }
        event.messagetype = 'EVT_REQ';
        socket.send(JSON.stringify(event));
    };

    /* Getter for the User */
    this.getUser = function() { return maehUser };

    /* Getter for the Application */
    this.getApplication = function () { return maehApplication};
}

MAEHWebsocket.prototype.setStateChangerCallback = function(func) {
    if (typeof (func) !== "function") {
        throw new Error ("StateChangerCallback must be a function.");
    }

    this.stateComponent = func;
};

function Callback(name, action) {
    isStringArgValid(name, "Name");
    if (typeof (action) !== "function") {
        throw new Error ("Action must be a function.");
    } else {
        var name = name;
        var action = action;

        this.getName = function () {
            return name;
        };

        this.callback = action;
    }
}

function ReceivedMessage(obj) {
    var callbackName = obj.callback;
    var data = obj.data;
    var senderId = obj.senderId;

    this.getCallbackName = function () { return callbackName };
    this.getProperty = function (name) { return data[name]; };
    this.getSender = function () { return senderId };
}

function ReceivedResponse(obj) {
    var receivers = obj.receivers;
    var callback = obj.callback;

    this.getReceivers = function () { return receivers };
    this.getCallback = function () { return callback };
}

function MAEHMessage(callbackName) {
    this.receivers = [];
    this.callback = callbackName;
    this.data = {};
}

MAEHMessage.prototype.addReceiver = function(rec) {
    if (typeof (rec) !== "object"
            || !rec instanceof MAEHMessageReceiver) {
        throw new Error("invalid Object Receiver must be a MAEHMessageReceiver");
    } else {
        // Validate Receiver
        this.receivers.push(rec);
    }
}

function MAEHMessageReceiver() {
    this.receivertype = null;
    this.callback = null;
    this.username = null;
    this.application = null;
    this.id = null;
}

function isStringArgValid(param, mess) {
    if (param !== undefined
            && param.length > 0) {
        return true;
    }

    throw new Error("Invalid Argument: " + mess);
}