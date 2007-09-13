package com.tumri.joz.campaign;

/**
 * This class gets thrown when there was some error while the campaign data loading process from the repository.
 *
 * @author bpatel
 */
public class CampaignDataLoadingException extends Exception {

    public CampaignDataLoadingException() {
    }

    public CampaignDataLoadingException(String message) {
        super(message);
    }

    public CampaignDataLoadingException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public CampaignDataLoadingException(Throwable throwable) {
        super(throwable);
    }
}
