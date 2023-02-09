package lab2;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MandelbrotThreads {

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

    public static class TablicaRunnable implements Runnable{
        int width, height, offset, N;
        double xmin, xmax, ymin, ymax;
        int[][] Tablica;

        public TablicaRunnable(int w, int h, int o, double xmi, double xma, double ymi, double yma, int n, int[][] T){
            this.width = w;
            this.height = h;
            this.offset = o;
            this.xmin = xmi;
            this.xmax = xma;
            this.ymin = ymi;
            this.ymax = yma;
            this.N = n;
            this.Tablica = T;
        }
        public void run(){
        	
            for(int x = this.offset; x < this.width; x++){
                double xtmp = xmin + (x*((xmax - xmin)/(height - 1)));
                for (int y = 0; y < height; y++){
                    double ytmp = ymin + (y*((ymax - ymin)/(height - 1)));
                    Tablica[x][y] = Point(N, xtmp, ytmp);
                    
                }
            }
        }
    }

    public static int[][] Tablica(int width, int height, double xmin, double xmax, double ymin, double ymax, int N){
        int[][] validityArray = new int[width][height];
        int numberOfThreads = Runtime.getRuntime().availableProcessors();
        int[] offsetArray = new int[numberOfThreads];
        int basicOffset = width/numberOfThreads;

        for(int i=0; i<numberOfThreads; ++i){
            offsetArray[i] = basicOffset*i;
        }

        Runnable runnable0 = new TablicaRunnable((basicOffset+offsetArray[0]), height, offsetArray[0], xmin, xmax, ymin, ymax, N, validityArray);
        Thread thread0 = new Thread(runnable0);
        thread0.start();

        Runnable runnable1 = new TablicaRunnable((basicOffset+offsetArray[1]), height, offsetArray[1], xmin, xmax, ymin, ymax, N, validityArray);
        Thread thread1 = new Thread(runnable1);
        thread1.start();

        Runnable runnable2 = new TablicaRunnable((basicOffset+offsetArray[2]), height, offsetArray[2], xmin, xmax, ymin, ymax, N, validityArray);
        Thread thread2 = new Thread(runnable2);
        thread2.start();

        Runnable runnable3 = new TablicaRunnable((basicOffset+offsetArray[3]), height, offsetArray[3], xmin, xmax, ymin, ymax, N, validityArray);
        Thread thread3 = new Thread(runnable3);
        thread3.start();

        try {
            thread0.join();
            thread1.join();
            thread2.join();
            thread3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return  validityArray;
    }

    public static BufferedImage Mandelbrot(int width, int height, int [][] Tablica) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for(int i=0; i<width; i++) {
            for(int j=0; j<height; j++) {
            	image.setRGB(i, j, Color.HSBtoRGB(Tablica[i][j]/256f, 1, Tablica[i][j]/(Tablica[i][j]+8f)));
            }
        }
        return image;
    }

    public static long Time(int ile, int width, int height, TimeUnit unit) {
        long[] time = new long[ile];

        for(int i=0; i<ile; i++) {
            long start = System.nanoTime();
            int[][] Tablica = Tablica(width, height, -2.1, 0.6, -1.2, 1.2, 200);
            Mandelbrot(width, height, Tablica);
            long end = System.nanoTime();
            time[i] = end - start;
        }

        double avg = 0;
        for(double i : time)
            avg += i;
        avg /= ile;

        return unit.convert((long)avg, TimeUnit.NANOSECONDS);
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

        int iterator = 0;
        for (int i : sizes) {
            double time = Time(n, i, i, TimeUnit.MILLISECONDS);
            times[iterator] = time;
            iterator++;
        }
        timesToFile(sizes,times);
    }
}
