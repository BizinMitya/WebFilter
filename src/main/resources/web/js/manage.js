$(document).ready(function () {
    checkProxyServer();
    setInterval("checkProxyServer()", 2000);
});

function checkProxyServer() {
    $.ajax({
        url: "/proxy/status",
        cache: false,
        success: function (status) {
            status = JSON.parse(status);
            let httpStatus = status[0];
            let httpsStatus = status[1];
            $("#switchHttpProxyServer").prop('checked', httpStatus === true);
            $("#switchHttpProxyServerLabel").html(httpStatus === true ? "HTTP прокси-сервер включен" : "HTTP прокси-сервер выключен");
            $("#switchHttpsProxyServer").prop('checked', httpsStatus === true);
            $("#switchHttpsProxyServerLabel").html(httpsStatus === true ? "HTTPS прокси-сервер включен" : "HTTPS прокси-сервер выключен");
        },
        error: function () {
            $("#switchHttpProxyServer").prop('checked', false);
            $("#switchHttpProxyServerLabel").html("HTTP прокси-сервер выключен");
            $("#switchHttpsProxyServer").prop('checked', false);
            $("#switchHttpsProxyServerLabel").html("HTTPS прокси-сервер выключен");
        }
    });
}

function switchHttpProxyServer() {
    const enableHttpProxyServer = $("#switchHttpProxyServer").is(':checked');
    if (enableHttpProxyServer) {
        $.ajax({
            url: "/proxy/startHttp",
            cache: false,
            success: function () {
                removeManageProxyErrorAlert();
                addManageProxyInfoAlert("HTTP прокси-сервер запущен!");
                $("#switchHttpProxyServerLabel").html("HTTP прокси-сервер включен");
            },
            error: function () {
                removeManageProxyInfoAlert();
                addManageProxyErrorAlert("Произошла ошибка при запуске HTTP прокси-сервера!");
                $("#switchHttpProxyServerLabel").html("HTTP прокси-сервер выключен");
            }
        });
    } else {
        $.ajax({
            url: "/proxy/stopHttp",
            cache: false,
            success: function () {
                removeManageProxyErrorAlert();
                addManageProxyInfoAlert("HTTP прокси-сервер остановлен!");
                $("#switchHttpProxyServerLabel").html("HTTP прокси-сервер выключен");
            },
            error: function () {
                removeManageProxyInfoAlert();
                addManageProxyErrorAlert("Произошла ошибка при остановке HTTP прокси-сервера!");
                $("#switchHttpProxyServerLabel").html("HTTP прокси-сервер выключен");
            }
        });
    }
}

function switchHttpsProxyServer() {
    const enableHttpsProxyServer = $("#switchHttpsProxyServer").is(':checked');
    if (enableHttpsProxyServer) {
        $.ajax({
            url: "/proxy/startHttps",
            cache: false,
            success: function () {
                removeManageProxyErrorAlert();
                addManageProxyInfoAlert("HTTPS прокси-сервер запущен!");
                $("#switchHttpsProxyServerLabel").html("HTTPS прокси-сервер включен");
            },
            error: function () {
                removeManageProxyInfoAlert();
                addManageProxyErrorAlert("Произошла ошибка при запуске HTTPS прокси-сервера!");
                $("#switchHttpsProxyServerLabel").html("HTTPS прокси-сервер выключен");
            }
        });
    } else {
        $.ajax({
            url: "/proxy/stopHttps",
            cache: false,
            success: function () {
                removeManageProxyErrorAlert();
                addManageProxyInfoAlert("HTTPS прокси-сервер остановлен!");
                $("#switchHttpsProxyServerLabel").html("HTTPS прокси-сервер выключен");
            },
            error: function () {
                removeManageProxyInfoAlert();
                addManageProxyErrorAlert("Произошла ошибка при остановке HTTPS прокси-сервера!");
                $("#switchHttpsProxyServerLabel").html("HTTPS прокси-сервер выключен");
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