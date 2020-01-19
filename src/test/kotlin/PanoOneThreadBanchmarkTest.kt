import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

class HelloJunit5Test {

    @Test
    fun Test1() {
        var pano = PanoOneThread()
        var res = pano.panoWritter("8k_2.jpg", "8k_1.jpg", "/Users/istokolyas/Desktop/360FishEye/src/test/resources")

        saveImg(res, "jpg", "/Users/istokolyas/Desktop/360FishEye/src/test/resources/one_thread_pano.jpg" )
    }

    fun saveImg(img: BufferedImage, format: String, name: String) {
        try {
            ImageIO.write(img, format, File(name))
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}