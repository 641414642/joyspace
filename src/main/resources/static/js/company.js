//添加编辑店面
function createOrEditCompany(event, create) {
    return showPostFormModal(event, create ? 'createCompanyForm' : 'editCompanyForm', null, true);
}
