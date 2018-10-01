$(document).ready(function () {
    loadAllHosts();
});

function loadAllHosts() {
    $.ajax({
        url: "/proxy/blacklist",
        cache: false,
        success: function (blacklist) {
            blacklist = JSON.parse(blacklist);
            const list = $("#blacklist");
            list.empty();
            for (let i = 0; i < blacklist.length; i++) {
                list.append('<tr id=' + (i + 1) + '>' +
                    '<th scope="row">' + (i + 1) + '</th>' +
                    '<td>' + blacklist[i].ip + '</td>' +
                    '<td>' + blacklist[i].hostname + '</td>' +
                    '<td>' +
                    '<button type="button" class="close" aria-label="Close" onclick="removeHost(this.id,' + (i + 1) + ')" id="' + blacklist[i].ip + '">' +
                    '<span aria-hidden="true">&times;</span>' +
                    '</button>' +
                    '</td>' +
                    '</tr>');
            }
        },
        error: function () {
            addReadFromServerErrorAlert();
        }
    });
}

function removeHostFromTable(id) {
    $("#" + id).remove();
}

function removeHost(ip, rowNumber) {
    $.ajax({
        url: "/proxy/blacklist",
        method: "DELETE",
        contentType: "text/plain; charset=UTF-8",
        data: {
            ip: ip
        },
        success: function () {
            removeHostFromTable(rowNumber);
            removeSaveHostErrorAlert();
            addSaveHostSuccessAlert("Хост успешно удалён из чёрного списка!");
        },
        statusCode: {
            400: function () {
                removeSaveHostSuccessAlert();
                addSaveHostErrorAlert("Некорректное имя хоста или IP адреса!");
            },
            500: function () {
                removeSaveHostSuccessAlert();
                addSaveHostErrorAlert("Ошибка при удалении хоста или IP адреса!");
            }
        }
    });
}

function addHostToBlacklist() {
    const validIpAddressRegex = /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$/;
    const validHostnameRegex = /^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$/;
    const host = $("#hostNameInput").val();
    if (validIpAddressRegex.test(host) || validHostnameRegex.test(host)) {
        removeSaveHostWarningAlert();
        $("#hostNameInput").val("");
        $.ajax({
            url: "/proxy/blacklist",
            method: "POST",
            contentType: "text/plain; charset=UTF-8",
            data: {
                host: host
            },
            success: function () {
                removeSaveHostErrorAlert();
                addSaveHostSuccessAlert("Хост успешно добавлен в чёрный список!");
                loadAllHosts();
            },
            statusCode: {
                400: function () {
                    removeSaveHostSuccessAlert();
                    addSaveHostErrorAlert("Некорректное имя хоста или IP адреса!");
                },
                500: function () {
                    removeSaveHostSuccessAlert();
                    addSaveHostErrorAlert("Ошибка при добавлении хоста или IP адреса!");
                }
            }
        });
    } else {
        addSaveHostWarningAlert("Хост или IP адрес не корректен!");
    }
}

function addSaveHostErrorAlert(message) {
    $("#saveHostErrorAlert").html('<div class="alert alert-danger alert-dismissible fade show mb-0" role="alert">' +
        '<strong>' + message + '</strong>' +
        '<button type="button" class="close" data-dismiss="alert" aria-label="Close">' +
        '<span aria-hidden="true">&times;</span>' +
        '</button>' +
        '</div>');
}

function removeSaveHostErrorAlert() {
    $("#saveHostErrorAlert").html("");
}

function addSaveHostWarningAlert(message) {
    $("#saveHostWarnAlert").html('<div class="alert alert-warning alert-dismissible fade show mb-0" role="alert">' +
        '<strong>' + message + '</strong>' +
        '<button type="button" class="close" data-dismiss="alert" aria-label="Close">' +
        '<span aria-hidden="true">&times;</span>' +
        '</button>' +
        '</div>');
}

function removeSaveHostWarningAlert() {
    $("#saveHostWarnAlert").html("");
}

function addSaveHostSuccessAlert(message) {
    $("#saveHostSuccessAlert").html('<div class="alert alert-success alert-dismissible fade show mb-0" role="alert">' +
        '<strong>' + message + '</strong>' +
        '<button type="button" class="close" data-dismiss="alert" aria-label="Close">' +
        '<span aria-hidden="true">&times;</span>' +
        '</button>' +
        '</div>');
}

function removeSaveHostSuccessAlert() {
    $("#saveHostSuccessAlert").html("");
}