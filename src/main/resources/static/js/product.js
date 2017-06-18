//添加投放地点
function createOrEditPosition(event) {
    return showPostFormModal(event, 'editPositionForm', null, true);
}

//添加编辑产品
function createOrEditProduct(event) {
    return showPostFormModal(event, 'editProductForm', null, true);
}

//上传图片文件
function uploadProductImageFiles(event) {
    $('#modalTemplate').removeClass().addClass("modal fade");
    showModal(event);
    return false;
}

//查看价目表
function viewPriceList(event) {
    return showPostFormModal(event, 'editPriceListForm', 'modal-info', true);
}

//添加编辑价目表
function createOrEditPriceList(event) {
    return showPostFormModal(event, 'editPriceListForm', null, true);
}
