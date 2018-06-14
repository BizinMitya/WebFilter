const MIN_PROXY_PORT = 1024;
const MIN_THREADS_COUNT = 1;
const MIN_TIMEOUT_FOR_CLIENT = 1000;
const MIN_TIMEOUT_FOR_SERVER = 1000;
const MAX_PROXY_PORT = 65535;
const MAX_THREADS_COUNT = 10;
const MAX_TIMEOUT_FOR_CLIENT = 60000;
const MAX_TIMEOUT_FOR_SERVER = 60000;

$(document).ready(function () {
    loadSettings();
});

function loadSettings() {
    $.ajax({
        url: "/proxy/settings",
        cache: false,
        dataType: "json",
        success: function (settings) {
            setSettings(settings);
            removeReadFromServerErrorAlert();
        },
        error: function () {
            addReadFromServerErrorAlert();
        }
    });
}

function saveSettings() {
    $.ajax({
        url: "/proxy/settings",
        method: "POST",
        contentType: "application/json; charset=UTF-8",
        data: getJSONSettings(),
        success: function () {
            removeSaveSettingsErrorAlert();
            addSaveSettingsSuccessAlert();
        },
        error: function () {
            removeSaveSettingsSuccessAlert();
            addSaveSettingsErrorAlert();
        }
    });
    loadSettings();
}

function getJSONSettings() {
    const proxyPort = $("#proxyPortInput").val();
    const threadsCount = $("#threadsCountInput").val();
    const timeoutForClient = $("#timeoutForClientInput").val();
    const timeoutForServer = $("#timeoutForServerInput").val();
    if (!$.isNumeric(proxyPort) || !$.isNumeric(threadsCount) ||
        !$.isNumeric(timeoutForClient) || !$.isNumeric(timeoutForServer) ||
        proxyPort < MIN_PROXY_PORT || proxyPort > MAX_PROXY_PORT ||
        threadsCount < MIN_THREADS_COUNT || threadsCount > MAX_THREADS_COUNT ||
        timeoutForClient < MIN_TIMEOUT_FOR_CLIENT || timeoutForClient > MAX_TIMEOUT_FOR_CLIENT ||
        timeoutForServer < MIN_TIMEOUT_FOR_SERVER || timeoutForServer > MAX_TIMEOUT_FOR_SERVER) {
        addSaveSettingsWarningAlert();
    }
    return JSON.stringify({
        proxyPort: proxyPort,
        threadsCount: threadsCount,
        timeoutForClient: timeoutForClient,
        timeoutForServer: timeoutForServer
    })
}

function setSettings(settings) {
    $("#proxyPortInput").val(settings.proxyPort);
    $("#threadsCountInput").val(settings.threadsCount);
    $("#timeoutForClientInput").val(settings.timeoutForClient);
    $("#timeoutForServerInput").val(settings.timeoutForServer);
}


function addSaveSettingsErrorAlert() {
    $("#saveSettingsErrorAlert").html('<div class="alert alert-danger alert-dismissible fade show mb-0" role="alert">' +
        '<strong>Ошибка сохранения настроек на сервере!</strong>' +
        '<button type="button" class="close" data-dismiss="alert" aria-label="Close">' +
        '<span aria-hidden="true">&times;</span>' +
        '</button>' +
        '</div>');
}

function removeSaveSettingsErrorAlert() {
    $("#saveSettingsErrorAlert").html("");
}

function addSaveSettingsSuccessAlert() {
    $("#saveSettingsSuccessAlert").html('<div class="alert alert-success alert-dismissible fade show mb-0" role="alert">' +
        '<strong>Настройки успешно сохранены и применены!</strong>' +
        '<button type="button" class="close" data-dismiss="alert" aria-label="Close">' +
        '<span aria-hidden="true">&times;</span>' +
        '</button>' +
        '</div>');
}

function removeSaveSettingsSuccessAlert() {
    $("#saveSettingsSuccessAlert").html("");
}

function addSaveSettingsWarningAlert() {
    $("#saveSettingsWarningAlert").html('<div class="alert alert-warning alert-dismissible fade show mb-0" role="alert">' +
        '<strong>Некоторые поля заполнены некорректно! Будут сохранены значения по умолчанию!</strong>' +
        '<button type="button" class="close" data-dismiss="alert" aria-label="Close">' +
        '<span aria-hidden="true">&times;</span>' +
        '</button>' +
        '</div>');
}

function removeSaveSettingsWarningAlert() {
    $("#saveSettingsWarningAlert").html("");
}

