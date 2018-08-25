//添加编辑产品
function createOrEditProduct(event) {

    return showPostFormModal(event, 'editProductForm', null, true,null,null,function () {
        var type = $('#templateId').find("option:selected").attr("type");
        if (type === "0") {
            $("#refined").show()
        } else {
            $("#refined").find("input").prop( "checked", false );
            $("#refined").find("input").prop( "value", 0 );
            $("#refined").hide()
        }
        if (type === "5") {
            $("#area").show();
            $("#piece").show();
        }else{
            $("#area").hide();
            $("#piece").hide();
        }
    });
}

//上传产品图片文件
function manageProductImageFiles(event) {
    $('#modalTemplate').removeClass().addClass("modal fade");
    showModal(event, function() {
        $('#productPrevImgFileInput').change(function() {
          $('#uploadProductPrevImgFileForm').submit();
        });

        $('#productThumbImgFileInput').change(function() {
          $('#uploadProductThumbImgFileForm').submit();
        });
    });
    return false;
}

//删除产品图片
function deleteProductImage(event) {
    if (confirm("您确定要删除此图片吗?")) {
        var source = event.target || event.srcElement;
        $(source).closest('form').submit();
    }
}

//上传店面图片文件
function managePositionImageFiles(event) {
    $('#modalTemplate').removeClass().addClass("modal fade");
    showModal(event, function() {
        $('#positionImgFileInput').change(function() {
          $('#uploadPositionImgFileForm').submit();
        });
    });
    return false;
}

//删除店面图片
function deletePositionImage(event) {
    if (confirm("您确定要删除此图片吗?")) {
        var source = event.target || event.srcElement;
        $(source).closest('form').submit();
    }
}
//查看价目表
function viewPriceList(event) {
    return showPostFormModal(event, 'editPriceListForm', 'modal-info', true);
}

//添加编辑价目表
function createOrEditPriceList(event) {
    return showPostFormModal(event, 'editPriceListForm', null, true);
}

function moveProduct(event, up) {
    var source = event.target || event.srcElement;
    var actionUrl = $(source).data('url');

    $.ajax({
        type: 'post',
        url: actionUrl,
        success: function (data) {
            if (data) {
                window.location.reload();
            }
        }
    });
}

//确认删除产品
function deleteProduct(event) {
    var source = event.target || event.srcElement;
    $("#productId").val($(source).data("id"));
    $("#productName").text($(source).data("name"));
    $("#confirmDeleteDialog").modal("show");
}

$(function() {
    var frm = $('#deleteProductForm');
    frm.submit(function (ev) {
        $.ajax({
            type: frm.attr('method'),
            url: frm.attr('action'),
            data: frm.serialize(),
            success: function (data) {
                if (data) {
                    window.location.reload();
                }
            }
        });

        ev.preventDefault();
    });
})

function tpriceEnabled(event) {

    var source = event.target || event.srcElement;
    var actionUrl = $(source).data('url');

    $.ajax({
        type: 'post',
        url: actionUrl,
        success: function (data) {
            if (data) {
                window.location.reload();
            }
        }
    });
}
