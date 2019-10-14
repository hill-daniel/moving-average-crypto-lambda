package org.kiwi.crypto.api;

import java.util.Collection;
import org.kiwi.crypto.currency.Currency;

public interface CurrencyRepository {

    Collection<Currency> retrieveCurrencies();
}
