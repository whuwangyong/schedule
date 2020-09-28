$(function () {

    /*
        // start pipeline
        // 点击开始按钮，弹框选择job
        $(".btnStart").click(
            function () {
                $("#btnStartModal").modal("show");
            });
        $(document).ready(function () {
            $('#btnStartModalForm').on('submit', function (e) {
                var sendData = $(this).serializeArray();
                var formUrl = $(this).attr("action");
                // e.preventDefault();
                $.ajax({
                    url: formUrl,
                    type: "GET",
                    data: sendData,
                    success: function (ret) {
                        if (ret.valid) {
                            alert("start success!");
                        } else {
                            alert(ret.msg);
                        }
                        // location.reload();
                    }
                });
                e.preventDefault();
            });

            $("#btnStartYes").on("click", function () {
                $('#btnStartModalForm').submit();
            });
        });
        */


    /*
    // 这种写法，不能弹框显示后端的返回值
    $(".btnStart").click(
        function () {
            $("#btnStartModal").modal("show");
        });

    $("#btnStartYes").click(
        function () {
            $('#btnStartModalForm').submit();
        });
      */



    //点开始后直接运行。不传参数（不涉及提交表单）
    $(".btnStart").click(function () {
        $(this).attr("disabled", "true");
        $.ajax({
            url: "/api/pipeline/start",
            type: "Get",
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


    //stop pipeline
    $(".btnStop").click(function () {
        $(this).attr("disabled", "true");
        $.ajax({
            url: "/api/pipeline/stop",
            type: "Get",
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

    //断点续作
    $(".btnResume").click(function () {
        $(this).attr("disabled", "true");
        $.ajax({
            url: "/api/pipeline/resume",
            type: "Get",
            success: function (ret) {
                if (ret.valid) {
                    alert("resume success!");
                } else {
                    alert(ret.msg);
                }
                location.reload();
            }
        });
    });


    // ------------ common -----------
    $(".btnChangeMode").click(
        function () {
            $("#setRunMode").val($("#currentMode").text());
            $("#changeModeModal").modal("show");
        });

    $("#btnChangeModeYes").click(
        function () {
            $('#changeModeForm').submit();
        });
    // ------------ common -----------


    $(".btnSetRounds").click(
        function () {
            $("#setRoundsModal").modal("show");
        });

    $("#btnSetRoundsYes").click(
        function () {
            $('#setRoundsForm').submit();
        });
});