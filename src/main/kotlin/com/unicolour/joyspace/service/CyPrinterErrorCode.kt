package com.unicolour.joyspace.service

const val GROUP_USUALLY = 0x00010000
const val GROUP_SETTING = 0x00020000
const val GROUP_HARDWARE = 0x00040000
const val GROUP_SYSTEM	= 0x00080000

enum class CyPrinterErrorCode(val value: Int, val message: String, val sendSms: Boolean) {
    USUALLY_PAPER_END(GROUP_USUALLY or 0x0008, "相纸已经用尽，不能打印了，请您立即更换耗材", true),
    USUALLY_RIBBON_END(GROUP_USUALLY or 0x0010, "色带已经用尽，不能打印了，请您立即更换耗材", true),

    SETTING_PAPER_JAM(GROUP_SETTING or 0x0002, "发生卡纸错误，请前往检查", true),
    SETTING_RIBBON_ERR(GROUP_SETTING or 0x0004, "发生色带错误，请前往检查", true),
    SETTING_PAPER_ERR(GROUP_SETTING or 0x0008, "发生相纸错误，请前往检查", true),
    SETTING_DATA_ERR(GROUP_SETTING or 0x0010, "发生数据错误，请前往检查", true),

    HARDWARE_ERR01(GROUP_HARDWARE or 0x0001, "发生硬件错误，打印头电压错误，请前往检查", true),
    HARDWARE_ERR02(GROUP_HARDWARE or 0x0002, "发生硬件错误，打印头位置错误，请前往检查", true),
    HARDWARE_ERR03(GROUP_HARDWARE or 0x0004, "发生硬件错误，电源风扇停止，请前往检查", true),
    HARDWARE_ERR04(GROUP_HARDWARE or 0x0008, "发生硬件错误，裁切刀故障，请前往检查", true),
    HARDWARE_ERR05(GROUP_HARDWARE or 0x0010, "发生硬件错误，夹紧轮位置错误，请前往检查", true),
    HARDWARE_ERR06(GROUP_HARDWARE or 0x0020, "发生硬件错误，打印头温度异常，请前往检查", true),
    HARDWARE_ERR07(GROUP_HARDWARE or 0x0040, "发生硬件错误，介质温度异常，请前往检查", true),
    HARDWARE_ERR08(GROUP_HARDWARE or 0x0080, "发生硬件错误，色带张力异常，请前往检查", true),
    HARDWARE_ERR09(GROUP_HARDWARE or 0x0100, "发生硬件错误，RFID模块错误，请前往检查", true),
    HARDWARE_ERR10(GROUP_HARDWARE or 0x0200, "发生硬件错误，马达温度异常，请前往检查", true),

    SYSTEM_ERR01(GROUP_SYSTEM or 0x0001, "发生系统错误，请前往检查", true),

    USUALLY_IDLE(GROUP_USUALLY or 0x0001, "空闲", false),
    USUALLY_PRINTING(GROUP_USUALLY or 0x0002, "正在打印", false),
    USUALLY_COOLING(GROUP_USUALLY or 0x0020, "打印头正在冷却", false),
    USUALLY_MOTCOOLING(GROUP_USUALLY or 0x0040, "马达正在冷却", false),

    SETTING_COVER_OPEN(GROUP_SETTING or 0x0001, "盖子打开", false)
}