import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Scanner;
import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class WeatherApp {

    private static final String CONFIG_FILE_PATH = ".\\credentials.txt";
    private static JFrame frame;
    private static JPanel panel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        frame = new JFrame("GeoForecast");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		
        panel = new JPanel();

        // Create the menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu aboutMenu = new JMenu("About");
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem exitItem = new JMenuItem("Exit");
        JMenuItem helpItem = new JMenuItem("Help");
		JMenuItem creatorItem = new JMenuItem("About Developers");

        // Add menu items to the file menu
        fileMenu.add(newItem);
        fileMenu.add(exitItem);

        // Add menu items to the about menu
        aboutMenu.add(helpItem);
		aboutMenu.add(creatorItem);

        // ActionListener for the "New" menu item
        newItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Prompt the user to enter the name of a city
                String cityName = JOptionPane.showInputDialog(frame, "Enter the name of a city:");
                if (cityName != null && !isNumeric(cityName)) {
                    try {
                        // Fetch weather information for the entered city
                        String weatherInfo = getWeatherInfo(cityName);
                        // Display the weather information using JOptionPane
                        displayWeatherInfo(weatherInfo);
                    } catch (IOException | URISyntaxException ex) {
                        // Show an error message if an error occurs
                        JOptionPane.showMessageDialog(frame, "An error occurred: " + ex.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // Show an error message for invalid input
                    JOptionPane.showMessageDialog(frame, "Invalid input! Please try again!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ActionListener for the "Exit" menu item
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Exit the program
                System.exit(0);
            }
        });

        // ActionListener for the "Help" menu item
        helpItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Show a help message
                JOptionPane.showMessageDialog(frame, "Click on File and then on New to enter the name of a city and obtain it's weather info.", "Help",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
		
		creatorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Show info about developers
                JOptionPane.showMessageDialog(frame, "This application was developed by Group 3!", "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Add menu items to the menu bar
        menuBar.add(fileMenu);
        menuBar.add(aboutMenu);

        // Set the menu bar in the frame
        frame.setJMenuBar(menuBar);
        // Add the panel to the frame
        frame.getContentPane().add(panel);

        // Set frame size to 1/4th of the screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width / 4;
        int height = screenSize.height / 4;
        frame.setSize(width, height);

        // Set frame location at the center of the screen
        int x = (screenSize.width - width) / 2;
        int y = (screenSize.height - height) / 2;
        frame.setLocation(x, y);

        // Make the frame visible
        frame.setVisible(true);
    }

    private static String getWeatherInfo(String cityName) throws IOException, URISyntaxException {
        Properties properties = loadConfigProperties();

        String apiKey = properties.getProperty("API_KEY");
        String apiUrl = properties.getProperty("API_URL");

        String formattedApiUrl = String.format(apiUrl, cityName, apiKey);

        URI uri = new URI(formattedApiUrl);

        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Scanner sc = new Scanner(connection.getInputStream());
            StringBuilder response = new StringBuilder();
            while (sc.hasNextLine()) {
                response.append(sc.nextLine());
            }
            sc.close();
            return response.toString();
		} else {
            return "Error: " + responseCode;
        }
    }

    private static void displayWeatherInfo(String weatherInfo) {
        JSONObject jsonObject = new JSONObject(weatherInfo);

        String cityName = jsonObject.getString("name");
        String description = "";
        if (jsonObject.has("weather")) {
            JSONArray weatherArray = jsonObject.getJSONArray("weather");
            if (weatherArray.length() > 0) {
                JSONObject weatherObj = weatherArray.getJSONObject(0);
                description = weatherObj.getString("description");
            }
        }

        double temperature = 0;
        double humidity = 0;
        double windSpeed = 0;
        if (jsonObject.has("main")) {
            JSONObject mainObj = jsonObject.getJSONObject("main");
            temperature = mainObj.getDouble("temp");
            humidity = mainObj.getDouble("humidity");
        }
        if (jsonObject.has("wind")) {
            JSONObject windObj = jsonObject.getJSONObject("wind");
            windSpeed = windObj.getDouble("speed");
        }

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String formattedTemperature = decimalFormat.format(temperature - 273.15);

        String message = "City: " + cityName + "\n" +
                "Description: " + description + "\n" +
                "Temperature: " + formattedTemperature + " C" + "\n" +
                "Humidity: " + decimalFormat.format(humidity) + " %" + "\n" +
                "Wind Speed: " + decimalFormat.format(windSpeed) + " m/s";

        JOptionPane.showMessageDialog(frame, message, "Weather Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private static Properties loadConfigProperties() throws IOException {
        Properties properties = new Properties();
        FileInputStream fileInputStream = new FileInputStream(CONFIG_FILE_PATH);
        properties.load(fileInputStream);
        fileInputStream.close();
        return properties;
    }

    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
