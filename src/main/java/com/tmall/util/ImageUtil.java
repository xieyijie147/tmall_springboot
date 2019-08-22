package com.tmall.util;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;


/*
*ImageUtil 工具类提供3个方法
1. change2jpg
确保图片文件的二进制格式是jpg。
仅仅通过ImageIO.write(img, "jpg", file);不足以保证转换出来的jpg文件显示正常。这段转换代码，可以确保转换后jpg的图片显示正常，而不会出现暗红色( 有一定几率出现)。
我也是度娘上抄的，哈哈哈~ 不过找了很多代码哦，才找到这一段能真正生效，而且不会发生错误的。

2. 后两种resizeImage用于改变图片大小，在上传产品图片的时候会用到。 这里不展开，到时候再讲
* */
public class ImageUtil {
    public static BufferedImage change2jpg(File f){
        try {
            Image i = Toolkit.getDefaultToolkit().createImage(f.getAbsolutePath());
            PixelGrabber pg = new PixelGrabber(i, 0, 0, -1, -1, true);
            pg.grabPixels();
            int width = pg.getWidth(), height = pg.getHeight();
            final int[] RGB_MASKS = {0xFF0000, 0xFF00, 0xFF };
            final ColorModel RGB_OPAQUE = new DirectColorModel(32, RGB_MASKS[0], RGB_MASKS[1], RGB_MASKS[2]);
            DataBuffer buffer = new DataBufferInt((int[])pg.getPixels(), pg.getWidth() * pg.getHeight());
            WritableRaster raster = Raster.createPackedRaster(buffer, width, height, width, RGB_MASKS, null);
            BufferedImage img = new BufferedImage(RGB_OPAQUE, raster, false, null);
            return img;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void resizeImage(File srcFile, int width, int height, File destFile){
        try {
            if(!destFile.getParentFile().exists()){
                destFile.getParentFile().mkdirs();
            }
            Image i = ImageIO.read(srcFile);
            i = resizeImage(i, width, height);
            ImageIO.write((RenderedImage)i, "jpg", destFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Image resizeImage(Image srcImage, int width, int height){
        try {
            BufferedImage bufferedImage = null;
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            bufferedImage.getGraphics().drawImage(srcImage.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
            return bufferedImage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
