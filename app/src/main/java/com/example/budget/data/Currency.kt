package com.example.budget.data

enum class Currency(val symbol: String, val displayName: String, val code: String) {
    TAKA("৳", "Taka", "BDT"),
    DOLLAR("$", "Dollar", "USD"),
    RUPEE("₹", "Rupee", "INR"),
    EURO("€", "Euro", "EUR"),
    POUND("£", "Pound", "GBP"),
    YEN("¥", "Yen", "JPY");

    companion object {
        fun fromCode(code: String): Currency {
            return values().find { it.code == code } ?: DOLLAR
        }
    }
} 