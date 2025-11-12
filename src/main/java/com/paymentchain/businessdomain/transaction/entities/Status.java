package com.paymentchain.businessdomain.transaction.entities;

public enum Status {
    PENDING("01", "Pendiente"),
    SETTLED("02", "Liquidada"),
    REJECTED("03", "Rechazada"),
    CANCELLED("04", "Cancelada");

    private final String code;
    private final String descripction;


    Status(String code, String descripction) {
        this.code = code;
        this.descripction = descripction;
    }

    public String getCode() {
        return code;
    }

    public String getDescripction() {
        return descripction;
    }

    public static Status fromCode(String code){
        if(!code.isBlank()){
            for(Status status : Status.values()){
                if (status.getCode().equalsIgnoreCase(code)){
                    return status;
                }

            }
        }
        return null;
    }
}
