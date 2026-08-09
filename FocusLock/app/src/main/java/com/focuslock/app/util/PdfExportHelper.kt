package com.focuslock.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.focuslock.app.data.model.UsageSessionEntity
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PdfExportHelper(private val context: Context) {

    fun exportUsageToPdf(sessions: List<UsageSessionEntity>, totalSavedMinutes: Int) {
        if (sessions.isEmpty()) return // Requirement: "i need no empty table in the pdf"

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Time formatters
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

        // Title
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Focus Lock - Detailed Activity Log", 50f, 50f, paint)

        // Month Header (Requirement: "make the month as a month name")
        val monthName = LocalDate.now().month.name.lowercase().replaceFirstChar { it.uppercase() }
        val year = LocalDate.now().year
        paint.textSize = 14f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("Report for $monthName $year", 50f, 75f, paint)

        // Summary
        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        canvas.drawText("Total Focus Time Saved: $totalSavedMinutes min", 50f, 110f, paint)

        // Table Header (Requirements: App Name, Usage, Start, End)
        paint.textSize = 10f
        var yPos = 150f
        canvas.drawText("App Name", 50f, yPos, paint)
        canvas.drawText("Usage", 180f, yPos, paint)
        canvas.drawText("Start Time", 260f, yPos, paint)
        canvas.drawText("End Time", 380f, yPos, paint)
        canvas.drawText("Date", 500f, yPos, paint)
        
        yPos += 5f
        drawLine(canvas, 50f, yPos, 550f, 2f)
        yPos += 20f

        // Table Content
        paint.isFakeBoldText = false
        sessions.forEach { session ->
            if (yPos > 800f) return@forEach // Simple page overflow handling

            val start = Instant.ofEpochMilli(session.startTimeMillis).atZone(ZoneId.systemDefault()).toLocalTime()
            val end = Instant.ofEpochMilli(session.endTimeMillis).atZone(ZoneId.systemDefault()).toLocalTime()
            val date = LocalDate.ofEpochDay(session.dateEpochDay).format(DateTimeFormatter.ofPattern("MMM dd"))
            
            val minutes = session.durationMillis / 60000
            val seconds = (session.durationMillis % 60000) / 1000
            val durationText = "${minutes}m ${seconds}s"

            // Get a cleaner name from package (Try real name first)
            val pm = context.packageManager
            val cleanName = try {
                val info = pm.getApplicationInfo(session.packageName, 0)
                pm.getApplicationLabel(info).toString()
            } catch (ignored: Exception) {
                session.packageName.split(".").last().replaceFirstChar { it.uppercase() }
            }

            canvas.drawText(cleanName, 50f, yPos, paint)
            canvas.drawText(durationText, 180f, yPos, paint)
            canvas.drawText(start.format(timeFormatter), 260f, yPos, paint)
            canvas.drawText(end.format(timeFormatter), 380f, yPos, paint)
            canvas.drawText(date, 500f, yPos, paint)
            
            yPos += 25f
            drawLine(canvas, 50f, yPos - 15f, 550f, 0.5f)
        }

        document.finishPage(page)

        // Save to file
        val exportsDir = File(context.cacheDir, "exports")
        if (!exportsDir.exists()) exportsDir.mkdirs()
        
        val file = File(exportsDir, "Usage_Report_${System.currentTimeMillis()}.pdf")
        try {
            document.writeTo(FileOutputStream(file))
            sharePdf(file)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document.close()
        }
    }

    private fun drawLine(canvas: Canvas, startX: Float, y: Float, endX: Float, strokeWidth: Float = 1f) {
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            this.strokeWidth = strokeWidth
        }
        canvas.drawLine(startX, y, endX, y, linePaint)
    }

    private fun sharePdf(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(intent, "Share Usage Report")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
