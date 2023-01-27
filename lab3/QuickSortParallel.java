import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;



public class QuickSortParallel {
	/*
    https://www.baeldung.com/java-quicksort
    https://www.geeksforgeeks.org/quick-sort-using-multi-threading/
    https://stackoverflow.com/questions/3425126/java-parallelizing-quick-sort-via-multi-threading
    */
    public static class QuickSortRunnable implements Runnable {

        Integer[] array;
        int start;
        int end;
        int MAX_THREADS;
        ExecutorService executor;
        int minParitionSize;

        public QuickSortRunnable(int workers, Integer[] array, int start, int end) {
            this.MAX_THREADS = workers;
            executor = Executors.newFixedThreadPool(MAX_THREADS);
            this.minParitionSize = array.length/workers;
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            quickSort(array, start, end);
        }

        public static <T> void swap(T[] array, int i, int j) {
            T tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }

        public void quickSort(Integer[] array, int start, int end) {
            if (end - start + 1 <= 1)
                return;

            int pivot_index = start + new Random().nextInt(end - start);
            int pivotValue = array[pivot_index];

            swap(array, pivot_index, end);

            int storeIndex = start;
            for (int i = start; i < end; i++) {
                if (array[i] <= pivotValue) {
                    swap(array, i, storeIndex);
                    storeIndex++;
                }
            }
            swap(array, storeIndex, end);

            if (end - start + 1 > minParitionSize) {
                QuickSortRunnable quick = new QuickSortRunnable(minParitionSize, array, start, storeIndex - 1);
                Future<?> future = executor.submit(quick);
                quickSort(array, storeIndex + 1, end);

                try {
                    future.get();
                    executor.shutdown();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                quickSort(array, start, storeIndex - 1);
                quickSort(array, storeIndex + 1, end);
            }
        }
    }
    
    public static void saveToFile(ArrayList toSave, String fileName) throws IOException {
        PrintWriter pw = new PrintWriter(new FileOutputStream(fileName));
        for (Object data : toSave)
            pw.println(data.toString());
        pw.close();
    }

    public static void main(String[] args) throws IOException {

        Integer[] sizes = new Integer[]{100,1000,10000,100000,1000000,2000000,3000000,4000000,5000000};
        int numIter = 10;

        ArrayList<Double> timesArraysSort = new ArrayList<>();
        ArrayList<Double> timesQuickSort = new ArrayList<>();

        for (int size : sizes) {
            double duration = 0.0;
            double duration2 = 0.0;
            for (int k = 0; k < numIter; k++) {
                Integer[] array1 = new Integer[size];
                Integer[] array2 = new Integer[size];
                for (int i = 0; i < array1.length; i++) {
                    int tmp = new Random().nextInt(size);
                    array1[i] = tmp;
                    array2[i] = tmp;
                }
                long startTime = System.nanoTime();
                Arrays.sort(array1);
                long endTime = System.nanoTime();
                duration += (double) ((endTime - startTime) / 1000000 / numIter);

                long startTime2 = System.nanoTime();
                QuickSortRunnable quick = new QuickSortRunnable(Runtime.getRuntime().availableProcessors(),
                        array2, 0, array2.length - 1);
                quick.run();
                long endTime2 = System.nanoTime();
                duration2 += (double) ((endTime2 - startTime2) / 1000000 / numIter);
            }
            timesArraysSort.add(duration);
            timesQuickSort.add(duration2);
        }
        saveToFile(timesArraysSort, "wykres_dane.txt");
        saveToFile(timesQuickSort, "wykres_dane_parallel.txt");


        Integer[] array1 = new Integer[sizes[1]];
        Integer[] array2 = new Integer[sizes[1]];
        for(int i=0; i<array1.length; i++) {
            int tmp = new Random().nextInt(sizes[1]);
            array1[i] = tmp;
            array2[i] = tmp;
        }

        Arrays.sort(array1);
        System.out.println(Arrays.toString(array1));

        QuickSortRunnable quick = new QuickSortRunnable(Runtime.getRuntime().availableProcessors(),
                array2, 0, array2.length - 1);
        quick.run();
        System.out.println(Arrays.toString(array2));
    }

}
