//订单详情
function viewPrintOrderDetail(event) {
    return showPostFormModal(event, 'printOrderDetail', 'modal-info', true);
}
//订单转账详情
function viewPrintOrderTransferDetail(event) {
    return showModal(event);
}

function refreshPrintOrderList(url ,pageNo) {
    if (document.getElementById("autoRefresh").checked) {
        $.ajax({
            type: "GET",
            url: url,
            data: {
                pageno: pageNo,
                partial: true,
                inputPositionId: $("#inputPositionId").val(),
                inputPrintStationId: $("#inputPrintStationId").val(),
                inputTimeRange: $("#inputTimeRange").val(),
                inputStartTime: $("#startTime").val(),
                inputEndTime: $("#endTime").val()
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

function getDate(numberOfDaysToAdd) {
    var d = new Date();
    d.setDate(d.getDate() + numberOfDaysToAdd);
    return d;
}

function updateTimeRange() {
    $("#startTime").datepicker("disable");
    $("#endTime").datepicker("disable");

    var selTimeRange = $("#inputTimeRange option:selected").val();

    var now = new Date();

    if (selTimeRange == "1") { //当天
        $("#startTime").datepicker("setDate", now);
        $("#endTime").datepicker("setDate", now);
    }
    else if (selTimeRange == "2") { //近三天
        $("#startTime").datepicker("setDate", getDate(-2));
        $("#endTime").datepicker("setDate", now);
    }
    else if (selTimeRange == "3") {  //本周
        if (now.getDay() > 0) {
            $("#startTime").datepicker("setDate", getDate(-now.getDay() + 1));
            $("#endTime").datepicker("setDate", getDate(-now.getDay() + 7));
        }
        else {
            $("#startTime").datepicker("setDate", getDate(-6));
            $("#endTime").datepicker("setDate", now);
        }
    }
    else if (selTimeRange == "4") {  //本月
        $("#startTime").datepicker("setDate", new Date(now.getFullYear(), now.getMonth(), 1));
        $("#endTime").datepicker("setDate", new Date(now.getFullYear(), now.getMonth() + 1, 0));
    }
    else {  //自定义
        $("#startTime").datepicker("enable");
        $("#endTime").datepicker("enable");
    }
}

$(function() {
    $("#autoRefresh").change(function() {
        localStorage.setItem("autoRefreshOrderList", this.checked ? "true" : "false");
    });

    var autoRefresh = localStorage.getItem("autoRefreshOrderList");
    $("#autoRefresh").prop('checked', autoRefresh != "false");

    $("#inputPositionId").on("change", function() {
        var selPrintStationId = $("#inputPrintStationId option:selected").val();

        $("#inputPrintStationId option").remove();
        $("#inputPrintStationId").append('<option value="0">&lt;所有自助机&gt;</option>');

        var selPositionId = $("#inputPositionId option:selected").val();
        if (selPositionId == 0) {
            $("#printStationCache option").clone().appendTo($("#inputPrintStationId"));
        }
        else {
            $("#printStationCache option[data-position-id=" + selPositionId + "]").clone().appendTo($("#inputPrintStationId"));
        }

        $("#inputPrintStationId").val(selPrintStationId);
        if ($("#inputPrintStationId").val() === null) {
            $("#inputPrintStationId").val("0");
        }
    });

    $("#startTime").datepicker({ autoclose: true, format: 'yyyy-mm-dd' });
    $("#endTime").datepicker({ autoclose: true, format: 'yyyy-mm-dd' });

    $("#inputTimeRange").change(updateTimeRange);
})

function reprintOrder(event) {
    return showPostFormModal(event, 'reprintOrderForm', null, false, null, function(data) {
        if (data.errcode == 0) {
            return true;
        }
        else {
            alert(data.errmsg);
            return false;
        }
    });
}