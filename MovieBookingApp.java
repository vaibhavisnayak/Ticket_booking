import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class MovieBookingApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginSwingApp().createLoginWindow());
    }
}

class LoginSwingApp {
    public void createLoginWindow() {
        JFrame loginFrame = new JFrame("Login");
        JTextField usernameField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JTextField phoneField = new JTextField(10);
        JButton loginButton = new JButton("Login");

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String phone = phoneField.getText().trim();

            if (username.length() < 3 || password.length() < 3) {
                JOptionPane.showMessageDialog(loginFrame, "Username and password must be at least 3 characters.");
                return;
            }

            if (!phone.matches("\\d{10}")) {
                JOptionPane.showMessageDialog(loginFrame, "Phone number must be exactly 10 digits.");
                return;
            }

            if (validateLogin(username, password)) {
                loginFrame.dispose();
                new MovieSelectionFrame(username, phone);
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Invalid credentials. Try again.");
            }
        });

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Username:")); panel.add(usernameField);
        panel.add(new JLabel("Password:")); panel.add(passwordField);
        panel.add(new JLabel("Phone:")); panel.add(phoneField);
        panel.add(new JLabel()); panel.add(loginButton);

        loginFrame.add(panel);
        loginFrame.pack();
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);
    }

    private boolean validateLogin(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader("users.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split(",");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading users.csv file.");
        }
        return false;
    }
}

class MovieSelectionFrame extends JFrame {
    private String username, phone;
    private JComboBox<String> movieSelect;
    private JComboBox<String> showtimeSelect;
    private JTextArea movieDetailsArea;
    private JButton selectSeatsButton;

    private Map<String, Movie> movies = new LinkedHashMap<>();

    public MovieSelectionFrame(String username, String phone) {
        this.username = username;
        this.phone = phone;
        setTitle("Movie Selection");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        movies.put("Kantara", new Movie("Kantara", "Action thriller", new String[]{"11:00 AM", "2:00 PM"}, "Manipal Multiplex"));
        movies.put("Interstellar", new Movie("Interstellar", "Sci-fi space epic", new String[]{"1:00 PM", "4:00 PM"}, "Galaxy Theater"));
        movies.put("Tangled", new Movie("Tangled", "Animated fairy tale adventure", new String[]{"10:00 AM", "1:00 PM"}, "Sunshine Cinema"));
        movies.put("Inception", new Movie("Inception", "Mind-bending thriller", new String[]{"3:00 PM", "6:00 PM"}, "Dreamscape Theater"));

        movieSelect = new JComboBox<>(movies.keySet().toArray(new String[0]));
        showtimeSelect = new JComboBox<>();
        movieDetailsArea = new JTextArea(10, 40);
        movieDetailsArea.setEditable(false);
        selectSeatsButton = new JButton("Select Seats");

        movieSelect.addActionListener(e -> updateDetails());
        selectSeatsButton.addActionListener(e -> openSeatSelection());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Movie:"));
        topPanel.add(movieSelect);
        topPanel.add(new JLabel("Showtime:"));
        topPanel.add(showtimeSelect);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(movieDetailsArea), BorderLayout.CENTER);
        add(selectSeatsButton, BorderLayout.SOUTH);

        updateDetails();
        setVisible(true);
    }

    private void updateDetails() {
        String name = (String) movieSelect.getSelectedItem();
        Movie movie = movies.get(name);
        movieDetailsArea.setText("Title: " + movie.name +
                "\nDescription: " + movie.description +
                "\nTheater: " + movie.theater +
                "\nShowtimes: " + String.join(", ", movie.showtimes));
        showtimeSelect.removeAllItems();
        for (String time : movie.showtimes) showtimeSelect.addItem(time);
    }

    private void openSeatSelection() {
        String movieName = (String) movieSelect.getSelectedItem();
        String showtime = (String) showtimeSelect.getSelectedItem();
        new SeatSelectionFrame(username, phone, movies.get(movieName), showtime);
        dispose();
    }
}

class Movie {
    String name, description, theater;
    String[] showtimes;
    Map<String, boolean[]> bookedSeats = new HashMap<>();

    public Movie(String name, String description, String[] showtimes, String theater) {
        this.name = name;
        this.description = description;
        this.showtimes = showtimes;
        this.theater = theater;
    }
}

class SeatSelectionFrame extends JFrame {
    private Set<String> selectedSeats = new HashSet<>();

    public SeatSelectionFrame(String username, String phone, Movie movie, String showtime) {
        setTitle("Select Seats for " + movie.name + " - " + showtime);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel seatPanel = new JPanel(new GridLayout(5, 5, 5, 5)); // 5x5 seat layout

        for (int i = 1; i <= 25; i++) {
            String seatNo = "S" + i;
            JButton seatButton = new JButton(seatNo);
            seatButton.setBackground(Color.LIGHT_GRAY);
            seatButton.addActionListener(e -> {
                if (selectedSeats.contains(seatNo)) {
                    selectedSeats.remove(seatNo);
                    seatButton.setBackground(Color.LIGHT_GRAY);
                } else {
                    selectedSeats.add(seatNo);
                    seatButton.setBackground(Color.GREEN);
                }
            });
            seatPanel.add(seatButton);
        }

        JButton confirmButton = new JButton("Confirm Booking");
        confirmButton.addActionListener(e -> {
            if (selectedSeats.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select at least one seat.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Booking Confirmed!\nUser: " + username +
                        "\nPhone: " + phone +
                        "\nMovie: " + movie.name +
                        "\nTheater: " + movie.theater +
                        "\nShowtime: " + showtime +
                        "\nSeats: " + String.join(", ", selectedSeats));
                dispose();
            }
        });

        add(new JLabel("Click on seats to select. Green = Selected"), BorderLayout.NORTH);
        add(seatPanel, BorderLayout.CENTER);
        add(confirmButton, BorderLayout.SOUTH);

        setVisible(true);
    }
}
