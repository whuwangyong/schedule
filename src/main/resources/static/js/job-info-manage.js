$(function () {

    //delete job
    $(".btnDelete").click(function () {
        var jobId = $(this).parent().data("id");
        $.ajax({
            url: "/api/job-info-manage/delete",
            type: "POST",
            data: {
                "jobId": $("#jobId_" + jobId).text()
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


    //------- 新建 job -----------
    $(".btnCreate").click(
        function () {
            $("#createModalLabel").html("新建 job");
            $("#createModal").modal("show");
        });
    $("#submitCreate").click(
        function () {
            // $('#createForm').submit();
            $.ajax({
                url: "/api/job-info-manage/add",
                type: "POST",
                dataType: "JSON", // 服务器返回的数据类型
                data: $('#createForm').serialize(),
                success: function (ret) {
                    if (ret.valid) {
                        // alert("add success!");
                        location.reload()
                    } else {
                        alert(ret.msg);
                    }
                }
            });
        });
    //------- 新建 job -----------


    //------- 修改 job 信息 -----------
    // modal 带入信息并展示
    $(".btnEdit").click(
        function () {
            $("#updateModalLabel").html("修改 job 信息");
            var jobId = $(this).parent().data("id");
            $("#edit_jobId").val(jobId);
            $("#edit_jobName").val($("#jobName_" + jobId).text());
            $("#edit_preJob").val($("#preJob_" + jobId).text());
            $("#edit_nextJob").val($("#nextJob_" + jobId).text());
            $("#edit_startCmd").val($("#startCmd_" + jobId).text());
            $("#edit_checkCmd").val($("#checkCmd_" + jobId).text());
            $("#edit_stopCmd").val($("#stopCmd_" + jobId).text());
            // $("#edit_initCmd").val($("#initCmd_" + jobId).text());
            $("#updateModal").modal("show");
        });
    // 提交修改
    $("#submitUpdate").click(
        function () {
            // $('#updateForm').submit();
            $.ajax({
                url: "/api/job-info-manage/update",
                type: "POST",
                dataType: "JSON", // 服务器返回的数据类型
                data: $('#updateForm').serialize(),
                success: function (ret) {
                    if (ret.valid) {
                        // alert("update success!");
                        location.reload();
                    } else {
                        alert(ret.msg);
                    }
                }
            });
        });
    //------- 修改 job 信息 -----------
});