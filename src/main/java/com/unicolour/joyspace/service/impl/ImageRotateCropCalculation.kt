package com.unicolour.joyspace.service.impl

internal fun rotateAndCropCalculation(imageWid: Int, imageHei: Int, angleDeg: Double) : CalcResult {
    val originRect = Rect(Point.ORIGIN_POINT, imageWid.toDouble(), imageHei.toDouble())
    val rotatedRect = originRect.rotate(angleDeg)
    val containRect = rotatedRect.containRect
    val innerRect = getInnerRect(originRect, rotatedRect)

    val dx = -containRect.p1.x
    val dy = -containRect.p1.y

    return CalcResult(
            originRect.translate(dx, dy),
            rotatedRect.translate(dx, dy),
            containRect.translate(dx, dy),
            innerRect.translate(dx, dy))
}

private fun getInnerRect(originRect: Rect, rotatedRect: Rect): Rect {
    val line1 = LineSegment(Point.ORIGIN_POINT, originRect.p1)
    val line2 = LineSegment(Point.ORIGIN_POINT, originRect.p2)
    val line3 = LineSegment(Point.ORIGIN_POINT, originRect.p3)
    val line4 = LineSegment(Point.ORIGIN_POINT, originRect.p4)

    val i1 = rotatedRect.getIntersectionWithLineSegment(line1)
    val i3 = rotatedRect.getIntersectionWithLineSegment(line3)

    val i2 = rotatedRect.getIntersectionWithLineSegment(line2)
    val i4 = rotatedRect.getIntersectionWithLineSegment(line4)

    val innerRect1 = if (i1 == null || i3 == null) null else Rect(i1, i3)
    val innerRect2 = if (i2 == null || i4 == null) null else Rect(i2, i4)

    if (innerRect1 != null && innerRect2 != null) {
        if (innerRect1.smallerThan(innerRect2)) {
            return innerRect1
        }
        else {
            return innerRect2
        }
    } else if (innerRect1 != null) {
        return innerRect1
    } else {
        return innerRect2!!
    }
}

internal class CalcResult(
        val originRect: Rect,
        val rotatedRect: Rect,
        val containRect: Rect,
        val innerRect: Rect
)

internal class Point(val x: Double, val y: Double) {
    companion object {
        val ORIGIN_POINT = Point(0.0, 0.0)
    }

    fun rotate(degAngle: Double) : Point {
        val a = degAngle * Math.PI / 180.0
        val sinA = Math.sin(a)
        val cosA = Math.cos(a)

        val yR = y * cosA - x * sinA
        val xR = y * sinA + x * cosA

        return Point(xR, yR)
    }

    fun translate(dx: Double, dy: Double) : Point {
        return Point(x + dx, y + dy)
    }
}

internal class LineSegment(private val p1: Point, private val p2: Point) {
    fun intersection(otherLine: LineSegment): Point? {
        val p0 = this.p1
        val p1 = this.p2
        val p2 = otherLine.p1
        val p3 = otherLine.p2

        val s1X = p1.x - p0.x
        val s1Y = p1.y - p0.y

        val s2X = p3.x - p2.x
        val s2Y = p3.y - p2.y

        val s = (-s1Y * (p0.x - p2.x) + s1X * (p0.y - p2.y)) / (-s2X * s1Y + s1X * s2Y)
        val t = (s2X * (p0.y - p2.y) - s2Y * (p0.x - p2.x)) / (-s2X * s1Y + s1X * s2Y)

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
            val iX = p0.x + t * s1X
            val iY = p0.y + t * s1Y

            return Point(iX, iY)
        }

        return null
    }
}

internal class Rect(
        val p1: Point,   //左上
        val p2: Point,   //右上
        val p3: Point,   //右下
        val p4: Point)   //左下
{

    constructor(centerPoint: Point, wid: Double, hei: Double) : this(
            Point(centerPoint.x - wid / 2.0, centerPoint.y - hei / 2.0),
            Point(centerPoint.x + wid / 2.0, centerPoint.y - hei / 2.0),
            Point(centerPoint.x + wid / 2.0, centerPoint.y + hei / 2.0),
            Point(centerPoint.x - wid / 2.0, centerPoint.y + hei / 2.0)
    )

    constructor(p1: Point, p3: Point) : this(
            Point(p1.x + (p3.x - p1.x) / 2.0, p1.y + (p3.y - p1.y) / 2.0),
            Math.abs(p3.x - p1.x),
            Math.abs(p3.y - p1.y))

    fun smallerThan(otherRect: Rect): Boolean {
        return Math.abs(p2.x - p1.x) < Math.abs(otherRect.p2.x - otherRect.p1.x)
    }

    fun rotate(degAngle: Double): Rect {
        val pr1 = this.p1.rotate(degAngle)
        val pr2 = this.p2.rotate(degAngle)
        val pr3 = this.p3.rotate(degAngle)
        val pr4 = this.p4.rotate(degAngle)

        return Rect(pr1, pr2, pr3, pr4)
    }

    val width : Double
        get() {
            return Math.abs(p2.x - p1.x)
        }

    val height : Double
        get() {
            return Math.abs(p3.y - p2.y)
        }

    val containRect: Rect
        get() {
            val minX = minOf(minOf(p1.x, p2.x), minOf(p3.x, p4.x))
            val maxX = maxOf(maxOf(p1.x, p2.x), maxOf(p3.x, p4.x))
            val minY = minOf(minOf(p1.y, p2.y), minOf(p3.y, p4.y))
            val maxY = maxOf(maxOf(p1.y, p2.y), maxOf(p3.y, p4.y))

            return Rect(Point(minX, minY), Point(maxX, maxY))
        }

    fun translate(dx: Double, dy: Double) : Rect {
        return Rect(
                p1.translate(dx, dy),
                p2.translate(dx, dy),
                p3.translate(dx, dy),
                p4.translate(dx, dy))
    }

    fun getIntersectionWithLineSegment(line: LineSegment): Point? {
        var p: Point? = line.intersection(LineSegment(p1, p2))
        if (p != null) {
            return p
        }

        p = line.intersection(LineSegment(p2, p3))
        if (p != null) {
            return p
        }

        p = line.intersection(LineSegment(p3, p4))
        if (p != null) {
            return p
        }

        p = line.intersection(LineSegment(p4, p1))
        return if (p != null) {
            p
        } else null
    }
}