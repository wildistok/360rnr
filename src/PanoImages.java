import java.awt.image.BufferedImage;

public class PanoImages {
    public BufferedImage leftImage;
    public BufferedImage rightImage;

    public PanoImages(BufferedImage leftImage, BufferedImage rightImage) {

        this.leftImage = leftImage;
        this.rightImage = rightImage;
    }

    public BufferedImage getLeftImage() {
        return leftImage;
    }

    public BufferedImage getRightImage() {
        return rightImage;
    }
}
