package com.batix.rundeck;

public class AnsibleStepException extends Exception {
    protected AnsibleFailureReason failureReason;

    public AnsibleStepException(String msg, AnsibleFailureReason reason) {
        super(msg);
        this.failureReason = reason;
    }

    public AnsibleStepException(Throwable cause, AnsibleFailureReason reason) {
        super(cause);
        this.failureReason = reason;
    }

    public AnsibleStepException(String msg, Throwable cause, AnsibleFailureReason reason) {
        super(msg, cause);
        this.failureReason = reason;
    }

    public AnsibleFailureReason getFailureReason() {
        return failureReason;
    }
}
