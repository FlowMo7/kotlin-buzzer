var socket;

function connectToWebsocket(webSocketUrl, onMessageReceived, onConnectionStateChanged) {
    socket = new ReconnectingWebSocket(webSocketUrl);
    socket.onopen = function (e) {
        console.log("[websocket] Connection established");
        onConnectionStateChanged(true);
    };

    socket.onmessage = function (event) {
        onMessageReceived(event.data);
    };

    socket.onclose = function (event) {
        if (event.wasClean) {
            console.log('[websocket] Connection closed cleanly, code=' + event.code + ' reason=' + event.reason);
        } else {
            console.log('[websocket] Connection died');
        }
        onConnectionStateChanged(false);
    };

    socket.onerror = function (error) {
        console.log('[websocket] Error: ' + error.message);
        onConnectionStateChanged(false);
    };
}

function sendWebsocketMessage(payload) {
    socket.send(JSON.stringify(payload));
}
