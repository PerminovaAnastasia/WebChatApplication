'use strict';
var username = "";
var toWhomClick = null;
var choise = false;

function takeDate() {
    var date = new Date();
    var time = ('0' + date.getDate()).slice(-2) + '.' + ('0' + (date.getMonth() + 1)).slice(-2) + "<br>";
    time += ('0' + date.getHours()).slice(-2) + ':' + ('0' + date.getMinutes()).slice(-2);
    time += ':' + ('0' + date.getSeconds()).slice(-2);
    return time;
}

var theHistory = function (Name, messageText, isDeleted, isEdited, time) {
    return {
        username: Name,
        delete: isDeleted,
        edit: isEdited,
        text: messageText,
        time: time,
        id: ""
    };
};

var appState = {
    mainUrl: 'chat',
    HistoryList: [],
    token: '0'
};

function run() {

    var appContainer = document.getElementById("buttonSend");
    appContainer.addEventListener('click', onAddButtonClick);

    var inputName = document.getElementById("userName");
    inputName.addEventListener('focusout', inputUserName);

    document.getElementsByClassName("glyphicon")[0].style.display = "none";
    document.getElementsByClassName("glyphicon")[1].style.display = "none";
    document.getElementsByClassName("glyphicon")[0].addEventListener('click', clickGlyphEdit);
    document.getElementsByClassName("glyphicon")[1].addEventListener('click', clickGlyphDel);

    var text = document.getElementById("tableMessage");
    text.addEventListener('keypress', ifTextInput);

    restore();
    window.setInterval(restore, 500);

    connectionServer(true);
}
function createAllHistory(allHistory) {

    for (var j = 0; j < allHistory.length; j++) {
        var prov = true;
        for (var i = 0; i < appState.HistoryList.length; i++) {
            if (appState.HistoryList[i].id == allHistory[j].id) {
                appState.HistoryList[i] = allHistory[j];
                updateOneMessage(appState.HistoryList[i]);
                prov = false;
                break;
            }
        }
        if (prov)
            addRowsToTable(allHistory[j]);
    }
}

function updateOneMessage(message) {

    var idTemp = document.getElementById(message.id);
    connectionServer(true);

    if (message.delete == true) {
        idTemp.childNodes[2].innerHTML = "The message was deleted";
        idTemp.childNodes[1].innerHTML = '<i class="glyphicon glyphicon-trash col-stage"></i>';
        return;
    }
    if (message.edit == true) {
        idTemp.childNodes[2].innerText = message.text;
        idTemp.childNodes[1].innerHTML = '<i class="glyphicon glyphicon-pencil col-stage"></i>'
        return;
    }
    idTemp.childNodes[2].innerText = message.text;

}

function restore(continueWith) {
    var url = appState.mainUrl + '?token=' + appState.token;

    get(url, function (responseText) {
        console.assert(responseText != null);

        var response = JSON.parse(responseText);

        appState.token = response.token;
        createAllHistory(response.messages);

        continueWith && continueWith();
    });
}

function onAddButtonClick() {

    var inputName = document.getElementById("userName");
    while (username.length === 0 || username == null) {
        username = prompt("Input your username!");
    }
    inputName.value = username;

    var todoText = document.getElementById('tableMessage');
    if (!/\S/.test(todoText.value)) {
        todoText.value = '';
        return;
    }
    var task = theHistory(username, todoText.value, false, false, takeDate());
    if (toWhomClick == null) {
        post(appState.mainUrl, JSON.stringify(task), function () {
        });
        connectionServer(true);
    }
    else {
        if (choise == true) {
            var idTemp = toWhomClick.getAttribute('id');
            for (var i = 0; i < appState.HistoryList.length; i++) {
                if (idTemp == appState.HistoryList[i].id) {
                    while (username != appState.HistoryList[i].username) {
                        username = prompt("Input neccesary username!");
                    }
                    if (appState.HistoryList[i].delete == true) {
                        toWhomClick.style.background = '#FFFFE0';
                        editable(false);
                        toWhomClick = null;
                        choise = false;
                        todoText.value = '';
                        return;
                    }
                    appState.HistoryList[i].edit = true;
                    appState.HistoryList[i].text = todoText.value;

                    var task = theHistory(username, appState.HistoryList[i].text, appState.HistoryList[i].delete, appState.HistoryList[i].edit, appState.HistoryList[i].time);
                    task.id = appState.HistoryList[i].id;
                    put(appState.mainUrl + '?id=' + appState.HistoryList[i].id, JSON.stringify(task), function () {
                        /*continueWith();*/
                    });
                    break;

                }
            }
            toWhomClick.style.background = '#FFFFE0';

            editable(false);
            toWhomClick = null;
            choise = false;
        }
        else {
            appState.HistoryList.push(task);
            addRowsToTable(task);
        }
    }
    todoText.value = '';

    restore();
}

