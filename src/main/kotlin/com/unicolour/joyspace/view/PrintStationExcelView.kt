package com.unicolour.joyspace.view

import com.unicolour.joyspace.controller.PrintStationController
import com.unicolour.joyspace.model.PrintStationStatus
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Workbook
import org.springframework.web.servlet.view.document.AbstractXlsxView
import java.text.DecimalFormat
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class PrintStationExcelView : AbstractXlsxView() {
    override fun buildExcelDocument(model: MutableMap<String, Any>, workbook: Workbook, request: HttpServletRequest, response: HttpServletResponse) {
        val printStations = model["printStations"] as List<PrintStationController.PrintStationInfo>
        val count = model["printStationCount"] as Int

        response.setHeader("Content-Disposition", """attachment; filename=PrintStations.xlsx"""")

        val sheet = workbook.createSheet("自助机列表")
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
        statHeaderRow.createCell(column).apply { setCellValue("自助机数量"); cellStyle = headerStyle }

        column = 0
        val statRow = sheet.createRow(rowOffset++)
        statRow.createCell(column).apply { setCellValue(count.toDouble()); cellStyle = intCellStyle }
        rowOffset++

        // create header row
        val header = sheet.createRow(rowOffset)
        column = 0
        header.createCell(column++).apply { setCellValue("ID"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 10 * 256) }
        header.createCell(column++).apply { setCellValue("名称"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 20 * 256) }
        header.createCell(column++).apply { setCellValue("投放商"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 30 * 256) }
        header.createCell(column++).apply { setCellValue("店面"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 30 * 256) }
        header.createCell(column++).apply { setCellValue("分账比例"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 10 * 256) }
        header.createCell(column++).apply { setCellValue("指定打印机类型"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 25 * 256) }
        header.createCell(column++).apply { setCellValue("打印机型号"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 25 * 256) }
        header.createCell(column++).apply { setCellValue("卷纸"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 10 * 256) }
        header.createCell(column++).apply { setCellValue("纸张"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 20 * 256) }
        header.createCell(column++).apply { setCellValue("打印机状态"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 15 * 256) }
        header.createCell(column++).apply { setCellValue("在线状态"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 15 * 256) }
        header.createCell(column++).apply { setCellValue("软件版本"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 10 * 256) }
        header.createCell(column).apply { setCellValue("广告"); cellStyle = headerStyle; sheet.setColumnWidth(columnIndex, 25 * 256) }

        val df = DecimalFormat("0.#")

        for((index, info) in printStations.withIndex()){
            column = 0

            val paperSize =
                    if (info.printStation.paperWidth == null || info.printStation.paperLength == null) {
                        ""
                    }
                    else {
                        df.format(info.printStation.paperWidth!! / 25.4) + " x " + df.format(info.printStation.paperLength!! / 25.4)
                    }

            val row =  sheet.createRow(rowOffset + index + 1)
            row.createCell(column++).apply { setCellValue(info.printStation.id.toDouble()); cellStyle = intCellStyle }
            row.createCell(column++).setCellValue(info.printStation.name)
            row.createCell(column++).setCellValue(info.printStation.company.name)
            row.createCell(column++).setCellValue(info.printStation.position.name)
            row.createCell(column++).setCellValue(df.format(info.printStation.transferProportion / 10.0) + "%")
            row.createCell(column++).setCellValue(info.printerTypeDisp)
            row.createCell(column++).setCellValue(info.printStation.printerModel ?: "")
            row.createCell(column++).apply { if (info.printStation.rollPaper == true) setCellValue("✓") }
            row.createCell(column++).setCellValue(paperSize)
            row.createCell(column++).setCellValue(PrintStationStatus.values().firstOrNull { it.value == info.printStation.status }?.message?:"")
            row.createCell(column++).setCellValue(if (info.online) "在线" else "离线")
            row.createCell(column++).setCellValue(info.printStation.lastLoginVersion?.toString() ?: "")
            row.createCell(column).setCellValue(info.printStation.adSet?.name ?: "")
        }
    }
}