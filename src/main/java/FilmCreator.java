import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.System.out;

public class FilmCreator {

    private static final Logger L = LoggerFactory.getLogger(FilmCreator.class);

    // standard image size from phone
    public static final int IMAGE_WIDTH_IN_PIXELS = 3024;
    public static final int IMAGE_HEIGHT_IN_PIXELS = 4032;

    public static final int IMAGE_RESOLUTION_PPI = 75;
    public static final double IMAGE_WIDTH_IN_INCHES = IMAGE_WIDTH_IN_PIXELS / IMAGE_RESOLUTION_PPI;
    public static final double IMAGE_HEIGHT_IN_INCHES = IMAGE_HEIGHT_IN_PIXELS / IMAGE_RESOLUTION_PPI;

    public static final int CANVAS_RESOLUTION_PPI = 75;
    public static final int CANVAS_WIDTH_IN_PIXELS = IMAGE_HEIGHT_IN_PIXELS * 6;
    public static final int CANVAS_HEIGHT_IN_PIXELS = CANVAS_WIDTH_IN_PIXELS;
    public static final double CANVAS_WIDTH_IN_INCHES = CANVAS_WIDTH_IN_PIXELS / CANVAS_RESOLUTION_PPI;
    public static final double CANVAS_HEIGHT_IN_INCHES = CANVAS_HEIGHT_IN_PIXELS / CANVAS_RESOLUTION_PPI;

    public static final double PRINT_WIDTH_IN_INCHES = 8.5;
    public static final double PRINT_HEIGHT_IN_INCHES = 11;

    public static final double REDUCTION_FACTOR = CANVAS_WIDTH_IN_INCHES / PRINT_WIDTH_IN_INCHES;
    public static final double REDUCION_PERCENTAGE = 1 - (PRINT_WIDTH_IN_INCHES / CANVAS_WIDTH_IN_INCHES);

    public static final double FRAME_WIDTH_IN_INCHES = IMAGE_WIDTH_IN_INCHES * (1 - REDUCION_PERCENTAGE);
    public static final double FRAME_HEIGHT_IN_INCHES = IMAGE_HEIGHT_IN_INCHES * (1 - REDUCION_PERCENTAGE);

    public static final double PRINT_TARGET_PPI = CANVAS_RESOLUTION_PPI * REDUCTION_FACTOR;

//    public static final double φ = (1 + Math.sqrt(5)) / 2;

    public FilmCreator() {
    }

    public final void createFilm(String filmName) throws Exception {

        dumpParameters();

        L.info("Creating film {}...", filmName);

        File dir = Paths.get("").toAbsolutePath().toFile();
        File filmsDir = new File(dir, "films");
        File filmDir = new File(filmsDir, filmName);
        if (!filmDir.exists())
            throw new Exception("FilmCreator directory " + filmDir.getPath() + " not found!");

        File imagesDir = new File(filmDir, "images");
        if (!imagesDir.exists())
            throw new Exception("No images! Please put some images in " + imagesDir.getPath());

        // create frames from images
        File frameseDir = new File(filmDir, "frames");
        frameseDir.mkdirs();
        FileUtils.cleanDirectory(frameseDir); // delete any existing frames
        File[] images = imagesDir.listFiles();
        for (int i = 0; i < images.length; i++) {
            createFrame(images[i], i, frameseDir);
        }

        // create film wheel from frames
        File[] frames = frameseDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jpeg");
            }
        });
        createFilmWheel(filmName, filmDir, frames);
        L.info("DONE");
    }

    private final void dumpParameters() {
        List<Field> fields = Arrays.stream(getClass().getDeclaredFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()) && !f.getName().matches("L"))
                .collect(Collectors.toList());
        Collections.sort(fields, Comparator.comparing(Field::getName));
        fields.forEach(f -> {
            try {
                out.println(StringUtils.rightPad(f.getName(), 24) + ": " + f.get(null));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void createFilmWheel(String filmName, File filmDir, File[] frames) throws Exception {

        int offset = CANVAS_HEIGHT_IN_PIXELS / 2;
        BufferedImage canvas = new BufferedImage(CANVAS_HEIGHT_IN_PIXELS, CANVAS_HEIGHT_IN_PIXELS, BufferedImage.TYPE_3BYTE_BGR);

        int canvasHeight = canvas.getHeight();
        int canvasWidth = canvas.getWidth();

        double horizon = canvasHeight / 2.0;
        double midline = canvasWidth / 2.0;
//        L.debug("horizon:[{}] midline:[{}]", horizon, midline);

        Graphics2D g = canvas.createGraphics();

        // draw x(blue)/y(red) axes
        g.setStroke(new BasicStroke(10));
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, canvasWidth, canvasHeight);
        g.setColor(Color.BLUE);
        g.drawLine(0, canvasHeight - offset, canvasWidth, canvasHeight - offset);
        g.setColor(Color.RED);
        g.drawLine(canvasWidth - offset, 0, canvasWidth - offset, canvasHeight);

        for (int i = 0; i < frames.length; i++) {
            File frameFile = frames[i];
            BufferedImage frame = ImageIO.read(frameFile);

            double halfFrameHeight = frame.getHeight() / 2.0;
            double halfFrameWidth = frame.getWidth() / 2.0;
            double rotationRequired = Math.toRadians(i * 22.5);
            AffineTransform a = AffineTransform.getRotateInstance(rotationRequired, horizon, midline);

            g.setTransform(a);

            double x = midline - halfFrameWidth;
            int topMargin = 400; // most top padding we can give before the bottom of the frames intersect
            g.drawImage(frame, (int) x, topMargin, null);
        }
        g.dispose();

        String format = "JPEG";
        File filmWheelFile = new File(filmDir, filmName + "." + format.toLowerCase());

        ImageIO.write(canvas, format, filmWheelFile);
        L.debug("Created film wheel {}", filmWheelFile.getAbsoluteFile());
    }

    private final void createFrame(File imageFile, int frameId, File frameseDir) throws IOException {

        BufferedImage image = ImageIO.read(imageFile);
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        int newWidth = originalHeight;
        int newHeight = originalWidth;

//        L.debug("frame {} dimensions originalWidth:[{}] newWidth:[{}] originalHeight:[{}] newHeight:[{}]",
//                frameId, originalWidth, newWidth, originalHeight, newHeight);

        int type = ((image.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : image.getType());
        BufferedImage frameImage = new BufferedImage(newWidth, newHeight, type);

        Graphics2D g2d = frameImage.createGraphics();

        double rotationRequired = Math.toRadians(90);
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - originalWidth) / 2, (newHeight - originalHeight) / 2);
        at.rotate(rotationRequired, originalWidth / 2, originalHeight / 2);
        g2d.setTransform(at);

        g2d.setComposite(AlphaComposite.Src);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(image, 0, 0, null);

        g2d.dispose();

        String format = "JPEG";
        File frameFile = new File(frameseDir, frameId + "." + format.toLowerCase());
        ImageIO.write(frameImage, format, frameFile);
        L.debug("Created frame {} from image {}", frameId, imageFile.getName());
    }

    public static final void main(String[] args) throws Exception {
        String filmName = args[0];
        FilmCreator filmCreator = new FilmCreator();
        filmCreator.createFilm(filmName);
    }
}