function addRowsToTable(value) {
    var scrolling = document.getElementsByClassName('my-table')[0];
    var scrollIsEnd = false;
    var heightTable = scrolling.clientHeight;
    if (scrolling.scrollHeight - scrolling.scrollTop <= heightTable + 50)
        scrollIsEnd = true;

    var Mtable = document.getElementsByClassName('table')[0];
    var row = Mtable.insertRow(-1);
    row.style.background = "#FFFFE0";
    row.addEventListener('click', clickMessage);
    appState.HistoryList.push(value);
    createItem(value, row);
    if (scrollIsEnd == true)
        scrolling.scrollTop = scrolling.scrollHeight;

}

function createItem(value, row) {

    var td1 = document.createElement("td");
    var td2 = document.createElement("td");
    var td3 = document.createElement("td");
    var tdGlyph = document.createElement("td");

    td1.classList.add("col-name");
    td2.classList.add("col-text");
    td3.classList.add("col-data");

    row.appendChild(td1);
    row.appendChild(tdGlyph);
    row.appendChild(td2);
    row.appendChild(td3);
    //
    row.setAttribute("id", value.id);

    td1.innerHTML = value.username;
    td2.innerText = value.text;
    td3.innerHTML = value.time;

    if (value.delete == true) {
        tdGlyph.innerHTML = '<i class="glyphicon glyphicon-trash col-stage"></i>';
    }
    else if (value.edit == true) {
        tdGlyph.innerHTML = '<i class="glyphicon glyphicon-pencil col-stage"></i>';
    }
}

function inputUserName() {
    var inputName = document.getElementById("userName");
    username = inputName.value;
}

function clickMessage() {
    if (toWhomClick == null) {
        toWhomClick = this;
        toWhomClick.style.background = '#E6E6FA';
        editable(true);
    }
    else {
        toWhomClick.style.background = '#FFFFE0';
        editable(false);
        if (toWhomClick != this) {
            toWhomClick = this;
            toWhomClick.style.background = '#E6E6FA';
            editable(true);
        }
        else
            toWhomClick = null;
    }
}
function editable(flag) {
    if (flag == true) {
        document.getElementsByClassName("glyphicon")[0].style.display = "inline-block";
        document.getElementsByClassName("glyphicon")[1].style.display = "inline-block";
    }
    else {
        document.getElementsByClassName("glyphicon")[0].style.display = "none";
        document.getElementsByClassName("glyphicon")[1].style.display = "none";
    }
}
function clickGlyphEdit() {
    choise = true;
    var text = document.getElementById('tableMessage');
    text.value = toWhomClick.childNodes[2].innerText;
}

