$(function () {


    //start job
    $(".btnStart").click(function () {
        $(this).attr("disabled", "true");
        var jobId = $(this).parent().data("id");
        $.ajax({
            url: "/api/step/startJob",
            type: "POST",
            data: {
                "jobId": $("#jobId_" + jobId).text()
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

    //stop job
    $(".btnStop").click(function () {
        $(this).attr("disabled", "true");
        var jobId = $(this).parent().data("id");
        $.ajax({
            url: "/api/step/stopJob",
            type: "POST",
            data: {
                "jobId": $("#jobId_" + jobId).text()
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


    $(".btnChangeState").click(
        function () {
            $("#changeStateModalLabel").html("修改作业状态");
            var jobId = $(this).parent().data("id");
            $("#jobId").val(jobId);
            //带入当前的值
            $("#edit_state").val($("#state_" + jobId).text());
            $("#changeStateModal").modal("show");
        });

    $("#saveState").click(
        function () {
            $('#changeStateForm').submit();
        });


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


});