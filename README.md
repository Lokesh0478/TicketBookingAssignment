# TicketBookingAssignment
Ticket Booking Assignment
Following are the APIs created in this project:
1. Purchase Ticket: [POST] http://localhost:8080/api/tickets/purchase
  Json: {
    "from": "London",
    "to": "France",
    "section": "A",
    "user": {
        "firstName": "first",
        "lastName": "last",
        "emailAddress": "first.last@example.com"
    }
}
2. Get Receipt Details: [GET] http://localhost:8080/api/tickets/receipt/firstlast
3. Get users and seats by section: [GET] http://localhost:8080/api/tickets/users?section=A
4. Delete user: [DEL] http://localhost:8080/api/tickets/remove/firstlast
5. Modify seat: [PUT] http://localhost:8080/api/tickets/modify-seat/firstlast?newSeat=A-13