function clickGlyphDel() {
    editable(false);
    toWhomClick.style.background = '#FFFFE0';

    var idTemp = toWhomClick.getAttribute('id');

    for (var i = 0; i < appState.HistoryList.length; i++) {
        if (appState.HistoryList[i].id == idTemp) {
            while (username != appState.HistoryList[i].username) {
                username = prompt("Input neccesary username!");
            }

            appState.HistoryList[i].delete = true;
            appState.HistoryList[i].text = "The message was deleted";

            var task = theHistory(username, appState.HistoryList[i].text, appState.HistoryList[i].delete, appState.HistoryList[i].edit, appState.HistoryList[i].time);
            task.id = appState.HistoryList[i].id;
            del(appState.mainUrl + '?id=' + appState.HistoryList[i].id, JSON.stringify(task), function () {
            });
            break;
        }
    }

    toWhomClick = null;
}

function ifTextInput(event) {
    var key = event.keyCode;
    if (key == 13) {
        if (event.shiftKey) {
            var text = document.getElementById('tableMessage');
            var caretPosition = getCaretPosition(text);
            text.value = text.value.slice(0, caretPosition) + '\n' + text.value.slice(caretPosition);
            setCaretPosition(text, caretPosition + 1);
        }
        else {
            onAddButtonClick();
        }
        event.preventDefault();
    }
}
function getCaretPosition(text) {
    var caretPosition = 0;
    if (document.selection) {
        var select = document.selection.createRange();
        select.moveStart('character', -text.value.length);
        caretPosition = select.text.length;
    }
    else if (text.selectionStart || text.selectionStart == '0')
        caretPosition = text.selectionStart;
    return caretPosition;
}

function setCaretPosition(text, position) {
    if (text.setSelectionRange) {
        text.focus();
        text.setSelectionRange(position, position);
    }
    else if (text.createTextRange) {
        var range = text.createTextRange();
        range.collapse(true);
        range.moveEnd('character', position);
        range.moveStart('character', position);
        range.select();
    }
}

//////////
function defaultErrorHandler(message) {
    //console.error(message);
    connectionServer(false);
    if(message == 400)
    {
        window.location.href = "/chat/resources/jsp/error400.jsp";
    }
    if(message == 500) {

        window.location.href = "/chat/resources/jsp/error500.jsp";
    }
}

function get(url, continueWith, continueWithError) {
    ajax('GET', url, null, continueWith, continueWithError);
}

function post(url, data, continueWith, continueWithError) {
    ajax('POST', url, data, continueWith, continueWithError);
}

function put(url, data, continueWith, continueWithError) {
    ajax('PUT', url, data, continueWith, continueWithError);
}

function del(url, data, continueWith, continueWithError) {
    ajax('DELETE', url, data, continueWith, continueWithError);
}

function isError(text) {
    if (text == "")
        return false;

    try {
        var obj = JSON.parse(text);
    } catch (ex) {
        return true;
    }

    return !!obj.error;
}

function ajax(method, url, data, continueWith, continueWithError) {
    var xhr = new XMLHttpRequest();

    // connectionServer(true);

    continueWithError = continueWithError || defaultErrorHandler;
    xhr.open(method || 'GET', url, true);

    xhr.onload = function () {
        if (xhr.readyState !== 4)
            return;

        if (xhr.status != 200) {
           // continueWithError('Error on the server side, response ' + xhr.status);
            continueWithError(xhr.status);
            return;
        }

        if (isError(xhr.responseText)) {
           // continueWithError('Error on the server side, response ' + xhr.responseText);
            continueWithError( xhr.responseText);
            return;
        }

        continueWith(xhr.responseText);
    };

    xhr.ontimeout = function () {
        ontinueWithError('Server timed out !');
    }

    xhr.onerror = function (e) {
        connectionServer(false);
        var errMsg = 'Server connection error !\n' +
            '\n' +
            'Check if \n' +
            '- server is active\n' +
            '- server sends header "Access-Control-Allow-Origin:*"';
        continueWithError(errMsg);
    };

    xhr.send(data);
}

function connectionServer(flag) {
    var label = document.getElementById('connect');
    if (flag == true) {
        label.classList.add('label-success');
        label.textContent = "Connected";
    }
    else {
        label.classList.remove('label-success');
        label.classList.add('label-danger');
        label.textContent = "Disconnected";
    }
}