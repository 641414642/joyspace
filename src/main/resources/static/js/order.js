//订单详情
function viewPrintOrderDetail(event) {
    return showPostFormModal(event, 'printOrderDetail', 'modal-info', true);
}

function refreshPrintOrderList(url ,pageNo) {
    if (document.getElementById("autoRefresh").checked) {
        $.ajax({
            type: "GET",
            url: url,
            data: {
                pageno: pageNo,
                partial: true,
                orderNo: ''
            },
            success: function (data) {
                var parent = $("#printOrderListTable").parent()
                $("#printOrderListTable").remove()
                $("#printOrderListTablePager").remove()

                parent.append(data)
            }
        });
    }

    setTimeout(function() {
        refreshPrintOrderList(url, pageNo);
    }, 5000)
}

function startRefresh(url, pageNo) {
    setTimeout(function() {
        refreshPrintOrderList(url, pageNo);
    }, 5000)
}