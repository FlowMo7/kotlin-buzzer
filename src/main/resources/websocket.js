var socket;

function connectToWebsocket(webSocketUrl, onMessageReceived) {
    socket = new WebSocket(webSocketUrl);
    socket.onopen = function (e) {
        console.log("[open] Connection established");
    };

    socket.onmessage = function (event) {
        console.log('[message] Data received from server: ' + event.data);
        onMessageReceived(event.data);
    };

    socket.onclose = function (event) {
        if (event.wasClean) {
            console.log('[close] Connection closed cleanly, code=' + event.code + ' reason=' + event.reason);
        } else {
            console.log('[close] Connection died');
        }
        // connectToWebsocket(webSocketUrl);    //TODO
    };

    socket.onerror = function (error) {
        console.log('[error] ' + error.message);
        connectToWebsocket(webSocketUrl);
    };
}

function admin(pathPrefix, lobbyCode) {
    connectToWebsocket(
        pathPrefix + '/ws/feed/' + lobbyCode,
        function (data) {
            console.log(data)
            var text = '<ul>'
            var payload = JSON.parse(data);
            payload.participants.forEach(function (item) {
                text += '<li>' + item.name + ' (' + item.buzzed + ')</li>';
            })
            text += '</ul>';
            document.getElementById('participant_list').innerHTML = text
        }
    );
}

function participant(pathPrefix, lobbyCode) {
    connectToWebsocket(
        pathPrefix + '/ws/feed/' + lobbyCode,
        function (data) {
            console.log(data)
        }
    );
}

function sendBuzz(pathPrefix, participantName, lobbyCode) {
    socket.send(JSON.stringify({
        type: 'Buzz',
        lobbyCode: lobbyCode,
        participant: participantName
    }));
}

function resetBuzzes(pathPrefix, lobbyCode) {
    socket.send(JSON.stringify({
        type: 'Clear',
        lobbyCode: lobbyCode
    }));
}

// function checkWebSocketConnectionAlive() {
//     if (socket.readyState !== WebSocket.OPEN) {
//         //loadContentPerGet();
//     }
//     if (socket.readyState === WebSocket.CLOSED) {
//         connectToWebsocket();
//     }
// }

// setInterval(function() {
//     checkWebSocketConnectionAlive();
// }, 5000);