package com.example.fabrick.controller;

import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.fabrick.dto.Account;
import com.example.fabrick.dto.BalanceDTO;
import com.example.fabrick.dto.BalanceResponseDTO;
import com.example.fabrick.dto.Creditor;
import com.example.fabrick.dto.LegalPersonBeneficiary;
import com.example.fabrick.dto.MoneyTransfer;
import com.example.fabrick.dto.NaturalPersonBeneficiary;
import com.example.fabrick.dto.TaxRelief;
import com.example.fabrick.dto.Transaction;
import com.example.fabrick.dto.TransactionResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class MainController {

	@Value("${baseurl}")
	String BASE_URL;
	@Value("${apikey}")
	String apikey;
	@Value("${Auth-Schema}")
	String authschema;
	@Value("${accountId}")
	String accountId;
	
	@GetMapping("/saldo")
	public BalanceDTO getBalance() {
		String url = BASE_URL + String.format("/api/gbs/banking/v4.0/accounts/%s/balance", accountId);
		HttpHeaders headers = new HttpHeaders();
		headers.set("Auth-Schema", authschema);
		headers.set("apikey",apikey);

		HttpEntity request = new HttpEntity(headers);

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<BalanceResponseDTO> response = restTemplate.exchange(url, HttpMethod.GET, request, BalanceResponseDTO.class);
		BalanceResponseDTO dto = response.getBody();
		
		return dto.getPayload();
	}
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	@GetMapping("/bonifico")
	public void bonifico() {
		String url = BASE_URL + String.format("/api/gbs/banking/v4.0/accounts/%s/payments/money-transfers", accountId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Auth-Schema", authschema);
		headers.set("apikey",apikey);
		
		MoneyTransfer mt=buildMoneyTransfer();
		
		
		String mts="";
		try {
			mts=objectMapper.writeValueAsString(mt);
			System.out.println(mts);
			HttpEntity<MoneyTransfer> request = new HttpEntity<>(mt,headers);
		    
			// Il servizio è molto strano, restituisce risposte inattese in base alla valorizzazione o meno (null vs stringa vuota)
			//Inoltre il response type atteso cambio in base all'esito, serve approfondire
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
			
			System.out.println(response.getBody());
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@GetMapping("/movimenti")
	public Transaction[] getTransaction(@RequestParam String from, @RequestParam String to) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
		//TODO: check from e to
		
		String url = BASE_URL + String.format("/api/gbs/banking/v4.0/accounts/%s/transactions?fromAccountingDate=%s&toAccountingDate=%S", accountId,from,to);
	
		HttpHeaders headers = new HttpHeaders();
		headers.set("Auth-Schema", authschema);
		headers.set("apikey",apikey);

		HttpEntity request = new HttpEntity(headers);

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<TransactionResponseDTO> response = restTemplate.exchange(url, HttpMethod.GET, request, TransactionResponseDTO.class);
		Transaction[] dto = response.getBody().getPayload();
		return dto;
	}
	
	private MoneyTransfer buildMoneyTransfer() {
		MoneyTransfer mt = new MoneyTransfer();
		mt.setAmount(10F);
		Creditor c = new Creditor();
		c.setName("John Doe");
		Account acc = new Account();
		acc.setAccountCode("IT23A0336844430152923804660");
		acc.setBicCode("SELBIT2BXXX");
		c.setAccount(acc);
		mt.setCreditor(c);
		mt.setDescription("Payment invoice 75/2017");
		mt.setExecutionDate("2019-04-01");
		mt.setUri("REMITTANCE_INFORMATION");
		mt.setFeeType("SHA");
		mt.setFeeAccountId("45685475");
		mt.setCurrency("EUR");
		mt.setIsInstant(false);
		mt.setIsUrgent(false);
		
		TaxRelief tr = new TaxRelief();
		tr.setBeneficiaryType("NATURAL_PERSON");
		tr.setCreditorFiscalCode("56258745832");
		tr.setTaxReliefId("L449");
		NaturalPersonBeneficiary npb = new NaturalPersonBeneficiary();
		npb.setFiscalCode1("MRLFNC81L04A859L");
		npb.setFiscalCode2(null);
		npb.setFiscalCode3(null);
		npb.setFiscalCode4(null);
		npb.setFiscalCode5(null);
		tr.setIsCondoUpgrade(false);
		tr.setNaturalPersonBeneficiary(npb);
		LegalPersonBeneficiary lpb = new LegalPersonBeneficiary();
		lpb.setFiscalCode(null);
		lpb.setLegalRepresentativeFiscalCode(null);
		tr.setLegalPersonBeneficiary(lpb);
		mt.setTaxRelief(tr);
		return mt;
	}
}
