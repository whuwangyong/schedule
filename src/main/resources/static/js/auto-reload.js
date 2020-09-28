function autoReload() {
    // alert("test");

    var optAction = false;
    var autoTime;
    var body = document.getElementsByTagName("body")[0];

    body.onkeydown = body.onmousemove = body.onclick = function () {
        optAction = true;
        clearTimeout(autoTime);
        autoTime = setTimeout(function () {
            optAction = false;
        }, 5000)
    };

    setTimeout(function () {
        setInterval(function () {
            if (!optAction) {
                // console.log("刷新页面");
                window.location.reload();
            }
        }, 1000)
    }, 5000)
}
