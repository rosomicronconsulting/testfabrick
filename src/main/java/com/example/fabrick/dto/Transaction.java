package com.example.fabrick.dto;

import lombok.Data;

@Data
public class Transaction {
	private String transactionId;
	private String operationId;
	private String accountingDate;
	private String valueDate;
	private TransactionType type;
	private Float amount ;
	private String currency;
	private String description;
}
