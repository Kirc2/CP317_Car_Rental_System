package main.java.com.carrental.gui;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import main.java.com.carrental.gui.Login_RegisterEndpoints.LoginHandler;
import main.java.com.carrental.gui.Login_RegisterEndpoints.RegisterHandler;
import main.java.com.carrental.gui.RentalHistoryEndpoints.CancelReservation;
import main.java.com.carrental.gui.RentalHistoryEndpoints.ListAllVehicles;
import main.java.com.carrental.gui.RentalHistoryEndpoints.VehicleInfo;
import main.java.com.carrental.gui.ReservationHandler.GETSVehicleInfo;
import main.java.com.carrental.gui.ReservationHandler.Reserve;

/**
 * Handles the frontend to backend endpoints, it spins up a server off of port 8080
 * then listens to call from the web page. If it recieves any GET or POST then
 * the class will handle them all
 */
public class CarRentalServer {

	/**
	 * Create Http server to run the web application first directs the Static web page
	 * to login.html that can be accessed in the /public folder
	 * 
	 */
	public CarRentalServer () {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
			server.createContext("/", new StaticFileHandler("public"));
			// ENDPOINT AT login.html
			server.createContext("/login", new LoginHandler());
			// ENDPOINT AT login.html
			server.createContext("/register", new RegisterHandler());
			// ENDPOINT AT Editprofile.html
			server.createContext("/edit_profile", new ProfileEndpoints());
			// ENDPOINT AT History.html
			server.createContext("/history", new ListAllVehicles());
			// ENPOINT AT Rental_Details.html
			server.createContext("/rentalhistory", new VehicleInfo());
			// ENDPOINT AT Payment.html
			server.createContext("/payment", new PaymentEndpoints());
			// ENDPOINT AT Rentals.html
			server.createContext("/rentals", new VehicleSearchEndpoints());
			// ENDPOINT AT Reservation.html
			server.createContext("/getvehicleinfo", new GETSVehicleInfo());
			// ENDPOINT AT Reservations.html
			server.createContext("/reserve", new Reserve());
			// ENDPOINT AT Rental_Details.html
			server.createContext("/cancel", new CancelReservation());

			server.setExecutor(null);
			server.start();
			System.out.println("Web server started, access page at : http://localhost:8080");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}


