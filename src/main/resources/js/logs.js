$(document).ready(function () {
    getCurrentLogs();
    setInterval("getCurrentLogs()", 1000);
});

function getCurrentLogs() {
    $.ajax({
        url: "/logs",
        cache: false,
        success: function (logs) {
            $("#log").html(prepareLogs(logs));
            removeReadFromServerErrorAlert();
        },
        error: function () {
            addReadFromServerErrorAlert();
        }
    });
}

function prepareLogs(logs) {
    return logs
        .replace(/(?:\r\n|\r|\n)/g, "<br>")
        .replace(/\[INFO]/ig, "<code class='text-info'>[INFO]</code>")
        .replace(/\[WARN]/ig, "<code class='text-warning'>[WARN]</code>")
        .replace(/\[ERROR]/ig, "<code class='text-danger'>[ERROR]</code>");
}