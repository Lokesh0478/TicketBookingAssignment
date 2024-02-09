package com.assignment.demo.rest.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.assignment.demo.dto.TicketPurchaseRequest;
import com.assignment.demo.entity.User;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

	private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

	private final Map<String, User> users = new HashMap<>();
	private final Map<String, String> seatAllocation = new HashMap<>();
	private final Map<String, Integer> seatMaintenence = new HashMap<>();
	private final Map<String, ArrayList<Integer>> releasedSeats = new HashMap<>();
	private final Map<String, ArrayList<Integer>> AlreadyBookedSeats = new HashMap<>();

	@PostMapping("/purchase")
	public ResponseEntity<String> purchaseTicket(@RequestBody TicketPurchaseRequest request) {
		String userId = request.getUser().getFirstName() + request.getUser().getLastName();
		users.put(userId, request.getUser());

		String seat = assignSeat(userId, request.getSection());
		seatAllocation.put(userId, seat);

		logger.info("Ticket Purchased for user {}. seat assigned: {}", userId, seat);

		return ResponseEntity.ok("Ticket purchased successfully. Receipt details:\n" +
				"From: " + request.getFrom() + "\n" +
				"To: " + request.getTo() + "\n" +
				"User: " + request.getUser().getFirstName() + " " + request.getUser().getLastName() + "\n" +
				"Price Paid: $20\n" +
				"Seat Allocated: " + seat);
	}

	@GetMapping("/receipt/{userId}")
	public ResponseEntity<String> getReceipt(@PathVariable String userId) {
		User user = users.get(userId);
		if (user == null) {
			return ResponseEntity.notFound().build();
		}

		String seat = seatAllocation.get(userId);

		logger.info("receipt fetched for user {}", userId);

		return ResponseEntity.ok("Receipt details:\n" +
				"From: London\n" +
				"To: France\n" +
				"User: " + user.getFirstName() + " " + user.getLastName() + "\n" +
				"Price Paid: $20\n" +
				"Seat Allocated: " + seat);
	}

	@GetMapping("/users")
	public ResponseEntity<Map<String, String>> getUsersBySection(@RequestParam String section) {
		Map<String, String> usersWithSeats = new HashMap<>();

		for (Map.Entry<String, String> entry : seatAllocation.entrySet()) {
			if (entry.getValue().contains(section)) {
				usersWithSeats.put(entry.getKey(), entry.getValue());
			}
		}

		logger.info("List of users fetched for section {}. List: {}", section, usersWithSeats);

		return ResponseEntity.ok(usersWithSeats);
	}

	@DeleteMapping("/remove/{userId}")
	public ResponseEntity<String> removeUser(@PathVariable String userId) {
		if (users.containsKey(userId)) {
			String removedSeat = seatAllocation.remove(userId);
			users.remove(userId);
			logger.info("seat removed from seatAllocation");
			String[] seat = removedSeat.split("-");
			if(releasedSeats.containsKey(seat[0])) {
				ArrayList<Integer> al = releasedSeats.get(seat[0]);
				al.add(Integer.valueOf(seat[1]));
				releasedSeats.put(seat[0], al);
				logger.info("removed seat added to releasedSeats list. updated list: {}", al);
			} else {
				ArrayList<Integer> al = new ArrayList<>();
				al.add(Integer.valueOf(seat[1]));
				releasedSeats.put(seat[0], al);
				logger.info("removed seat added to releasedSeats. updated list: {}", al);
			}
			if(AlreadyBookedSeats.containsKey(seat[0])) {
				ArrayList<Integer> al = AlreadyBookedSeats.get(seat[0]);
				if(al != null && al.size() > 0 && al.contains(Integer.valueOf(seat[1]))) {
					al.remove(Integer.valueOf(seat[1]));
					logger.info("removed seat removed from AlreadyBookedSeats, updated list: {}", al);
					AlreadyBookedSeats.put(seat[0], al);
				}
			}
			return ResponseEntity.ok("User with ID " + userId + " removed. Seat " + removedSeat + " released.");
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@PutMapping("/modify-seat/{userId}")
	public ResponseEntity<String> modifyUserSeat(@PathVariable String userId, @RequestParam String newSeat) {

		String[] seat1 = newSeat.split("-");
		if(seat1.length != 2 || !seat1[1].matches("-?\\d+")) {
			return ResponseEntity.badRequest().body("Please provide seat in the format section-seatNumber");
		}

		if(seatAllocation.containsValue(newSeat)) {
			return ResponseEntity.badRequest().body("Seat is already allocated to different user. "
					+ "Please retry with different seat");
		}

		if (users.containsKey(userId) && seatAllocation.containsKey(userId)) {
			String removedSeat = seatAllocation.get(userId);
			String[] seat = removedSeat.split("-");
			if(releasedSeats.containsKey(seat[0])) {
				ArrayList<Integer> al = releasedSeats.get(seat[0]);
				al.add(Integer.valueOf(seat[1]));
				releasedSeats.put(seat[0], al);
				logger.info("old seat {} added to releasedSeats list. updated list: {}", seat[1], al);
			} else {
				ArrayList<Integer> al = new ArrayList<>();
				al.add(Integer.valueOf(seat[1]));
				releasedSeats.put(seat[0], al);
				logger.info("old seat {} added to releasedSeats. new list: {}", seat[1], al);
			}
			if(AlreadyBookedSeats.containsKey(seat1[0])) {
				ArrayList<Integer> al = AlreadyBookedSeats.get(seat1[0]);
				if(al != null && al.size() > 0 && al.contains(Integer.valueOf(seat[1]))) {
					al.remove(Integer.valueOf(seat[1]));
					logger.info("old seat {} removed from AlreadyBookedSeats. new list: {}", seat[1], al);
				}
				al.add(Integer.valueOf(seat1[1]));
				AlreadyBookedSeats.put(seat1[0], al);
				logger.info("new seat {} added to AlreadyBookedSeats. updated list: {}", seat1[1], al);
			} else {
				ArrayList<Integer> al = new ArrayList<>();
				al.add(Integer.valueOf(seat1[1]));
				AlreadyBookedSeats.put(seat1[0], al);
				logger.info("new seat {} added to AlreadyBookedSeats. new list: {}", seat1[1], al);
			}
			seatAllocation.put(userId, newSeat);
			return ResponseEntity.ok("Seat for user " + userId + " modified to " + newSeat);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	private String assignSeat(String userId, String section) {
		int number = 1;

		if (users.containsKey(userId) && seatAllocation.containsKey(userId)) {
			String removedSeat = seatAllocation.get(userId);
			String[] seat = removedSeat.split("-");
			if(releasedSeats.containsKey(seat[0])) {
				ArrayList<Integer> al = releasedSeats.get(seat[0]);
				al.add(Integer.valueOf(seat[1]));
				releasedSeats.put(seat[0], al);
				logger.info("request received for existing user {}, old seat added to releasedSeats."
						+ "updated list: {}", userId, releasedSeats);
			} else {
				ArrayList<Integer> al = new ArrayList<>();
				al.add(Integer.valueOf(seat[1]));
				releasedSeats.put(seat[0], al);
				logger.info("request received for existing user {}, old seat added to releasedSeats."
						+ "new list: {}", userId, releasedSeats);
			}
		}

		if(releasedSeats.containsKey(section)) {
			ArrayList<Integer> al = releasedSeats.get(section);
			if(al != null && al.size() > 0) {
				number = (int) al.remove(0);
				logger.info("Assigning seat {} from releasedSeats. updated list: {}", number, releasedSeats);
				ArrayList<Integer> abl = AlreadyBookedSeats.get(section);
				if(abl != null) {
					abl.add(number);
				} else {
					abl = new ArrayList<>();
					abl.add(number);
				}
				
				AlreadyBookedSeats.put(section, abl);
				return (section + "-" + number);
			}
		}

		if(seatMaintenence.containsKey(section)) {
			number = seatMaintenence.get(section) + 1;
			ArrayList<Integer> al = AlreadyBookedSeats.get(section);
			if(al != null && al.size() > 0) {
				final int a = number;
				al.removeIf(i -> i < a);
				while(al.contains(number)) {
					logger.info("seat number {} already found in AlreadyBookedSeats. updating number.", number);
					number++;
				}
			}

			seatMaintenence.put(section, number);
		} else {
			number = 1;
			if(AlreadyBookedSeats.containsKey(section)) {
				ArrayList<Integer> al = AlreadyBookedSeats.get(section);
				if(al != null && al.size() > 0) {
					while(al.contains(number)) {
						logger.info("seat number {} already found in AlreadyBookedSeats. updating number.", number);
						number++;
					}
				}
			}
			seatMaintenence.put(section, number);
		}

		return (section + "-" + number);

	}
}
