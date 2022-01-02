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

function getXnd(place) {
    if (place === 1) {
        return '1st'
    } else if (place === 2) {
        return '2nd';
    } else if (place === 3) {
        return '3rd';
    } else {
        return place + 'th';
    }
}

function buildParticipantsList(participants) {
    if (participants.length === 0) {
        return '<i>No participants yet</i>';
    } else {
        var text = '<ul>'
        participants.forEach(function (item, index) {
            text += '<li>' + item.name;

            if (item.buzzed) {
                text += ' (BUZZED ' + getXnd(index + 1) + ')';
            }
            text += '</li>';
        })
        text += '</ul>';
        return text;
    }
}

function buildBuzzesList(participants) {
    if (participants.length === 0) {
        return '';
    } else {
        var text = ''
        participants.filter(it => it.buzzed === true).forEach(function (item, index) {
            if (index === 0) {
                text += '<div class="host-buzz-entry host-buzz-entry-first">' + item.name + '<span class="host-buzz-place">' + getXnd(index + 1) + '</span></div><br />\n';
            } else {
                text += '<div class="host-buzz-entry host-buzz-entry-not-first">' + item.name + '<span class="host-buzz-place">' + getXnd(index + 1) + '</span></div><br />\n';
            }
        })
        return text;
    }
}

function host(pathPrefix, lobbyCode) {
    connectToWebsocket(
        pathPrefix + '/ws/host/' + lobbyCode,
        function (data) {
            console.log(data)
            let payload = JSON.parse(data);
            document.getElementById('participant_count').innerHTML = payload.participants.length;
            document.getElementById('participant_list').innerHTML = buildParticipantsList(payload.participants);
            document.getElementById('buzzes_list').innerHTML = buildBuzzesList(payload.participants);
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

