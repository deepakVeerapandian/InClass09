package com.example.inclass09;

import java.io.Serializable;

public class Email implements Serializable  {
    String subject, date, emailMsg, senderFirstName, senderLastName;
    int email_msg_id;

    public Email() {
    }
}
