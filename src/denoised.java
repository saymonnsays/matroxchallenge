import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.opencv.videoio.VideoCapture;
import javax.imageio.ImageIO;

public class denoised {

    private static VideoCapture videoCapture;
    private static JFrame videoFrame;
    private static Timer timer;

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.setProperty("ffmpeg", "C:\\ProgramData\\chocolatey\\bin\\ffmpeg.exe");

        videoCapture = new VideoCapture("C:\\Users\\Saymo\\IdeaProjects\\wattt\\src\\codejam_matrox_2023_noisy.mp4");

        if (!videoCapture.isOpened()) {
            System.out.println("Error: Could not open video capture.");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            videoFrame = new JFrame("Denoised Video");
            videoFrame.setLayout(new BorderLayout());
            videoFrame.setSize(800, 600);
            videoFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            videoFrame.setVisible(true);
        });

        Mat frameMat = new Mat();
        Mat denoisedFrame = new Mat();
        MatOfByte matOfByte = new MatOfByte();

        timer = new Timer(1, e -> {
            if (videoCapture.read(frameMat)) {
                Imgproc.medianBlur(frameMat, denoisedFrame, 3);

                Imgcodecs.imencode(".bmp", denoisedFrame, matOfByte);
                byte[] byteArray = matOfByte.toArray();
                InputStream in = new ByteArrayInputStream(byteArray);
                BufferedImage bufImage = null;
                try {
                    bufImage = ImageIO.read(in);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                displayDenoisedVideoGUI(bufImage);
            } else {
                videoCapture.release();
                timer.stop();
                SwingUtilities.invokeLater(() -> {
                    videoFrame.dispose();
                });
            }
        });

        timer.start();
    }

    private static void displayDenoisedVideoGUI(BufferedImage image) {
        SwingUtilities.invokeLater(() -> {
            if (videoFrame != null) {
                JLabel label = new JLabel(new ImageIcon(image));
                videoFrame.setContentPane(label);
                videoFrame.revalidate();
                videoFrame.repaint();
            }
        });
    }
}
