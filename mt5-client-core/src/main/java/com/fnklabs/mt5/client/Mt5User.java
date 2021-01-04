package com.fnklabs.mt5.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Mt5User {
    @JsonProperty("ID")
    private Long id;

    @JsonProperty("Registration")
    private Timestamp registrationDate;

    @JsonProperty("Login")
    private String login;

    @JsonProperty("Group")
    private String group;

    @JsonProperty("Leverage")
    private Integer leverage;

    @JsonProperty("Balance")
    private BigDecimal balance;

    @JsonProperty("FirstName")
    private String firstName;

    @JsonProperty("MiddleName")
    private String middleName;

    @JsonProperty("LastName")
    private String lastName;

    @JsonProperty("Password")
    private String password;

    @JsonProperty("Country")
    private String country;

    @JsonProperty("City")
    private String city;

    @JsonProperty("State")
    private String state;

    @JsonProperty("ZipCode")
    private String zip;

    @JsonProperty("Address")
    private String address;

    @JsonProperty("Phone")
    private String phone;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("Comment")
    private String comment;

    @JsonProperty("Color")
    private long color;

    @JsonProperty("Equity")
    private BigDecimal equity;

    @JsonProperty("MarginFree")
    private BigDecimal marginFree;

    public BigDecimal getMarginFree() {
        return marginFree;
    }

    public void setMarginFree(BigDecimal marginFree) {
        this.marginFree = marginFree;
    }

    public long getColor() {
        return color;
    }

    public void setColor(long color) {
        this.color = color;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public Timestamp getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Timestamp registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getEquity() {
        return equity;
    }

    public void setEquity(BigDecimal equity) {
        this.equity = equity;
    }

    public BigDecimal getMargin() {
        return marginFree;
    }

    public void setMargin(BigDecimal margin) {}

    public BigDecimal getFreeMargin() {
        return marginFree;
    }

    public void setFreeMargin(BigDecimal freeMargin) {}

    public DateTime registrationDate() {
        return new DateTime(getRegistrationDate().getTime());
    }

    public DateTime getLastDate() {
        return null;
    }

    public int getLeverage() {
        return leverage;
    }

    public String getFullname() {
        return String.format("%s %s", getFirstName(), getLastName()).trim();
    }

    public String getPassword() {
        return password;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZipCode() {
        return getZip();
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setLeverage(Integer leverage) {
        this.leverage = leverage;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
