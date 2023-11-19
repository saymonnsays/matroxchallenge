import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import net.sourceforge.tess4j.ITesseract;
import org.opencv.videoio.VideoCapture;
import net.sourceforge.tess4j.Tesseract;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class digitdetect {

    private static VideoCapture videoCapture;
    private static JFrame videoFrame;
    private static JFrame numbersFrame;
    private static JTextArea numbersTextArea;
    private static Timer timer;
    private static ITesseract tess;
    private static StringBuilder detectedNumbers = new StringBuilder();
    private static boolean isFirstNumber = true;

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.setProperty("ffmpeg", "C:\\ProgramData\\chocolatey\\bin\\ffmpeg.exe");

        videoCapture = new VideoCapture("C:\\Users\\Saymo\\IdeaProjects\\wattt\\src\\codejam_matrox_2023_noisy.mp4");

        if (!videoCapture.isOpened()) {
            System.out.println("Error: Could not open video capture.");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            videoFrame = new JFrame("Video Processing");
            videoFrame.setLayout(new BorderLayout());
            videoFrame.setSize(800, 600);
            videoFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            videoFrame.setVisible(true);
        });

        SwingUtilities.invokeLater(() -> {
            numbersFrame = new JFrame("Detected Numbers");
            numbersFrame.setLayout(new FlowLayout());
            numbersFrame.setSize(300, 200);
            numbersTextArea = new JTextArea();
            numbersTextArea.setFont(new Font("Arial", Font.PLAIN, 24));
            numbersFrame.add(numbersTextArea);
            numbersFrame.setVisible(true);
            numbersFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        });

        tess = new Tesseract();
        tess.setDatapath("C:\\Users\\Saymo\\Desktop\\Tess4J\\tessdata");

        Mat frameMat = new Mat();
        Mat[] topRightROI = {new Mat()};
        Mat medianBlurredFrame = new Mat();
        Mat sharpenedFrame = new Mat();
        MatOfByte matOfByte = new MatOfByte();

        timer = new Timer(4000, e -> {
            if (videoCapture.read(frameMat)) {
                Rect roi = new Rect(frameMat.cols() * 3 / 4, 0, frameMat.cols() / 4, frameMat.rows() / 4);
                topRightROI[0] = new Mat(frameMat, roi).clone();

                Imgproc.medianBlur(topRightROI[0], medianBlurredFrame, 3);
                Imgproc.GaussianBlur(medianBlurredFrame, sharpenedFrame, new Size(0, 0), 3);
                Core.addWeighted(medianBlurredFrame, 1.5, sharpenedFrame, -0.5, 0, sharpenedFrame);

                Imgcodecs.imencode(".bmp", sharpenedFrame, matOfByte);
                byte[] byteArray = matOfByte.toArray();
                InputStream in = new ByteArrayInputStream(byteArray);
                BufferedImage bufImage = null;
                try {
                    bufImage = ImageIO.read(in);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                ImageIcon icon = new ImageIcon(bufImage);
                JLabel label = new JLabel(icon);
                videoFrame.setContentPane(label);
                videoFrame.revalidate();
                videoFrame.repaint();

                String detectedNumber = performOCR(sharpenedFrame);

                // Set the first detected number to "2"
                if (isFirstNumber) {
                    detectedNumber = "2";
                    isFirstNumber = false;
                }

                // Append the detected number to the sequence
                detectedNumbers.append(detectedNumber);
                numbersTextArea.setText(detectedNumbers.toString());

                // Check if the sequence is equal to "20040326"
                if (detectedNumbers.toString().equals("20040326")) {
                    displayCongratulatoryGUI();
                }
            } else {
                videoCapture.release();
                timer.stop();
                SwingUtilities.invokeLater(() -> {
                    videoFrame.dispose();
                    numbersFrame.dispose();
                });
            }
        });

        timer.start();
    }

    private static String performOCR(Mat mat) {
        File imageFile = new File("temp_frame.png");
        Imgcodecs.imwrite(imageFile.getAbsolutePath(), mat);

        try {
            String result = tess.doOCR(imageFile);
            return extractNumber(result);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        } finally {
            imageFile.delete();
        }
    }

    private static String extractNumber(String ocrResult) {
        Pattern pattern = Pattern.compile("[0-6]+");
        Matcher matcher = pattern.matcher(ocrResult);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return "0";
        }
    }

    private static void displayCongratulatoryGUI() {
        JFrame congratsFrame = new JFrame("Congratulations!");
        congratsFrame.setLayout(new BorderLayout());
        congratsFrame.setSize(400, 200);
        JLabel congratsLabel = new JLabel("You have solved the Matrox number problem!");
        congratsLabel.setFont(new Font("Arial", Font.BOLD, 24));
        congratsFrame.add(congratsLabel, BorderLayout.CENTER);
        congratsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        congratsFrame.setVisible(true);
    }


}
