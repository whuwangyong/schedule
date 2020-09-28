$(function () {


    //start bench
    $(".btnStart").click(function () {
        $(this).attr("disabled", "true");
        var benchId = $(this).parent().data("id");
        $.ajax({
            url: "/api/benchmark/start",
            type: "POST",
            data: {
                "benchId": $("#benchId_" + benchId).text()
            },
            dataType: "JSON",
            success: function (ret) {
                if (ret.valid) {
                    alert("start success!");
                } else {
                    alert(ret.msg);
                }
                location.reload();
            }
        });
    });

    //stop bench
    $(".btnStop").click(function () {
        $(this).attr("disabled", "true");
        var benchId = $(this).parent().data("id");
        $.ajax({
            url: "/api/benchmark/stop",
            type: "POST",
            data: {
                "benchId": $("#benchId_" + benchId).text()
            },
            dataType: "JSON",
            success: function (ret) {
                if (ret.valid) {
                    alert("stop success!");
                } else {
                    alert(ret.msg);
                }
                location.reload();
            }
        });
    });


    $(".btnDelete").click(function () {
        var benchId = $(this).parent().data("id");
        $.ajax({
            url: "/api/benchmark/delete",
            type: "POST",
            data: {
                "benchId": $("#benchId_" + benchId).text()
            },
            dataType: "JSON",
            success: function (ret) {
                if (ret.valid) {
                    // alert("delete success!");
                    location.reload();
                } else {
                    alert(ret.msg);
                }
            }
        });
    });


    $(".btnCreate").click(
        function () {
            $("#createModalLabel").html("新建 Benchmark Job");
            $("#createModal").modal("show");
        });

    $("#saveBench").click(
        function () {
            $('#createBenchForm').submit();
        });


    // ------------ common -----------
    $(".btnChangeMode").click(
        function () {
            $("#changeModeModalLabel").html("设置运行模式");
            $("#setRunMode").val($("#currentMode").text());
            $("#changeModeModal").modal("show");
        });
    $("#saveMode").click(
        function () {
            $('#changeModeForm').submit();
        });
    // ------------ common -----------


});