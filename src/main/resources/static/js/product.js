//添加编辑自助机
function createOrEditPrintStation(event) {
    return showPostFormModal(event, 'editPrintStationForm', null, true);
}

//添加编辑投放地点
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

//显示二维码
function showPrintStationQrCode(event) {
    var source = event.target || event.srcElement;
    var qrCode = $(source).data('qrcode');

    var content = '<div class="modal-content">\
             <div class="modal-header">\
                 <button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>\
                 <h4 class="modal-title">微信二维码</h4>\
             </div>\
             <div class="modal-body">\
                <div id="qrCodeDiv"></div>\
             </div>\
             <div class="modal-footer">\
                 <button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>\
             </div>\
         </div>';
    $('#modalTemplate .modal-dialog').empty().append(content)

    new QRCode(document.getElementById("qrCodeDiv"), qrCode);

    $('#modalTemplate').modal('show');

    return false;
}
