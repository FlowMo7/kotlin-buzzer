package dev.moetz.buzzer.qr

import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.OutputStream
import javax.imageio.ImageIO

class QrCodeManager {

    fun generateQRCode(content: String, width: Int, height: Int, outputStream: OutputStream) {
        val writer = QRCodeWriter()
        val matrix = writer.encode(
            content,
            BarcodeFormat.QR_CODE,
            width,
            height
        )

        val image = BufferedImage(matrix.width, matrix.height, BufferedImage.TYPE_INT_RGB).apply {
            graphics.apply {
                color = Color.WHITE
                fillRect(0, 0, matrix.width, matrix.height)
                color = Color.BLACK
                (0 until matrix.height).forEach { y ->
                    (0 until matrix.width).forEach { x ->
                        if (matrix.get(x, y)) {
                            fillRect(x, y, 1, 1)
                        }
                    }
                }
            }
        }

        ImageIO.write(image, "png", outputStream)
    }

}