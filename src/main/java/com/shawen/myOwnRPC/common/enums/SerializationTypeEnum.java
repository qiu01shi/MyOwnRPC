package com.shawen.myOwnRPC.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SerializationTypeEnum {

    OBJECT((byte) 0x00, "object"),
    JSON((byte) 0x01, "json"),
    KYRO((byte) 0x02, "kyro"),
    PROTOSTUFF((byte) 0x03, "protostuff"),
    HESSIAN((byte) 0X04, "hessian");



    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (SerializationTypeEnum c : SerializationTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }

}