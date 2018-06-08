$(document).ready(function () {
    checkProxyServer();
    setInterval("checkProxyServer()", 1000);
});

function checkProxyServer() {
    $.ajax({
        url: "/proxy/status",
        cache: false,
        async: false,
        success: function (status) {
            if (status === "true") {
                $("#switchProxyServer").attr('checked', "true");
            } else {
                $("#switchProxyServer").removeAttr('checked');
            }
            $("#switchProxyServerLabel").html(status === "true" ? "Прокси-сервер включен" : "Прокси-сервер выключен");
        }
    });
}

function switchProxyServer() {
    const enableProxyServer = $("#switchProxyServer").is(':checked');
    if (enableProxyServer) {
        $.ajax({
            url: "/proxy/start",
            cache: false,
            success: function () {
                removeManageProxyErrorAlert();
                addManageProxyInfoAlert("Прокси-сервер запущен!");
                $("#switchProxyServerLabel").html("Прокси-сервер включен");
            },
            error: function () {
                removeManageProxyInfoAlert();
                addManageProxyErrorAlert("Произошла ошибка при запуске прокси-сервера!");
                $("#switchProxyServerLabel").html("Прокси-сервер выключен");
            }
        });
    } else {
        $.ajax({
            url: "/proxy/stop",
            cache: false,
            success: function () {
                removeManageProxyErrorAlert();
                addManageProxyInfoAlert("Прокси-сервер остановлен!");
                $("#switchProxyServerLabel").html("Прокси-сервер выключен");
            },
            error: function () {
                removeManageProxyInfoAlert();
                addManageProxyErrorAlert("Произошла ошибка при остановке прокси-сервера!");
                $("#switchProxyServerLabel").html("Прокси-сервер выключен");
            }
        });
    }
}

function addManageProxyInfoAlert(text) {
    $("#manageProxyInfoAlert").html('<div class="alert alert-info alert-dismissible fade show mb-0" role="alert">' +
        '<strong>' + text + '</strong>' +
        '<button type="button" class="close" data-dismiss="alert" aria-label="Close">' +
        '<span aria-hidden="true">&times;</span>' +
        '</button>' +
        '</div>');
}

function removeManageProxyInfoAlert() {
    $("#manageProxyInfoAlert").html("");
}

function addManageProxyErrorAlert(text) {
    $("#manageProxyErrorAlert").html('<div class="alert alert-danger alert-dismissible fade show mb-0" role="alert">' +
        '<strong>' + text + '</strong>' +
        '<button type="button" class="close" data-dismiss="alert" aria-label="Close">' +
        '<span aria-hidden="true">&times;</span>' +
        '</button>' +
        '</div>');
}

function removeManageProxyErrorAlert() {
    $("#manageProxyErrorAlert").html("");
}