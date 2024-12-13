package vQuant;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Vector_Quantization {

    static int imageHeight = 0;
    static int imageWidth = 0;

    static int itr = 1;

    static ArrayList<ArrayList<ArrayList<Integer>>> ogImageAs2x2Blocks = new ArrayList<ArrayList<ArrayList<Integer>>>();
    static ArrayList<ImageBlock> imageAsCodeBlocksWithIndexes = new ArrayList<ImageBlock>();
    static ArrayList<ArrayList<ArrayList<Double>>> codeBookBlocks = new ArrayList<ArrayList<ArrayList<Double>>>();

    public static void createTestImage() {
        // Define the pixel values (6x6 array)
        int[][] pixels = {
            {1, 2, 7, 9, 4, 11},    // Row 1
            {3, 4, 6, 6, 12, 12},   // Row 2
            {4, 9, 15, 14, 9, 9},   // Row 3
            {10, 10, 20, 18, 8, 8}, // Row 4
            {4, 3, 17, 16, 1, 4},   // Row 5
            {4, 5, 18, 18, 5, 6}    // Row 6
        };

        imageWidth = pixels[0].length; // Number of columns
        imageHeight = pixels.length;  // Number of rows

        // Create a grayscale image
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

        // Populate the image with pixel values
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                // Ensure values are between 0 and 255
                int grayValue = Math.min(255, Math.max(0, pixels[y][x]));
                image.setRGB(x, y, (grayValue << 16) | (grayValue << 8) | grayValue); // Grayscale encoding
            }
        }

        // Save the image as BMP
        try {
            File outputFile = new File("test.bmp");
            ImageIO.write(image, "bmp", outputFile);
            System.out.println("Image saved as test.bmp");
        } catch (IOException e) {
            System.err.println("Error saving the image: " + e.getMessage());
        }
    }

    public static ArrayList<ArrayList<Integer>> readGrayscaleImage(String inputImagePath) throws IOException {
        BufferedImage image = ImageIO.read(new File(inputImagePath));

        imageWidth = image.getWidth();
        imageHeight = image.getHeight();

        ArrayList<ArrayList<Integer>> grayscaleValues = new ArrayList<>();

        // Populate the 2D ArrayList with pixel values
        for (int y = 0; y < imageHeight; y++) {
            ArrayList<Integer> row = new ArrayList<>();
            for (int x = 0; x < imageWidth; x++) {
                int rgb = image.getRGB(x, y);

                // Extract grayscale intensity (0–255)
                int red = (rgb >> 16) & 0xFF;  // Red component
                int green = (rgb >> 8) & 0xFF; // Green component
                int blue = rgb & 0xFF;         // Blue component

                // In grayscale, all channels should have the same value, so you can use any channel
                int grayscaleValue = red; // Using red here; you can use green or blue too

                row.add(grayscaleValue);
            }
            grayscaleValues.add(row);
        }

        return grayscaleValues;
    }
    
    public static void writeGrayscaleImage(ArrayList<ArrayList<Integer>> grayscaleValues, String outputImagePath) throws IOException {
        int height = grayscaleValues.size();
        int width = grayscaleValues.get(0).size();

        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = grayscaleValues.get(y).get(x);
                int rgbValue = (pixelValue << 16) | (pixelValue << 8) | pixelValue; // Convert to grayscale RGB
                outputImage.setRGB(x, y, rgbValue);
            }
        }

        ImageIO.write(outputImage, "BMP", new File(outputImagePath)); // Save as BMP
    }



    public static ArrayList<ArrayList<Double>> getFirst2x2block(ArrayList<ArrayList<Integer>> inputImage) {

        ArrayList<ArrayList<Double>> firstBlock = new ArrayList<ArrayList<Double>>();

        double cornerAvg = 0;
        
        for (int i = 0; i < 2; i++) {

            ArrayList<Double> row = new ArrayList<Double>();
            for (int j = 0; j < 2; j++) {

                cornerAvg = 0;
                for (int y = i; y < imageHeight; y = y + 2) {
                    for (int x = j; x < imageWidth; x = x + 2) {
                        cornerAvg += inputImage.get(y).get(x);
                        // System.out.println(inputImage.get(y).get(x) );
                    }
                }
                // System.out.println(cornerAvg/((imageHeight*imageWidth)/4));
                // System.out.println("j: " + j + "  i: " + i);
                row.add(cornerAvg/((imageHeight*imageWidth)/4));

            }
            firstBlock.add(row);
            
        }

        return firstBlock;
    }

    public static void cutImageIntoBlocks(ArrayList<ArrayList<Integer>> inputImage) {
        ArrayList<ArrayList<Integer>> block = new ArrayList<ArrayList<Integer>>();

        for (int y = 0; y < imageHeight; y = y + 2) {
            for (int x = 0; x < imageWidth; x = x + 2) {
                block = new ArrayList<ArrayList<Integer>>();
                
                ArrayList<Integer> row1 = new ArrayList<Integer>();
                ArrayList<Integer> row2 = new ArrayList<Integer>();
                
                row1.add(inputImage.get(y).get(x));
                row1.add(inputImage.get(y).get(x+1));
                row2.add(inputImage.get(y+1).get(x));
                row2.add(inputImage.get(y+1).get(x+1));

                block.add(row1);
                block.add(row2);

                ImageBlock blockObj = new ImageBlock(0, block);
                imageAsCodeBlocksWithIndexes.add(blockObj);

            }
        }

        // for (ImageBlock imageBlock : imageAsCodeBlocksWithIndexes) {
            
        //     System.out.println("index: " + imageBlock.index);
        //     for (int i = 0; i < 2; i++) {
        //         for (int j = 0; j < 2; j++) {
        //             System.out.print(imageBlock.block.get(i).get(j) + " ");
                    
        //         }
        //         System.out.println();
        //     }
        //     System.out.println();
        // }
    }

    public static void allocateToCodeBlock() {

        for (ImageBlock imageBlock : imageAsCodeBlocksWithIndexes) {
            int index = 0;
            int minBlock = 0;
            double min = 10000000;
            for (ArrayList<ArrayList<Double>> codeBlock : codeBookBlocks) {

                double diff = 0;
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        diff += Math.abs(codeBlock.get(i).get(j) - imageBlock.block.get(i).get(j));
                    }
                }

                if (diff < min) {
                    min = diff;
                    minBlock = index;
                }

                if(index == codeBookBlocks.size()-1) {
                    break;
                }

                index++;

            }

            imageBlock.index = minBlock;

        }

        // for (ImageBlock imageBlock : imageAsCodeBlocksWithIndexes) {
            
        //     System.out.println("index: " + imageBlock.index);
        //     for (int i = 0; i < 2; i++) {
        //         for (int j = 0; j < 2; j++) {
        //             System.out.print(imageBlock.block.get(i).get(j) + " ");
                    
        //         }
        //         System.out.println();
        //     }
        //     System.out.println();
        // }
        // System.out.println();
        // System.out.println();
        // System.out.println();
    }

    public static void getNew2x2CodeblockWithAvgs() {

        codeBookBlocks.clear();

        // ArrayList<ArrayList<Double>> splitBlock1 = new ArrayList<ArrayList<Double>>();
        // ArrayList<ArrayList<Double>> splitBlock2 = new ArrayList<ArrayList<Double>>();
        // ArrayList<ArrayList<Double>> splitBlock3 = new ArrayList<ArrayList<Double>>();
        // ArrayList<ArrayList<Double>> splitBlock4 = new ArrayList<ArrayList<Double>>();

        int currBlockIndex = 0;
        for (int k = 0; k < Math.pow(2, itr); k++) {
            int numWithThisIndex = 0;
            for (ImageBlock imageBlock : imageAsCodeBlocksWithIndexes) {
                if(imageBlock.index == currBlockIndex) {
                    numWithThisIndex++;
                }
            }

            ArrayList<ArrayList<Double>> avgBlock = new ArrayList<ArrayList<Double>>();
            
            for (int i = 0; i < 2; i++) {
                ArrayList<Double> row = new ArrayList<Double>();
                for (int j = 0; j < 2; j++) {

                    double cornerAvg = 0;
                    for (ImageBlock imageBlock : imageAsCodeBlocksWithIndexes) {
                        if(imageBlock.index == currBlockIndex) {
                            
                            cornerAvg += imageBlock.block.get(i).get(j);
                            
                        }
                    }
                    row.add(cornerAvg/(numWithThisIndex));
                }
                avgBlock.add(row);
            }
            codeBookBlocks.add(avgBlock);
            currBlockIndex++;
        }

        // for (ArrayList<ArrayList<Double>> codeBlock : codeBookBlocks) {
        //     for (int i = 0; i < 2; i++) {
        //         for (int j = 0; j < 2; j++) {
        //             System.out.print(codeBlock.get(i).get(j) + " ");
        //         }
        //         System.out.println();
        //     }
        //     // for (int i = 0; i < 2; i++) {
        //     //     for (int j = 0; j < 2; j++) {
        //     //         System.out.print(block2.get(i).get(j) + " ");
        //     //     }
        //     //     System.out.println();
        //     // }
        // }   
        

    }

    public static void splitCodeBlock() {
        ArrayList<ArrayList<Double>> block1 = new ArrayList<ArrayList<Double>>();
        ArrayList<ArrayList<Double>> block2 = new ArrayList<ArrayList<Double>>();
        ArrayList<ArrayList<ArrayList<Double>>> newCOdeBlocks = new ArrayList<ArrayList<ArrayList<Double>>>();

        // for (ArrayList<ArrayList<Double>> codeBlock : codeBookBlocks) {
        //     for (int i = 0; i < 2; i++) {
        //         for (int j = 0; j < 2; j++) {
        //             System.out.print(codeBlock.get(i).get(j) + " ");
        //         }
        //         System.out.println();
        //     }
        //     // for (int i = 0; i < 2; i++) {
        //     //     for (int j = 0; j < 2; j++) {
        //     //         System.out.print(block2.get(i).get(j) + " ");
        //     //     }
        //     //     System.out.println();
        //     // }
        // }   

        for (ArrayList<ArrayList<Double>> codeBlock : codeBookBlocks) {
            block1 = new ArrayList<ArrayList<Double>>();
            block2 = new ArrayList<ArrayList<Double>>();

            for (int i = 0; i < 2; i++) {
                ArrayList<Double> row1 = new ArrayList<Double>();
                ArrayList<Double> row2 = new ArrayList<Double>();

                for (int j = 0; j < 2; j++) {

                    if(codeBlock.get(i).get(j) % 1 == 0) {
                        row1.add(codeBlock.get(i).get(j)-1);
                        row2.add(codeBlock.get(i).get(j)+1);

                    } else {
                        
                        row1.add(Math.floor(codeBlock.get(i).get(j)));
                        row2.add(Math.ceil(codeBlock.get(i).get(j)));

                    }
                }
                block1.add(row1);
                block2.add(row2);
            }
            newCOdeBlocks.add(block1);
            newCOdeBlocks.add(block2);
        }

        codeBookBlocks = newCOdeBlocks;

        // for (ArrayList<ArrayList<Double>> codeBlock : codeBookBlocks) {
        //     for (int i = 0; i < 2; i++) {
        //         for (int j = 0; j < 2; j++) {
        //             System.out.print(codeBlock.get(i).get(j) + " ");
        //         }
        //         System.out.println();
        //     }
        //     System.out.println();
        //     // for (int i = 0; i < 2; i++) {
        //     //     for (int j = 0; j < 2; j++) {
        //     //         System.out.print(block2.get(i).get(j) + " ");
        //     //     }
        //     //     System.out.println();
        //     // }
        // }   
        // System.out.println(codeBookBlocks.size());
    }


    public static void main(String[] args) {
        String inputImagePath = "test.bmp"; // Replace with your BMP input image path
        String outputImagePath = "output_image.bmp"; // Replace with your BMP output image path

        try {
            createTestImage();

            ArrayList<ArrayList<Integer>> inputImage = readGrayscaleImage(inputImagePath);
            ArrayList<ArrayList<Double>> outputImage = new ArrayList<ArrayList<Double>>();

            cutImageIntoBlocks(inputImage);


            ArrayList<ArrayList<Double>> firstBlock = getFirst2x2block(inputImage);
            codeBookBlocks.add(firstBlock);

            // size of code book
            int k = 2;
            while (k != 0) {

                splitCodeBlock();
                allocateToCodeBlock();
                if(k > 0) {
                getNew2x2CodeblockWithAvgs();
                itr++;
                allocateToCodeBlock();
                }
                k--;
            }
            itr--;
            getNew2x2CodeblockWithAvgs();
            // allocateToCodeBlock();
            // allocateToCodeBlock();
            // getNew2x2CodeblockWithAvgs();
            // allocateToCodeBlock();
            // allocateToCodeBlock();


            for (ImageBlock imageBlock : imageAsCodeBlocksWithIndexes) {
            
                System.out.println("index: " + imageBlock.index);
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        System.out.print(imageBlock.block.get(i).get(j) + " ");
                        
                    }
                    System.out.println();
                }
                System.out.println();
            }
            System.out.println();
            System.out.println();
            System.out.println();

            for (ArrayList<ArrayList<Double>> codeBlock : codeBookBlocks) {
                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        System.out.print(codeBlock.get(i).get(j) + " ");
                    }
                    System.out.println();
                }
                System.out.println();
            }   
            System.out.println(codeBookBlocks.size());


            // for (int i = 0; i < 2; i++) {
            //     for (int j = 0; j < 2; j++) {
                    
            //         System.out.print(firstBlock.get(i).get(j) + " ");
            //     }
            //     System.out.println();
            // }

        } catch (IOException e) {
            System.err.println("Error processing the image: " + e.getMessage());
        }
    }
}
