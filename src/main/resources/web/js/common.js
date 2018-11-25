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
            status = JSON.parse(status);
            let httpStatus = status[0];
            let httpsStatus = status[1];
            if (httpStatus === true) {
                $("#httpStatus").removeClass("text-danger").addClass("text-success").html("HTTP");
            } else {
                $("#httpStatus").removeClass("text-success").addClass("text-danger").html("HTTP");
            }
            if (httpsStatus === true) {
                $("#httpsStatus").removeClass("text-danger").addClass("text-success").html("HTTPS");
            } else {
                $("#httpsStatus").removeClass("text-success").addClass("text-danger").html("HTTPS");
            }
            removeReadFromServerErrorAlert();
        },
        error: function () {
            addReadFromServerErrorAlert();
            $("#httpStatus").removeClass("text-success").addClass("text-danger").html("HTTP");
            $("#httpsStatus").removeClass("text-success").addClass("text-danger").html("HTTPS");
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
