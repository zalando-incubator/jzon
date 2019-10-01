package org.zalando.jzon.service;

import static org.zalando.jzon.service.PayloadKeyParser.SEPARATOR;

public abstract class KnownKeys {

    public static final String KEY_CUSTOMER = "customer";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_NUMBER = "number";
    public static final String KEY_HASH = "hash";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_SIMPLE_SKU = "simple_sku";
    public static final String KEY_SALES_CHANNEL = "sales_channel";
    public static final String KEY_CUSTOMER_EMAIL = KEY_CUSTOMER + SEPARATOR + KEY_EMAIL;
    public static final String KEY_CUSTOMER_NUMBER = KEY_CUSTOMER + SEPARATOR + KEY_NUMBER;
    public static final String KEY_CUSTOMER_HASH = KEY_CUSTOMER + SEPARATOR + KEY_HASH;
    public static final String KEY_CUSTOMER_ADDRESS = KEY_CUSTOMER + SEPARATOR + KEY_ADDRESS;
    public static final String KEY_LAST_NAME = "last_name";

}
