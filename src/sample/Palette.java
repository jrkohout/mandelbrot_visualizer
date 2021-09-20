package sample;

import javafx.scene.paint.Color;

public class Palette {

    // todo implement file support with palettes (save custom palettes to a file, generate palettes from a file)
    // todo hues

    public static Color[] get(String paletteName, int size) {
        Color[] palette = new Color[size];
        switch (paletteName) {
            case "palette1":
                return defaultPalette(palette);
            case "grayscale":
                return grayScale(palette);
            case "reverse_grayscale":
                return reverseGrayScale(palette);
            case "warm":
                return warm(palette);
            case "reverse_warm":
                return reverseWarm(palette);
            case "pastel":
                return pastel(palette);
            default:
                return grayScale(palette);
        }
    }

    private static Color[] defaultPalette(Color[] palette) {
        int red;
        int green;
        int blue;
        double  sectionLength = palette.length / 4.0;
        for (int i = 0; i < palette.length; i++) {
            if (i < sectionLength) {
                red = 255;
                green = (int) (256 * i / sectionLength);
                blue = 0;
            } else if (i < 2 * sectionLength) {
                red = 255 - (int) (256 * (i - sectionLength) / sectionLength);
                green = 255;
                blue = 0;
            } else if (i < 3 * sectionLength) {
                red = 0;
                green = 255;
                blue = (int) (256 * (i - 2 * sectionLength) / sectionLength);
            } else {
                red = 0;
                green = 255 - (int) (256 * (i - 3 * sectionLength) / sectionLength);
                blue = 255;
            }
            //System.out.printf("Iteration: %d, red=%d, green=%d, blue=%d%n", i, red, green, blue);
            palette[palette.length - i - 1] = Color.rgb(red, green, blue);
        }
        palette[palette.length - 1] = Color.BLACK;
        return palette;
    }

    private static Color[] grayScale(Color[] palette) {
        for (int i = 0; i < palette.length; i++) {
            palette[palette.length - i - 1] = Color.gray((double) i / palette.length);
        }
        return palette;
    }

    private static Color[] reverseGrayScale(Color[] palette) {
        for (int i = 0; i < palette.length; i++) {
            palette[i] = Color.gray((double) i / palette.length);
        }
        return palette;
    }

    private static Color[] warm(Color[] palette) {
        int green;
        for (int i = 0; i < palette.length; i++) {
            green = (int)((double) i / palette.length * 255);
            palette[i] = Color.rgb(255, green, 0);
        }
        return palette;
    }

    private static Color[] reverseWarm(Color[] palette) {
        int green;
        for (int i = 0; i < palette.length; i++) {
            green = (int)((double) i / palette.length * 255);
            palette[palette.length - i - 1] = Color.rgb(255, green, 0);
        }
        return palette;
    }

    private static Color[] pastel(Color[] palette) {
        int red;
        int green;
        int blue;
        double sectionLength = palette.length / 3.0;
        for (int i = 0; i < palette.length; i++) {
            if (i < sectionLength) {
                red = 255;
                // 76 = 256 - 179
                green = 179 + (int) (77 * i / sectionLength);
                blue = 186;
            } else if (i < 2 * sectionLength) {
                red = 255 - (int) (70 * (i - sectionLength) / sectionLength);
                green = 255;
                blue = 186 + (int) (16 * (i - sectionLength) / sectionLength);
            } else {
                red = 186;
                green = 255;
                blue = 201 + (int) (55 * (i - 2 * sectionLength) / sectionLength);
            }
            //System.out.printf("Iteration: %d, red=%d, green=%d, blue=%d%n", i, red, green, blue);
            palette[palette.length - i - 1] = Color.rgb(red, green, blue);
        }
        //palette[palette.length - 1] = Color.BLACK;
        return palette;
    }
}
