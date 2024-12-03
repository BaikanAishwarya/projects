import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/")
public class HotelReviewSystem extends HttpServlet {
    private static final String URL = "jdbc:mysql://localhost:3306/HotelReviewDB";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private List<Review> getReviews(String hotelName) {
        List<Review> reviews = new ArrayList<>();
        String query = "SELECT * FROM reviews WHERE hotel_name = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, hotelName);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                reviews.add(new Review(
                        resultSet.getInt("id"),
                        resultSet.getString("hotel_name"),
                        resultSet.getString("user"),
                        resultSet.getInt("rating"),
                        resultSet.getString("comment")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reviews;
    }

    private void addReview(Review review) {
        String query = "INSERT INTO reviews (hotel_name, user, rating, comment) VALUES (?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, review.getHotelName());
            statement.setString(2, review.getUser());
            statement.setInt(3, review.getRating());
            statement.setString(4, review.getComment());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getServletPath();

        switch (action) {
            case "/addReview":
                showAddReviewForm(request, response);
                break;
            case "/insertReview":
                insertReview(request, response);
                break;
            case "/reviews":
                listReviews(request, response);
                break;
            default:
                listHotels(request, response);
                break;
        }
    }

    private void listHotels(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("hotels.jsp").forward(request, response);
    }

    private void showAddReviewForm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("addReview.jsp").forward(request, response);
    }

    private void insertReview(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String hotelName = request.getParameter("hotelName");
        String user = request.getParameter("user");
        int rating = Integer.parseInt(request.getParameter("rating"));
        String comment = request.getParameter("comment");

        Review review = new Review(0, hotelName, user, rating, comment);
        addReview(review);

        response.sendRedirect("reviews?hotelName=" + hotelName);
    }

    private void listReviews(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String hotelName = request.getParameter("hotelName");
        String sortBy = request.getParameter("sortBy");
        String minRatingParam = request.getParameter("minRating");
        int minRating = minRatingParam != null ? Integer.parseInt(minRatingParam) : 0;

        List<Review> reviews = getReviews(hotelName);
        if ("rating".equals(sortBy)) {
            reviews.sort((r1, r2) -> Integer.compare(r2.getRating(), r1.getRating()));
        }

        if (minRating > 0) {
            reviews = reviews.stream()
                    .filter(review -> review.getRating() >= minRating)
                    .collect(Collectors.toList());
        }

        request.setAttribute("reviews", reviews);
        request.setAttribute("hotelName", hotelName);
        request.getRequestDispatcher("reviews.jsp").forward(request, response);
    }
}

class Review {
    private int id;
    private String hotelName;
    private String user;
    private int rating;
    private String comment;

    public Review(int id, String hotelName, String user, int rating, String comment) {
        this.id = id;
        this.hotelName = hotelName;
        this.user = user;
        this.rating = rating;
        this.comment = comment;
    }

    public int getId() {
        return id;
    }

    public String getHotelName() {
        return hotelName;
    }

    public String getUser() {
        return user;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    // Getters and setters
}
