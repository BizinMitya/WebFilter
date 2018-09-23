$(document).ready(function () {
    checkStatus();
    setInterval("checkStatus()", 1000);
});

$(function () {
    $('[data-toggle="tooltip"]').tooltip();//инициализация тултипов
});

function checkStatus() {
    $.ajax({
        url: "/proxy/status",
        cache: false,
        success: function (status) {
            if (status === "true") {
                $("#status").removeClass("text-danger").addClass("text-success").html("Прокси-сервер включен");
            } else {
                $("#status").removeClass("text-success").addClass("text-danger").html("Прокси-сервер выключен");
            }
            removeReadFromServerErrorAlert();
        },
        error: function () {
            addReadFromServerErrorAlert();
            $("#status").removeClass("text-success").addClass("text-danger").html("Прокси-сервер выключен");
        }
    });
}

function addReadFromServerErrorAlert() {
    $("#readFromServerErrorAlert").html('<div class="alert alert-danger alert-dismissible fade show mb-0" role="alert">' +
        '<strong>Ошибка при получении данных от сервера!</strong>' +
        '<button type="button" class="close" data-dismiss="alert" aria-label="Close">' +
        '<span aria-hidden="true">&times;</span>' +
        '</button>' +
        '</div>');
}

function removeReadFromServerErrorAlert() {
    $("#readFromServerErrorAlert").html("");
}
