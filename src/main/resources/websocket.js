var socket;

function connectToWebsocket(webSocketUrl, onMessageReceived, onConnectionStateChanged) {
    socket = new WebSocket(webSocketUrl);
    socket.onopen = function (e) {
        console.log("[open] Connection established");
        onConnectionStateChanged(true);
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
        onConnectionStateChanged(false);
        // connectToWebsocket(webSocketUrl);    //TODO
    };

    socket.onerror = function (error) {
        console.log('[error] ' + error.message);
        onConnectionStateChanged(false);
        connectToWebsocket(webSocketUrl);
    };
}

function host(pathPrefix, lobbyCode) {
    connectToWebsocket(
        pathPrefix + '/ws/host/' + lobbyCode,
        function (data) {
            console.log(data)
            let payload = JSON.parse(data);
            if (payload.participants.length === 0) {
                document.getElementById('participant_list').innerHTML = '<i>No participants yet</i>';
            } else {
                var text = '<ul>'
                payload.participants.forEach(function (item, index) {
                    text += '<li>' + item.name;

                    if (item.buzzed) {
                        text += ' (BUZZED ';
                        if (index === 0) {
                            text += '1st'
                        } else if (index === 1) {
                            text += '2nd';
                        } else if (index === 2) {
                            text += '3rd';
                        } else {
                            text += (index + 1) + 'th';
                        }
                        text += ')'
                    }
                    text += '</li>';
                })
                text += '</ul>';
                document.getElementById('participant_list').innerHTML = text;
            }
        },
        function (isConnected) {
            if (isConnected) {
                document.getElementById('connection_status').innerHTML = '';
            } else {
                document.getElementById('connection_status').innerHTML = 'Not connected. You will not get updates on buzzes. Please try refreshing this page.';
            }
        }
    );
}

function participant(pathPrefix, lobbyCode) {
    let nickname = getParameterByName('nickname');
    connectToWebsocket(
        pathPrefix + '/ws/feed/' + lobbyCode + '?nickname=' + nickname,
        function (data) {
            console.log(data)
            let payload = JSON.parse(data);
            payload.participants.forEach(function (participantState) {
                if (participantState.name === nickname) {
                    if (participantState.buzzed === true) {
                        document.getElementById('buzzer_button').classList.add('buzzer-buzzed');
                        document.getElementById('buzzer_button').classList.remove('buzzer-ready');
                    } else {
                        document.getElementById('buzzer_button').classList.add('buzzer-ready');
                        document.getElementById('buzzer_button').classList.remove('buzzer-buzzed');
                    }
                }
            })
        },
        function (isConnected) {
            if (isConnected) {
                document.getElementById('connection_status').innerHTML = '';
            } else {
                document.getElementById('connection_status').innerHTML = 'Not connected. Your buzzes will not count. Please try refreshing this page.';
            }
        }
    );
}

function sendBuzz() {
    socket.send(JSON.stringify({
        type: 'Buzz'
    }));
}

function resetBuzzes() {
    socket.send(JSON.stringify({
        type: 'Clear'
    }));
}


function getParameterByName(name, url = window.location.href) {
    name = name.replace(/[\[\]]/g, '\\$&');
    let regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
}

