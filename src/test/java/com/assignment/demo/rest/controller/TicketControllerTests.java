package com.assignment.demo.rest.controller;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.assignment.demo.DemoApplication;
import com.assignment.demo.dto.TicketPurchaseRequest;
import com.assignment.demo.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestMethodOrder(OrderAnnotation.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = DemoApplication.class)
public class TicketControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    void testPurchaseTicket() throws Exception {
        TicketPurchaseRequest request = new TicketPurchaseRequest();
        request.setFrom("London");
        request.setTo("France");
        request.setSection("A");

        User user = new User();
        user.setFirstName("Lokesh");
        user.setLastName("Kumar");
        user.setEmailAddress("lokesh.kumar@example.com");
        request.setUser(user);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/tickets/purchase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ticket purchased successfully")));
    }

    @Test
    @Order(2)
    void testGetReceipt() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/tickets/receipt/LokeshKumar"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Receipt details")))
                .andExpect(content().string(containsString("Lokesh Kumar")))
                .andExpect(content().string(containsString("Price Paid: $20")));
    }

    @Test
    @Order(3)
    void testGetUsersBySection() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/tickets/users?section=A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.LokeshKumar").value("A-1"));
    }

    @Test
    @Order(4)
    void testModifyUserSeat() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/tickets/modify-seat/LokeshKumar?newSeat=A-10"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Seat for user LokeshKumar modified to A-10")));
    }
    
    @Test
    @Order(5)
    void testRemoveUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/tickets/remove/LokeshKumar"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User with ID LokeshKumar removed")));
    }
}
