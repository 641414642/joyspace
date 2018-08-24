package com.unicolour.joyspace.view

import com.unicolour.joyspace.model.BusinessModel
import com.unicolour.joyspace.model.PrintOrder
import com.unicolour.joyspace.model.StationType
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Workbook
import org.springframework.web.servlet.view.document.AbstractXlsxView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class PrintOrderExcelView(private val isSuperAdmin:Boolean) : AbstractXlsxView() {
    override fun buildExcelDocument(model: MutableMap<String, Any>, workbook: Workbook, request: HttpServletRequest, response: HttpServletResponse) {
        val printOrders = model["printOrders"] as List<PrintOrder>
        val orderCount = model["printOrderCount"] as Int
        val photoCopies = model["photoCopies"] as Int
        val turnOver = model["turnOver"] as Int

        val companyIdBusinessModelMap = model["companyIdBusinessModelMap"] as Map<Int, Int>
        val printStationIdStationTypeMap = model["printStationIdStationTypeMap"] as Map<Int, Int>
        val printStationIdPrinterModelMap = model["printStationIdPrinterModelMap"] as Map<Int, String>

        val businessModelValObjMap = BusinessModel.values().map { it.value to it }.toMap()
        val stationTypeValObjMap = StationType.values().map { it.value to it }.toMap()

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


        var rowOffset = 0
        var column = 0
        val statHeaderRow = sheet.createRow(rowOffset++)
        statHeaderRow.createCell(column++).apply { setCellValue("订单数量"); cellStyle = headerStyle }
        statHeaderRow.createCell(column++).apply { setCellValue("照片数量"); cellStyle = headerStyle }
        statHeaderRow.createCell(column).apply { setCellValue("营业额"); cellStyle = headerStyle }

        column = 0
        val statRow = sheet.createRow(rowOffset++)
        statRow.createCell(column++).apply { setCellValue(orderCount.toDouble()); cellStyle = intCellStyle }
        statRow.createCell(column++).apply { setCellValue(photoCopies.toDouble()); cellStyle = intCellStyle }
        statRow.createCell(column).apply { setCellValue(turnOver / 100.0); cellStyle = currencyCellStyle }
        rowOffset++

        // create header row
        val header = sheet.createRow(rowOffset)
        column = 0
        header.createCell(column++).apply { setCellValue("ID"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 10 * 256) }
        header.createCell(column++).apply { setCellValue("创建时间"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 20 * 256) }
        if (isSuperAdmin) {
            header.createCell(column++).apply { setCellValue("店家"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 20 * 256) }
            header.createCell(column++).apply { setCellValue("经营方式"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 10 * 256) }
        }
        header.createCell(column++).apply { setCellValue("店面"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 20 * 256) }
        header.createCell(column++).apply { setCellValue("自助机"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 10 * 256) }
        if (isSuperAdmin) {
            header.createCell(column++).apply { setCellValue("站点属性"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 10 * 256) }
            header.createCell(column++).apply { setCellValue("打印机型号"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 15 * 256) }
        }
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

        for((index, order) in printOrders.withIndex()){
            column = 0
            val row =  sheet.createRow(rowOffset + index + 1)
            row.createCell(column++).apply { setCellValue(order.id.toDouble()); cellStyle = intCellStyle }
            row.createCell(column++).apply { setCellValue(order.createTime); cellStyle = dateTimeCellStyle }
            if (isSuperAdmin) {
                val businessModelVal = companyIdBusinessModelMap[order.companyId]
                val businessModel = businessModelValObjMap[businessModelVal] ?: BusinessModel.DEFAULT

                row.createCell(column++).setCellValue(order.companyName)
                row.createCell(column++).setCellValue(businessModel.displayName)
            }
            row.createCell(column++).setCellValue(order.positionName)
            row.createCell(column++).apply { setCellValue(order.printStationId.toDouble()); cellStyle = intCellStyle }
            if (isSuperAdmin) {
                val stationTypeVal = printStationIdStationTypeMap[order.printStationId]
                val stationType = stationTypeValObjMap[stationTypeVal] ?: StationType.DEFAULT

                row.createCell(column++).setCellValue(stationType.displayName)
                row.createCell(column++).setCellValue(printStationIdPrinterModelMap[order.printStationId])
            }

            row.createCell(column++).setCellValue(order.userName)
            row.createCell(column++).apply { setCellValue(order.totalFee / 100.0); cellStyle = currencyCellStyle }

            if (order.discount > 0) {
                row.createCell(column++).apply { setCellValue(order.discount / 100.0); cellStyle = currencyCellStyle; }
            }
            else {
                row.createCell(column++)
            }

            row.createCell(column++).apply { setCellValue(order.transferTime); cellStyle = dateTimeCellStyle }
            if (order.transferTime != null) {
                row.createCell(column++).apply { setCellValue(order.transferAmount / 100.0); cellStyle = currencyCellStyle }
                row.createCell(column++).apply { setCellValue(order.transferCharge / 100.0); cellStyle = currencyCellStyle }
            }
            else {
                row.createCell(column++)
                row.createCell(column++)
            }

            row.createCell(column++).setCellValue(order.transferReceiverName ?: "")
            row.createCell(column++).apply { if (order.payed) setCellValue("✓") }
            row.createCell(column++).apply { if (order.imageFileUploaded) setCellValue("✓") }
            row.createCell(column++).apply { if (order.downloadedToPrintStation) setCellValue("✓") }
            row.createCell(column++).apply { if (order.printedOnPrintStation) setCellValue("✓") }
            row.createCell(column).apply { if (order.transfered) setCellValue("✓") }
        }
    }
}