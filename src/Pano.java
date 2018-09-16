import java.awt.image.BufferedImage;

public interface Pano {
    public BufferedImage panoWritter(String file1, String file2, String fisheyePath);
    public BufferedImage panoWritter(BufferedImage buffered0, BufferedImage buffered1, String fisheyePath);

}
