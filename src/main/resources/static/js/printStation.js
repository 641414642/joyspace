//添加编辑自助机
function createOrEditPrintStation(event) {
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

    return true;
}

//激活自助机
function activatePrintStation(event) {
    return showPostFormModal(event, 'activatePrintStationForm', null, true, validateActivationInput, processActivationResult);
}

//显示二维码
function showPrintStationQrCode(event) {
    var source = event.target || event.srcElement;
    var qrCode = $(source).data('qrcode');

    var content =
         '<div class="modal-content">' +
             '<div class="modal-header">' +
                 '<button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>' +
                 '<h4 class="modal-title">自助机二维码</h4>' +
             '</div>' +
             '<div class="modal-body">' +
                  '<div id="qrCodeDiv"></div>' +
             '</div>' +
             '<div class="modal-footer">' +
                  '<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>' +
             '</div>' +
         '</div>';
    $('#modalTemplate .modal-dialog').empty().append(content)

    new QRCode(document.getElementById("qrCodeDiv"), qrCode);

    $('#modalTemplate').modal('show');

    return false;
}
