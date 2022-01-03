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
        let sortedParticipants = participants;
        sortedParticipants.sort();
        sortedParticipants.forEach(function (item) {
            text += '<li>' + item.name + '</li>';
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
    sendWebsocketMessage({type: 'Buzz'});
}

function resetBuzzes() {
    sendWebsocketMessage({type: 'Clear'});
}

function getParameterByName(name, url = window.location.href) {
    name = name.replace(/[\[\]]/g, '\\$&');
    let regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
}

