function buildParticipantsList(participants) {
    if (participants.length === 0) {
        return '<i>No participants yet</i>';
    } else {
        var text = '<ul>'
        let sortedParticipants = participants.slice();
        sortedParticipants.sort((a, b) => a.name.localeCompare(b.name));
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
            let date = convertDateToLocalTimezone(item.buzzedAt);
            let timeString = withLeadingZero(date.getHours()) + ':' + withLeadingZero(date.getMinutes()) + ':' + withLeadingZero(date.getSeconds()) + '.' + withLeadingZero(date.getMilliseconds(), true);

            let entryCSSClass;
            if (index === 0) {
                entryCSSClass = 'host-buzz-entry-first';
            } else {
                entryCSSClass = 'host-buzz-entry-not-first';
            }

            text += '<div class="host-buzz-entry ' + entryCSSClass + '">' +
                '<div class="host-buzz-place">' + getXnd(index + 1) + '</div>' +
                '<div>' + item.name + '</div>' +
                '<div class="host-buzz-time" title="Buzzed at">' + timeString + '</div>' +
                '</div>';
        })
        return text;
    }
}

function host(scheme, hostname, path, lobbyCode) {
    connectToWebsocket(
        scheme + '://' + hostname + path + 'ws/host/' + lobbyCode + '?secret=' + getParameterByName('secret'),
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

function monitor(scheme, hostname, path, lobbyCode) {
    connectToWebsocket(
        scheme + '://' + hostname + path + 'ws/monitor/' + lobbyCode,
        function (data) {
            let payload = JSON.parse(data);
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

function participant(scheme, hostname, path, lobbyCode) {
    let nickname = getParameterByName('nickname');
    connectToWebsocket(
        scheme + '://' + hostname + path + 'ws/feed/' + lobbyCode + '?nickname=' + nickname,
        function (data) {
            let payload = JSON.parse(data);
            payload.participants.forEach(function (participantState) {
                if (participantState.name === nickname) {
                    if (participantState.buzzed === true) {
                        document.getElementById('buzzer_button').classList.add('buzzer-buzzed');
                        document.getElementById('buzzer_button').classList.remove('buzzer-ready');
                        document.getElementById('buzzer_button_image').src = path + 'icon/buzzer_buzzed.png';
                    } else {
                        document.getElementById('buzzer_button').classList.add('buzzer-ready');
                        document.getElementById('buzzer_button').classList.remove('buzzer-buzzed');
                        document.getElementById('buzzer_button_image').src = path + 'icon/buzzer.svg';
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
    preloadImage(path + 'icon/buzzer.svg');
    preloadImage(path + 'icon/buzzer_buzzed.png');
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

function convertDateToLocalTimezone(date) {
    let parsedAndConverted = new Date((typeof date === "string" ? new Date(date) : date).toLocaleString());
    parsedAndConverted.setMilliseconds(getMilliseconds(date));
    return parsedAndConverted;
}

function withLeadingZero(number, threeDigits = false) {
    return ((threeDigits && number < 100) ? '0' : '') + (number < 10 ? '0' : '') + number;
}

function getMilliseconds(date) {
    let split = date.split('.');
    if (split.length > 3) {
        return Math.round(parseInt(split[1]) / 1000);
    } else {
        return Math.round(parseInt(split[1]));
    }
}

function preloadImage(imageUrl) {
    let img = new Image();
    img.src = imageUrl;
}