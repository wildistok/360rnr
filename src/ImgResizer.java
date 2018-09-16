import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.ExifTagConstants;
import org.apache.sanselan.formats.tiff.constants.TiffTagConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;


public class ImgResizer {
    int messege = 0;

    //получить название
    protected static String getName(String path) {
        String name;
        int i, first = 0, last = 0;
        for (i = path.length() - 1; i > 0; i--) {
            char a = path.charAt(i);
            if (a == '.') last = i;
            if ((a == '/') | (a == '\\')) {

                first = i;
                break;
            }
        }
        name = path.substring(first + 1, last);
        return name;
    }

    public static BufferedImage readImg(String path) {
        BufferedImage img;
        File file = new File(path);
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            img = null;
        }
        return img;
    }

    //do something with png and resolution
    public PanoImages resizerImg(String path, int resol, String directory) {
        //чтение первоначального файл
        File file = new File(path);

        BufferedImage inputImage = readImg(path);
        System.out.println(inputImage.getWidth());

        resol = inputImage.getWidth() / 2;
        BufferedImage imageLeft = new BufferedImage(resol, resol, BufferedImage.TYPE_INT_RGB);
        BufferedImage imageRight = new BufferedImage(resol, resol, BufferedImage.TYPE_INT_RGB);

        if (getType(path).equals("png") || getType(path).equals("PNG")) {
            String newPath = directory + getName(path) + "_tmp.jpg";
            saveImg(inputImage, "jpg", newPath);
            file = new File(newPath);
            inputImage = readImg(newPath);
        }

        //test
        BufferedImage img2_test = inputImage.getSubimage(0, 0, inputImage.getWidth() / 2, inputImage.getHeight());

        File imageLeftFile = new File(directory + getName(path) + "_1.jpg");
        File imageLeftFileTmp = new File(directory + getName(path) + "_1tmp.jpg");

        imageLeft.createGraphics().drawImage(img2_test, 0, 0, resol, resol, Color.white, null);
        saveImg(imageLeft, "jpg", String.valueOf(imageLeftFile.toPath()));
        saveImg(imageLeft, "jpg", String.valueOf(imageLeftFileTmp.toPath()));
//        setMetadata(file, file1Tmp, file1, resol);
        try {
            Files.delete(imageLeftFileTmp.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        inputImage = readImg(path);
        BufferedImage img3_test = inputImage.getSubimage(inputImage.getWidth() / 2, 0, inputImage.getWidth() / 2, inputImage.getHeight());

        File imageRightFile = new File(directory + getName(path) + "_2.jpg");
        File imageRightFileTmp = new File(directory + getName(path) + "_2tmp.jpg");

        imageRight.createGraphics().drawImage(img3_test, 0, 0, resol, resol, Color.white, null);
        saveImg(imageRight, "jpg", String.valueOf(imageRightFile.toPath()));
        saveImg(imageRight, "jpg", String.valueOf(imageRightFileTmp.toPath()));
//        setMetadata(file, file2Tmp, file2, resol);
        try {
            Files.delete(imageRightFileTmp.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }


        return new PanoImages(imageLeft, imageRight);
    }

    public String getPath(String path) {
        String apath = null;
        int last = 0;
        for (int i = path.length() - 1; i > 0; i--) {
            char a = path.charAt(i);
            if ((a == '/') | (a == '\\')) {
                last = i;
                break;
            }
        }
        apath = path.substring(0, last + 1);
        return apath;
    }

//    protected void setMetadata(File src, File dst, File tmp, int resol) {
//        OutputStream os = null;
//        try {
//            TiffOutputSet outputSet = null;
//
//            IImageMetadata meta = Imaging.getMetadata(src);
//            JpegImageMetadata jpegMetadata = (JpegImageMetadata) meta;
//            if (jpegMetadata != null)  {
//                TiffImageMetadata exif = jpegMetadata.getExif();
//                if (exif != null) {
//                    outputSet = exif.getOutputSet();
//                }
//            }
//            if (outputSet == null) {
//                outputSet = new TiffOutputSet();
//            }
//            TiffOutputDirectory exifDirectory = outputSet
//                    .getOrCreateRootDirectory();
//            exifDirectory.removeField(ExifTagConstants.EXIF_TAG_EXIF_IMAGE_WIDTH);
//            exifDirectory.add(TiffTagConstants.TIFF_TAG_IMAGE_WIDTH, resol);
//            exifDirectory.add(DcfTagConstants.EXIF_TAG_RELATED_IMAGE_WIDTH, resol);
//
//
//            os = new FileOutputStream(String.valueOf(tmp.toPath()));
//            os = new BufferedOutputStream(os);
//            new ExifRewriter().updateExifMetadataLossy(dst, os, outputSet);
//        } catch (ImageReadException | IOException | ImageWriteException e) {
//            e.printStackTrace();
//        }
//    }

    protected String getType(String path) {
        String str = null;
        int last = 0;
        for (int i = path.length() - 1; i > 0; i--) {
            char a = path.charAt(i);
            if (a == '.') {
                last = i;
                break;
            }
        }
        str = path.substring(last + 1, path.length());
        return str;
    }

    //сохранение изображения
    public void saveImg(BufferedImage img, String format, String name) {
        try {
            ImageIO.write(img, format, new File(name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
