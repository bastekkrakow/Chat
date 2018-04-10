function onLoad() {
  //  var wsUri = "ws://localhost:8080/chat";
    var wsUri = "ws://77.55.237.32:8282/chat";

    websocket = new WebSocket(wsUri);
    websocket.onopen = function (evt) {
        onOpen(evt)
    };
    websocket.onclose = function (evt) {
        onClose(evt)
    };
    websocket.onmessage = function (evt) {
        onMessage(evt)
    };
    websocket.onerror = function (evt) {
        onError(evt)
    };
}

function onOpen(evt) {
    state.className = "success";
    state.innerHTML = "Connected to server";
}

function onClose(evt) {
    state.className = "fail";
    state.innerHTML = "Not connected";
    connected.innerHTML = "0";
}

function onMessage(evt) {
    // There are two types of messages:
    // 1. a chat participant message itself
    // 2. a message with a number of connected chat participants
    var message = evt.data;

    if (message.startsWith("log:")) {
        message = message.slice("log:".length);
        log.innerHTML = log.innerHTML + "<li class = \"message\">" + message + "</li>";
        log.scrollTop = log.scrollHeight;
    }else if (message.startsWith("connected:")) {
        message = message.slice("connected:".length);
        connected.innerHTML = message;
    }else if(message.startsWith("server:")){
        message = message.slice("server:".length);
        log.innerHTML = log.innerHTML + "<li class = \"server\">" + message + "</li>";
        log.scrollTop = log.scrollHeight;
    }
}

function onError(evt) {
    state.className = "fail";
    state.innerHTML = "Communication error";
}

function addMessage() {
    var message = chat.value;
    chat.value = "";
    websocket.send(message);
}