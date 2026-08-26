package application.dto;

import jakarta.validation.constraints.NotBlank;

public class PagoBankyRequest {

    @NotBlank
    private String status;

    @NotBlank
    private String transactionCode;

    private String cardBrand;
    private String cardLastFourDigits;
    private String cardholderName;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }
    public String getCardBrand() { return cardBrand; }
    public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }
    public String getCardLastFourDigits() { return cardLastFourDigits; }
    public void setCardLastFourDigits(String cardLastFourDigits) { this.cardLastFourDigits = cardLastFourDigits; }
    public String getCardholderName() { return cardholderName; }
    public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }
}
