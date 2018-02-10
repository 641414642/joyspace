function isPositiveInt(str) {
    var n = Math.floor(Number(str));
    return n !== Infinity && String(n) === str && n > 0;
}

function processCreateCodeResult(data) {
    if (data.errcode == 0) {
        return true;
    }
    else {
        if (data.errcode == 17 || data.errcode == 18) {
            showFormGroupErrMsg("printStationIdStart", data.errmsg);
        }
        else {
            alert(data.errmsg);
        }

        return false;
    }
}

function validateCreateCodeInput() {
    clearFormGroupErrMsg("#createActivationCodeForm");

    var idStart = $("#createActivationCodeForm [name='printStationIdStart']").val();
    var quantity = $("#createActivationCodeForm [name='quantity']").val();

    if (idStart.length === 0 || !idStart.trim()) {
        showFormGroupErrMsg("printStationIdStart", "请输入起始ID!");
        return false;
    }
    else if (!isPositiveInt(idStart)) {
        showFormGroupErrMsg("printStationIdStart", "起始ID必须是正整数!");
        return false;
    }

    if (quantity.length === 0 || !quantity.trim()) {
        showFormGroupErrMsg("quantity", "请输入数量!");
        return false;
    }
    else if (!isPositiveInt(quantity) ) {
        showFormGroupErrMsg("quantity", "数量必须是正整数!");
        return false;
    }

    return true;
}

//添加自助机激活码
function createActivationCode(event) {
    return showPostFormModal(event, 'createActivationCodeForm', null, true, validateCreateCodeInput, processCreateCodeResult);
}

function validateEditCodeInput() {
    clearFormGroupErrMsg("#editActivationCodeForm");

    var proportion = $("#editActivationCodeForm [name='proportion']").val();

    if (proportion.length === 0 || !proportion.trim()) {
        showFormGroupErrMsg("proportion", "请输入分账比例!");
        return false;
    }
    else if (Number(proportion) <= 0) {
        showFormGroupErrMsg("proportion", "分账比例必须大于0");
        return false;
    }
    else if (Number(proportion) > 100) {
        showFormGroupErrMsg("proportion", "分账比例必须小于或等于100");
        return false;
    }

    return true;
}

//编辑自助机激活码
function editActivationCode(event) {
    return showPostFormModal(event, 'editActivationCodeForm', null, true, validateEditCodeInput);
}

//导出自助机激活码
function exportActivationCode(event) {
    return showModal(event, function() {
        $("#exportActivationCodeForm").submit(function(e) {
            $('#modalTemplate').modal('hide');
        })
    });
}