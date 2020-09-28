$(function () {
    $(".btnFilter").click(
        function () {
            $("#btnFilterModal").modal("show");
        });

    $("#btnFilterYes").click(
        function () {
            $('#btnFilterModalForm').submit();
        });
});