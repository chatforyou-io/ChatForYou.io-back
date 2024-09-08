package com.chatforyou.io.services;

import com.chatforyou.io.client.OpenViduHttpException;
import com.chatforyou.io.client.OpenViduJavaClientException;
import com.chatforyou.io.models.OpenViduWebhookData;
import org.apache.coyote.BadRequestException;

public interface OpenViduWebhookService {
    void processWebhookEvent(OpenViduWebhookData webhookData) throws OpenViduJavaClientException, OpenViduHttpException, BadRequestException;
}
