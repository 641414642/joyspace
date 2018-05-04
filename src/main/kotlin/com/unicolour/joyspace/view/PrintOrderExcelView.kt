package com.unicolour.joyspace.view

import com.unicolour.joyspace.dto.PrintOrderWrapper
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Workbook
import org.springframework.web.servlet.view.document.AbstractXlsxView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class PrintOrderExcelView : AbstractXlsxView() {
    override fun buildExcelDocument(model: MutableMap<String, Any>, workbook: Workbook, request: HttpServletRequest, response: HttpServletResponse) {
        val printOrders = model["printOrders"] as List<PrintOrderWrapper>

        response.setHeader("Content-Disposition", """attachment; filename="PrintOrders.xlsx"""")

        val sheet = workbook.createSheet("订单列表")
        val createHelper = workbook.creationHelper

        val headerStyle = workbook.createCellStyle()
        headerStyle.fillForegroundColor = HSSFColor.GREY_25_PERCENT.index
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)

        val dateTimeCellStyle = workbook.createCellStyle()
        dateTimeCellStyle.dataFormat = createHelper.createDataFormat().getFormat("yyyy-m-d h:mm:ss")

        val intCellStyle = workbook.createCellStyle()
        intCellStyle.dataFormat = createHelper.createDataFormat().getFormat("0")

        val currencyCellStyle = workbook.createCellStyle()
        currencyCellStyle.dataFormat = createHelper.createDataFormat().getFormat("""¥#,##0.00;¥-#,##0.00""")

        // create header row
        val header = sheet.createRow(0)
        var column = 0
        header.createCell(column++).apply { setCellValue("ID"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 10 * 256) }
        header.createCell(column++).apply { setCellValue("编号"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 20 * 256) }
        header.createCell(column++).apply { setCellValue("创建时间"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 20 * 256) }
        header.createCell(column++).apply { setCellValue("自助机"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 10 * 256) }
        header.createCell(column++).apply { setCellValue("店面"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 20 * 256) }
        header.createCell(column++).apply { setCellValue("用户"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 20 * 256) }
        header.createCell(column++).apply { setCellValue("总价"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 10 * 256) }
        header.createCell(column++).apply { setCellValue("折扣"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 10 * 256) }
        header.createCell(column++).apply { setCellValue("转账时间"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 20 * 256) }
        header.createCell(column++).apply { setCellValue("转账金额"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 10 * 256) }
        header.createCell(column++).apply { setCellValue("手续费"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 10 * 256) }
        header.createCell(column++).apply { setCellValue("收款人"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 10 * 256) }
        header.createCell(column++).apply { setCellValue("支付"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 5 * 256) }
        header.createCell(column++).apply { setCellValue("上传"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 5 * 256) }
        header.createCell(column++).apply { setCellValue("下载"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 5 * 256) }
        header.createCell(column++).apply { setCellValue("打印"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 5 * 256) }
        header.createCell(column).apply { setCellValue("转账"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 5 * 256) }

        for((index, wrapper) in printOrders.withIndex()){
            column = 0
            val row =  sheet.createRow(index+1)
            row.createCell(column++).apply { setCellValue(wrapper.order.id.toDouble()); cellStyle = intCellStyle }
            row.createCell(column++).setCellValue(wrapper.order.orderNo)
            row.createCell(column++).apply { setCellValue(wrapper.order.createTime); cellStyle = dateTimeCellStyle }
            row.createCell(column++).apply { setCellValue(wrapper.order.printStationId.toDouble()); cellStyle = intCellStyle }
            row.createCell(column++).setCellValue(wrapper.position.name)
            row.createCell(column++).setCellValue(wrapper.userName)
            row.createCell(column++).apply { setCellValue(wrapper.order.totalFee / 100.0); cellStyle = currencyCellStyle }

            if (wrapper.order.discount > 0) {
                row.createCell(column++).apply { setCellValue(wrapper.order.discount / 100.0); cellStyle = currencyCellStyle; }
            }
            else {
                row.createCell(column++)
            }

            row.createCell(column++).apply { setCellValue(wrapper.transferRecord?.transferTime); cellStyle = dateTimeCellStyle }
            if (wrapper.transferRecord != null) {
                row.createCell(column++).apply { setCellValue(wrapper.transferRecord.amount / 100.0); cellStyle = currencyCellStyle }
            }
            else {
                row.createCell(column++)
            }

            if (wrapper.transferRecordItem?.charge ?: 0 > 0) {
                row.createCell(column++).apply { setCellValue(wrapper.transferRecordItem!!.charge / 100.0); cellStyle = currencyCellStyle }
            }
            else {
                row.createCell(column++)
            }
            row.createCell(column++).setCellValue(wrapper.transferRecord?.receiverName)
            row.createCell(column++).apply { if (wrapper.order.payed) setCellValue("✓") }
            row.createCell(column++).apply { if (wrapper.order.imageFileUploaded) setCellValue("✓") }
            row.createCell(column++).apply { if (wrapper.order.downloadedToPrintStation) setCellValue("✓") }
            row.createCell(column++).apply { if (wrapper.order.printedOnPrintStation) setCellValue("✓") }
            row.createCell(column).apply { if (wrapper.order.transfered) setCellValue("✓") }
        }
    }
}