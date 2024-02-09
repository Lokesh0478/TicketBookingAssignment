package com.assignment.demo.dto;

import com.assignment.demo.entity.User;

import lombok.Data;

@Data
public class TicketPurchaseRequest {

	private String from;
    private String to;
    private String section;
    private User user;

}
