package com.examples.opencar.geo;

 class Amount
{

    private String value;

    private String currency;

    public void setValue(String value){
        this.value = value;
    }
    public String getValue(){
        return this.value;
    }
    public void setCurrency(String currency){
        this.currency = currency;
    }
    public String getCurrency(){
        return this.currency;
    }
}
class Confirmation
{
    private String type;

    private String enforce;

    private String return_url;

    public void setType(String type){
        this.type = type;
    }
    public String getType(){
        return this.type;
    }
    public void setEnforce(String enforce){
        this.enforce = enforce;
    }
    public String getEnforce(){
        return this.enforce;
    }
    public void setReturn_url(String return_url){
        this.return_url = return_url;
    }
    public String getReturn_url(){
        return this.return_url;
    }
}


public class MyPayment
{
    private String payment_token;

    private Amount amount;

    private Confirmation confirmation;

    private String capture;

    private String description;

    public void setPayment_token(String payment_token){
        this.payment_token = payment_token;
    }
    public String getPayment_token(){
        return this.payment_token;
    }
    public void setAmount(Amount amount){
        this.amount = amount;
    }
    public Amount getAmount(){
        return this.amount;
    }
    public void setConfirmation(Confirmation confirmation){
        this.confirmation = confirmation;
    }
    public Confirmation getConfirmation(){
        return this.confirmation;
    }
    public void setCapture(String capture){
        this.capture = capture;
    }
    public String getCapture(){
        return this.capture;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public String getDescription(){
        return this.description;
    }
}
