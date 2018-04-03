//添加编辑模板
function createOrEditTemplate(event) {
    $('#modalTemplate').removeClass().addClass("modal fade");

    var source = event.target || event.srcElement;
    var tplType = $(source).data("tpl-type");

    showModal(event);
    return false;
}
