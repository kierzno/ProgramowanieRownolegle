import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class Mandelbrot{
	
	/*
	 https://github.com/joni/fractals/blob/master/mandelbrot/MandelbrotColor.java
	 */

	
	public static int Point(int max, double xpos, double ypos) {
        double x = 0;
        double y = 0;

        for(int i=0; i<max; ++i) {
            double oldX = x;
            double oldY = y;
            x = oldX*oldX - oldY*oldY + xpos;
            y = 2*oldX*oldY + ypos;

            if(Math.sqrt(x*x + y*y) >= 2)
                return i;
        }
        return 0;
    }


    public static BufferedImage Mandelbrot(int width, int height, double xmin, double xmax, double ymin, double ymax, int N) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        int point = 0;
                
        for(int i=0; i<width; ++i) {
            for(int j=0; j<height; ++j) {
                double xpos = xmin + i*((xmax - xmin) / (width - 1));
                double ypos = ymin + j*((ymax - ymin) / (height - 1));
                point = Point(N, xpos, ypos);
                image.setRGB(i, j, Color.HSBtoRGB(point/256f, 1, point/(point+8f)));
            }
        }
        return image;
    }

    public static BufferedImage getMandelbrot(int width, int height) {
        return Mandelbrot(width, height, -2.1, 0.6, -1.2, 1.2, 200);
    }

    public static long Time(int N, int width, int height, TimeUnit unit) {
        long[] time = new long[N];

        for(int i=0; i<N; ++i) {
            long start = System.nanoTime();
            getMandelbrot(width, height);
            long end = System.nanoTime();
            time[i] = end - start;
        }

        double avg = 0;
        for(double i : time)
            avg += i;
        avg /= N;

        return unit.convert((long)avg, TimeUnit.NANOSECONDS);
    }

    public static void imageToFile(BufferedImage image) throws IOException {
        File output = new File("Mandelbrot" + ".png");
        ImageIO.write(image, "png", output);
    }

    public static void timesToFile(int[] sizes, double[] times) throws IOException {
        FileWriter fw = new FileWriter("Mandelbrot" + ".txt");
        for (int i = 0; i < sizes.length; i++) {
            fw.write(sizes[i] + "\t" + times[i] + "\n");
        }
        fw.close();
    }

    public static void main(String[] args) throws IOException {
        int n = 3;
        int[] sizes = {32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};
        double[] times = {32, 64, 128, 256, 512, 1024, 2048, 4096, 8192};

        int j = 0;
        for (int i : sizes) {
            double time = Time(n, i, i, TimeUnit.MILLISECONDS);
            times[j] = time;
            j++;
        }
        timesToFile(sizes,times);
        imageToFile(getMandelbrot(1024, 1024));
    }
}
