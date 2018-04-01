//编辑自助机
function editPrintStation(event) {
    return showPostFormModal(event, 'editPrintStationForm', null, true);
}

function processActivationResult(data) {
    if (data.errcode == 0) {
        return true;
    }
    else {
        if (data.errcode == 14 || data.errcode == 15) {
            showFormGroupErrMsg("code", data.errmsg);
        }
        else {
            alert(data.errmsg);
        }

        return false;
    }
}

function validateActivationInput() {
    clearFormGroupErrMsg("#activatePrintStationForm");

    var code = $("#activatePrintStationForm [name='code']").val();
    var password = $("#activatePrintStationForm [name='printStationPassword']").val();

    if (code.length === 0 || !code.trim()) {
        showFormGroupErrMsg("code", "激活码不能为空!");
        return false;
    }
    else if (password.length === 0 || !password.trim()) {
        showFormGroupErrMsg("printStationPassword", "自助机密码不能为空!");
        return false;
    }
    else if ($("#positionId option:selected").length == 0) {
        showFormGroupErrMsg("positionId", "请选择店面!");
        return false;
    }

    return true;
}

function updatePositionSelectOptions() {
    var companyId = $("#companyId option:selected").val();
    if (companyId) {
        $("#positionId option").appendTo($("#positionCache"));
        $("#positionCache option[data-company-id=" + companyId + "]").appendTo($("#positionId"));
    }
}

//激活自助机
function activatePrintStation(event) {
    return showPostFormModal(event, 'activatePrintStationForm', null, true,
        validateActivationInput, processActivationResult, function() {
            $("#companyId").on("change", function() {
                updatePositionSelectOptions();
            });

            updatePositionSelectOptions();
        });
}

//上传日志文件
function confirmUploadLogFile(event) {
    return showPostFormModal(event, 'uploadLogFileForm', null, false, null, null, function() {
        $("#logFileDate")
            .datepicker({ autoclose: true, format: 'yyyy-mm-dd' })
            .datepicker("setDate", new Date());
    });
}