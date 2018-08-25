function processPositionResult(data) {
    if (data.errcode == 0) {
        return true;
    }
    else {
        if (data.errcode == 16) {
            showFormGroupErrMsg("longitudeAndLatitude", data.errmsg);
        }
        else {
            alert(data.errmsg);
        }

        return false;
    }
}

function validatePositionInput() {
    clearFormGroupErrMsg("#editPositionForm");

    var name = $("#editPositionForm [name='name']").val();

    if (name.length === 0 || !name.trim()) {
        showFormGroupErrMsg("name", "店面全称不能为空!");
        return false;
    }

    return true;
}

//添加编辑店面
function createOrEditPosition(event) {
    return showPostFormModal(event, 'editPositionForm', null, true, validatePositionInput, processPositionResult);
}