(function (Battleship) {
    "use strict";

    var version = 0x01;

    //protocol
    Battleship.LOGIN = 0x01;
    Battleship.LOGIN_SUCCESS = 0x02;

    Battleship.doLogin = function (uri, name, onSuccess, onFail) {
        connect(uri, function() {
            var payload = {
                login: name
            };

            sendMessage(Battleship.LOGIN, payload, function (event) {
                var data = event.data;
                data = JSON.parse(data);

                if (data.type == Battleship.LOGIN_SUCCESS) {
                    var playerId = data.payload.id;
                    console.log("Login successful. ID: " + playerId);
                    onSuccess(playerId);
                }
                else {
                    onFail();
                }
            });
        });
    };

    function sendMessage(type, payload, callback) {
        var message = {
            version: version,
            type: type,
            payload: payload
        };
        ws.onmessage = callback;
        ws.send(JSON.stringify(message));
    }

    var ws;

    function connect(url, onOpen) {
        ws = new WebSocket(url);
        ws.onopen = onOpen;
        ws.onclose = onclose;
        ws.onerror = onerror;
    }

    function onclose(event) {
        console.log("Web Socket closed");
    }

    function onerror(event) {
        console.log(event);
    }

}(window.Battleship = window.Battleship || {}));
