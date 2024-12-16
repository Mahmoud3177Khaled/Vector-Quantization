package vQuant;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class Vector_Quantization {

    static int imageHeight = 0;
    static int imageWidth = 0;

    static int codeBookSize = 0;
    static int codeBookBlockSize = 0;

    static int itr = 1;
    static boolean notSameImageBlocksIndexes = true;

    // static ArrayList<ArrayList<ArrayList<Integer>>> ogImageAs2x2Blocks = new ArrayList<ArrayList<ArrayList<Integer>>>();
    static ArrayList<ImageBlock> imageAsBlocksWithIndexes = new ArrayList<ImageBlock>();
    static ArrayList<ArrayList<ArrayList<Double>>> codeBookBlocks = new ArrayList<ArrayList<ArrayList<Double>>>();

    static ArrayList<Integer> tempImageBlocksIndexes = new ArrayList<Integer>();

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



    public static ArrayList<ArrayList<Double>> getFirstCodeBlock(ArrayList<ArrayList<Integer>> inputImage) {

        ArrayList<ArrayList<Double>> firstBlock = new ArrayList<ArrayList<Double>>();

        double cornerAvg = 0;
        
        for (int i = 0; i < codeBookBlockSize; i++) {

            ArrayList<Double> row = new ArrayList<Double>();
            for (int j = 0; j < codeBookBlockSize; j++) {

                cornerAvg = 0;
                for (int y = i; y < imageHeight; y = y + codeBookBlockSize) {
                    for (int x = j; x < imageWidth; x = x + codeBookBlockSize) {
                        cornerAvg += inputImage.get(y).get(x);
                        // System.out.println(inputImage.get(y).get(x) );
                    }
                }
                // System.out.println(cornerAvg/((imageHeight*imageWidth)/4));
                // System.out.println("j: " + j + "  i: " + i);
                row.add(cornerAvg/(((imageHeight*imageWidth)/(codeBookBlockSize*codeBookBlockSize))));

            }
            firstBlock.add(row);
            
        }

        return firstBlock;
    }

    public static void cutImageIntoBlocks(ArrayList<ArrayList<Integer>> inputImage) {
        ArrayList<ArrayList<Integer>> block = new ArrayList<ArrayList<Integer>>();

        for (int y = 0; y < imageHeight; y = y + codeBookBlockSize) {
            for (int x = 0; x < imageWidth; x = x + codeBookBlockSize) {
                
                block = new ArrayList<ArrayList<Integer>>();
                for (int i = 0; i < codeBookBlockSize; i++) {
                    ArrayList<Integer> row = new ArrayList<Integer>();
                    for (int j = 0; j < codeBookBlockSize; j++) {
                        row.add(inputImage.get(y+i).get(x+j));
                    }
                    block.add(row);
                }
                
                ImageBlock blockObj = new ImageBlock(0, block);
                imageAsBlocksWithIndexes.add(blockObj);

        

            }
        }

        // for (int y = 0; y < imageHeight; y = y + codeBookBlockSize) {
        //     for (int x = 0; x < imageWidth; x = x + codeBookBlockSize) {
        //         block = new ArrayList<ArrayList<Integer>>();
                
        //         ArrayList<Integer> row1 = new ArrayList<Integer>();
        //         ArrayList<Integer> row2 = new ArrayList<Integer>();
                
        //         row1.add(inputImage.get(y).get(x));
        //         row1.add(inputImage.get(y).get(x+1));
        //         row2.add(inputImage.get(y+1).get(x));
        //         row2.add(inputImage.get(y+1).get(x+1));

        //         block.add(row1);
        //         block.add(row2);

        //         ImageBlock blockObj = new ImageBlock(0, block);
        //         imageAsBlocksWithIndexes.add(blockObj);

        //     }
        // }

        // for (ImageBlock imageBlock : imageAsBlocksWithIndexes) {
            
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

        notSameImageBlocksIndexes = true;
        tempImageBlocksIndexes.clear();

        for (ImageBlock imageBlock : imageAsBlocksWithIndexes) {
            tempImageBlocksIndexes.add(imageBlock.index);
        }

        for (ImageBlock imageBlock : imageAsBlocksWithIndexes) {
            int index = 0;
            int minBlock = 0;
            double min = 10000000;
            for (ArrayList<ArrayList<Double>> codeBlock : codeBookBlocks) {

                double diff = 0;
                for (int i = 0; i < codeBookBlockSize; i++) {
                    for (int j = 0; j < codeBookBlockSize; j++) {
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

        // System.out.println("\n");
        // for (Integer temp : tempImageBlocksIndexes) {
        //     System.out.print(temp + " ");
        // }
        // System.out.println("");
        // for (ImageBlock imageBlock : imageAsBlocksWithIndexes) {
        //     System.out.print(imageBlock.index + " ");

        // }
        int i = 0;
        for (ImageBlock imageBlock : imageAsBlocksWithIndexes) {
            if(imageBlock.index != tempImageBlocksIndexes.get(i)) {
                notSameImageBlocksIndexes = true;
                break;
            } else {
                notSameImageBlocksIndexes = false;
                
            }
            i++;
        }

        // for (ImageBlock imageBlock : imageAsBlocksWithIndexes) {
            
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

    public static void getNewCodeBlockUsingAvgs() {

        codeBookBlocks.clear();

        // ArrayList<ArrayList<Double>> splitBlock1 = new ArrayList<ArrayList<Double>>();
        // ArrayList<ArrayList<Double>> splitBlock2 = new ArrayList<ArrayList<Double>>();
        // ArrayList<ArrayList<Double>> splitBlock3 = new ArrayList<ArrayList<Double>>();
        // ArrayList<ArrayList<Double>> splitBlock4 = new ArrayList<ArrayList<Double>>();

        int currBlockIndex = 0;
        for (int k = 0; k < Math.pow(2, itr); k++) {
            int numWithThisIndex = 0;
            for (ImageBlock imageBlock : imageAsBlocksWithIndexes) {
                if(imageBlock.index == currBlockIndex) {
                    numWithThisIndex++;
                }
            }

            if(numWithThisIndex == 0) {
                currBlockIndex++;
                continue;
            }

            ArrayList<ArrayList<Double>> avgBlock = new ArrayList<ArrayList<Double>>();
            
            for (int i = 0; i < codeBookBlockSize; i++) {
                ArrayList<Double> row = new ArrayList<Double>();
                for (int j = 0; j < codeBookBlockSize; j++) {

                    double cornerAvg = 0;
                    for (ImageBlock imageBlock : imageAsBlocksWithIndexes) {
                        if(imageBlock.index == currBlockIndex) {
                            
                            cornerAvg += imageBlock.block.get(i).get(j);
                            
                        }
                    }
                    row.add(cornerAvg/(numWithThisIndex));
                }
                avgBlock.add(row);
            }

            // if(numWithThisIndex == 0) {
            //     continue;
            // }

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

            // if(codeBlock.get(0).get(0).isNaN()) {
            //     continue;
            // }

            // block1 = new ArrayList<ArrayList<Double>>();
            // block2 = new ArrayList<ArrayList<Double>>();

            // for (int y = 0; y < imageHeight; y++) {
            //     ArrayList<Double> row1 = new ArrayList<Double>();
            //     ArrayList<Double> row2 = new ArrayList<Double>();
            //     for (int x = 0; x < imageWidth; x++) {

            //         if(codeBlock.get(y).get(x) % 1 == 0) {
            //             row1.add(codeBlock.get(y).get(x)-1);
            //             row2.add(codeBlock.get(y).get(x)+1);

            //         } else {
                        
            //             row1.add(Math.floor(codeBlock.get(y).get(x)));
            //             row2.add(Math.ceil(codeBlock.get(y).get(x)));

            //         }
            //     }
            //     block1.add(row1);
            //     block2.add(row2);
            // }
            // newCOdeBlocks.add(block1);
            // newCOdeBlocks.add(block2);
            // codeBookBlocks = newCOdeBlocks;


            // System.out.println(cornerAvg/((imageHeight*imageWidth)/4));
            // System.out.println("j: " + j + "  i: " + i);
            // row.add(cornerAvg/((imageHeight*imageWidth)/4));

            block1 = new ArrayList<ArrayList<Double>>();
            block2 = new ArrayList<ArrayList<Double>>();

            for (int i = 0; i < codeBookBlockSize; i++) {
                ArrayList<Double> row1 = new ArrayList<Double>();
                ArrayList<Double> row2 = new ArrayList<Double>();

                for (int j = 0; j < codeBookBlockSize; j++) {

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

    public static void compileCompressedImage(String outPutFile) {
        // ArrayList<ArrayList<Integer>> compressedImageInCodeBookBlocks = new ArrayList<ArrayList<Integer>>();
        try {
            FileWriter writer = new FileWriter(new File(outPutFile + ".txt"));
            
            String row = "";
            int i = 1;
            for (ImageBlock imageBlock : imageAsBlocksWithIndexes) {
                
                row += imageBlock.index + " ";

                if (i % (imageWidth/codeBookBlockSize) == 0) {
                    row += "\n";
                    
                }
                
                i++;
            }
            
            writer.write(row);
            writer.close();

        } catch (IOException e) {
            System.out.println("can not save compressed image");
        }

        // return compressedImageInCodeBookBlocks;
    }

    public static void saveCodeBooksToFile(String codeBookPath) {
        try {
            FileWriter writer = new FileWriter(new File(codeBookPath + ".txt"));
            
            String row = "";
            int i = 0;
            for (ArrayList<ArrayList<Double>> codeBookBlock : codeBookBlocks) {
                
                row += i;
                row += "\n";

                for (int j = 0; j < codeBookBlockSize; j++) {
                    for (int k = 0; k < codeBookBlockSize; k++) {
                        
                        row += codeBookBlock.get(j).get(k) + " ";

                    }
                    row += "\n";
                }
                
                i++;
            }
            
            writer.write(row);
            writer.close();

        } catch (IOException e) {
            System.out.println("can not save compressed image");
        }
    }

    public static ArrayList<ArrayList<Integer>> constructDecompressedImage() {
        codeBookBlockSize = 0;
        System.out.println("Code book file name: ");
        Scanner consoleScanner = new Scanner(System.in);
        String codeBookFile = consoleScanner.nextLine() + ".txt";
        codeBookBlocks = new ArrayList<ArrayList<ArrayList<Double>>>();
        try(Scanner codeBookScanner = new Scanner(new File(codeBookFile))) {
            while (codeBookScanner.hasNextLine()) {
                if (!codeBookScanner.hasNextInt())
                    break;
                codeBookScanner.nextLine();
                ArrayList<ArrayList<Double>> block = new ArrayList<>();
                int i = 0;
                while (codeBookScanner.hasNextDouble() && (codeBookBlockSize == 0 || i < codeBookBlockSize)) {
                    ArrayList<Double> lineDouble = new ArrayList<Double>();
                    String[] line = codeBookScanner.nextLine().split(" ");
                    codeBookBlockSize = line.length;
                    for (String s : line) {
                        lineDouble.add(Double.parseDouble(s));
                    }
                    block.add(lineDouble);
                    ++i;
                }
                codeBookBlocks.add(block);
            }
            codeBookBlockSize = codeBookBlocks.get(0).get(0).size();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        ArrayList<ArrayList<Integer>> image = new ArrayList<ArrayList<Integer>>();
        ArrayList<ArrayList<Integer>> compressedImage = new ArrayList<ArrayList<Integer>>();

        System.out.println("Compressed image file name: ");
        String compressedImageFile = consoleScanner.nextLine() + ".txt";
        try (Scanner compressedImageScanner = new Scanner(new File(compressedImageFile))) {
            while (compressedImageScanner.hasNextLine()) {
                ArrayList<Integer> lineInteger = new ArrayList<>();
                String[] line = compressedImageScanner.nextLine().split(" ");
                for (String s : line) {
                    lineInteger.add(Integer.parseInt(s));
                }
                compressedImage.add(lineInteger);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        imageHeight = compressedImage.size() * codeBookBlockSize;
        imageWidth = compressedImage.get(0).size() * codeBookBlockSize;
        for (int i = 0; i < imageHeight; i++) {
            ArrayList<Integer> line = new ArrayList<>();
            for (int j = 0; j < imageWidth; j++) {
                int blockIndex = compressedImage.get(i / codeBookBlockSize).get(j / codeBookBlockSize);
                line.add((codeBookBlocks.get(blockIndex).get(i % codeBookBlockSize).get(j % codeBookBlockSize)).intValue());
            }
            image.add(line);
        }
        return image;
    }

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        String choice = "";

        while (!"x".equals(choice)) { 

            System.out.println("\nWelcome to Vector Quantization Compression! \n");
            System.out.println("Please select an option: ");
            System.out.println("1. Compress a gray scale image");
            System.out.println("2. Decompress an image using a code book ");
            
            choice = scanner.nextLine();
            
            if(choice.equals("1")) {
                itr = 1;
                codeBookBlocks.clear();
                imageAsBlocksWithIndexes.clear();
                notSameImageBlocksIndexes = true;
                
                System.out.println("Path to image to compress: ");
                String inputImagePath = scanner.nextLine() + ".bmp";

                try {
                ArrayList<ArrayList<Integer>> inputImage = readGrayscaleImage(inputImagePath);
                
                // size of code book block
                System.out.println("Enter Block size: ");
                codeBookBlockSize = Integer.parseInt(scanner.nextLine());
                    
                if(imageWidth % codeBookBlockSize != 0) {
                    System.out.println("can't device " + imageWidth + " into " + codeBookBlockSize + " blocks");
                    continue;
                }

                // size of code book
                System.out.println("Enter Code Book size: ");
                codeBookSize = Integer.parseInt(scanner.nextLine());

                cutImageIntoBlocks(inputImage);

                ArrayList<ArrayList<Double>> firstBlock = getFirstCodeBlock(inputImage);
                codeBookBlocks.add(firstBlock);

                while (codeBookSize != 0) {

                    splitCodeBlock();
                    allocateToCodeBlock();
                    itr++;
                    getNewCodeBlockUsingAvgs();
                    codeBookSize--;
                }
    
                itr--;
                while (notSameImageBlocksIndexes) {
                    getNewCodeBlockUsingAvgs();
                    allocateToCodeBlock();
    
                }

                System.out.println("Save compressed image as: ");
                compileCompressedImage(scanner.nextLine());
                System.out.println("Save code book as: ");
                saveCodeBooksToFile(scanner.nextLine());
                
                System.out.println("\n*** COMPRESSION DONE!! ***\n");

                } catch (IOException ex) {
                    System.out.println("cannot read image, amke sure it is grayscale");
                }
            
            } else if (choice.equals("2")) {
                ArrayList<ArrayList<Integer>> grayscaleValues = constructDecompressedImage();
                System.out.println("Decompressed image name: ");
                writeGrayscaleImage(grayscaleValues, scanner.nextLine()+".bmp");

            } else if ("x".equals(choice)) {
                System.out.println("\nThank you for using our Vector Quantization Compression app!");
                System.out.println("Made with passion by: ");
                System.out.println("\n1. Ziad Mohamed");
                System.out.println("2. Mahmoud khaled\n");
                System.out.println("- Have a nice day! :)\n");

            } else if (!"x".equals(choice)) {
                System.out.println("Please select 1 to compress, 2 to decompress");

            }
        }

        // try {
        //     createTestImage();

        //     ArrayList<ArrayList<Integer>> inputImage = readGrayscaleImage(inputImagePath);
        //     // ArrayList<ArrayList<Double>> outputImage = new ArrayList<ArrayList<Double>>();

        //     // size of code book block
        //     codeBookBlockSize = 2;
        //     // size of code book
        //     codeBookSize = 2;

        //     cutImageIntoBlocks(inputImage);


        //     ArrayList<ArrayList<Double>> firstBlock = getFirstCodeBlock(inputImage);
        //     codeBookBlocks.add(firstBlock);

        //     while (codeBookSize != 0) {

        //         splitCodeBlock();
        //         allocateToCodeBlock();
        //         itr++;
        //         getNewCodeBlockUsingAvgs();
        //         // System.out.println(notSameImageBlocksIndexes);
        //         codeBookSize--;
        //     }

        //     itr--;
        //     // System.out.println(notSameImageBlocksIndexes);
        //     while (notSameImageBlocksIndexes) {
        //         getNewCodeBlockUsingAvgs();
        //         allocateToCodeBlock();

        //     }
        //     // allocateToCodeBlock();

        //     // while (codeBookSize != 0) {

        //     //     splitCodeBlock();
        //     //     allocateToCodeBlock();
        //     //     itr++;
        //     //     if(codeBookSize > 0) {
        //     //         getNewCodeBlockUsingAvgs();
        //     //         // allocateToCodeBlock();
        //     //         // getNewCodeBlockUsingAvgs();
        //     //         // allocateToCodeBlock();
        //     //     }
        //     //     codeBookSize--;
        //     // }
        //     // itr--;
        //     // allocateToCodeBlock();
        //     // getNewCodeBlockUsingAvgs();
        //     // allocateToCodeBlock();
        //     // // allocateToCodeBlock();
        //     // // allocateToCodeBlock();
        //     // // getNewCodeBlockUsingAvgs();
        //     // // allocateToCodeBlock();
        //     // // allocateToCodeBlock();

        //     compileCompressedImage("comressedImage");
        //     saveCodeBooksToFile();


        //     for (ImageBlock imageBlock : imageAsBlocksWithIndexes) {
            
        //         System.out.println("index: " + imageBlock.index);
        //         for (int i = 0; i < codeBookBlockSize; i++) {
        //             for (int j = 0; j < codeBookBlockSize; j++) {
        //                 System.out.print(imageBlock.block.get(i).get(j) + " ");
                        
        //             }
        //             System.out.println();
        //         }
        //         System.out.println();
        //     }
        //     System.out.println();
        //     System.out.println();
        //     System.out.println();

        //     for (ArrayList<ArrayList<Double>> codeBlock : codeBookBlocks) {
        //         for (int i = 0; i < codeBookBlockSize; i++) {
        //             for (int j = 0; j < codeBookBlockSize; j++) {
        //                 System.out.print(codeBlock.get(i).get(j) + " ");
        //             }
        //             System.out.println();
        //         }
        //         System.out.println();
        //     }   
        //     System.out.println(codeBookBlocks.size());


        //     // for (int i = 0; i < 2; i++) {
        //     //     for (int j = 0; j < 2; j++) {
                    
        //     //         System.out.print(firstBlock.get(i).get(j) + " ");
        //     //     }
        //     //     System.out.println();
        //     // }

        // } catch (IOException e) {
        //     System.err.println("Error processing the image: " + e.getMessage());
        // }
    }
}