package com.example.demo.dto.support;

public class SupportReplyDTO {

    private Long messageId;

    private String replyText;

    public SupportReplyDTO() {}

    public SupportReplyDTO(Long messageId, String replyText) {
        this.messageId = messageId;
        this.replyText = replyText;
    }

    public Long getMessageId() { return messageId; }

    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public String getReplyText() { return replyText; }

    public void setReplyText(String replyText) { this.replyText = replyText; }
}
