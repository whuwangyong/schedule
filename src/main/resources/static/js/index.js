$(function() {


	//start job
    $(".btnStart").click(function() {
    	var jobId = $(this).parent().data("id");
        $.ajax({
            url: "/api/startJob",
            type: "POST",
            data: {
                "jobId": $("#jobId_"+jobId).text(),
                "name": $("#name_"+jobId).text()
            },
            dataType:"JSON",
            success: function(ret) {
                if (ret.valid) {
                	alert("start success!");
                    location.reload();
                } else {
                	alert(ret.msg); 
                }
            }
        });
    });

    //stop job
    $(".btnStop").click(function() {
    	var jobId = $(this).parent().data("id");
        $.ajax({
            url: "/api/stopJob",
            type: "POST",
            data: {
                "jobId": $("#jobId_"+jobId).text(),
                "name": $("#name_"+jobId).text()
            },
            dataType:"JSON",
            success: function(ret) {
                if (ret.valid) {
                	alert("stop success!");
                	location.reload();
                } else {
                	alert(ret.msg);
                }
            }
        });
    });


    $(".btnEdit").click(
        function () {
            $("#quartzModalLabel").html("修改作业");
            var jobId = $(this).parent().data("id");
            $("#jobId").val(jobId);
            //带入当前的值
            $("#edit_name").val($("#name_" + jobId).text());
            $("#edit_state").val($("#state_" + jobId).text());
            $("#edit_startCmd").val($("#startCmd_" + jobId).text());
            $("#edit_checkCmd").val($("#checkCmd_" + jobId).text());
            $("#edit_stopCmd").val($("#stopCmd_" + jobId).text());


            $("#quartzModal").modal("show");
        });

    $("#save").click(
        function () {
            $('#mainForm').submit();
            location.reload(); // not ok
            /*$.ajax({
                url: "/api/saveOrUpdate?t=" + new Date().getTime(),
                type: "POST",
                dataType:"JSON",
                data:  $('#mainForm').serialize(),
                success: function(ret) {
                    if (ret.valid) {
                        alert("save success!");
                        location.reload();
                    } else {
                        alert(ret.msg);
                    }
                }
            });*/
        });

});