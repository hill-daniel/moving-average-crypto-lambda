package org.kiwi.crypto.currency;

import java.util.Collection;

public interface CurrencyMapper<T> {

    Collection<Currency> map(T value);
}
